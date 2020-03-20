package org.nutz.walnut.ext.titanium.sidebar;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnObjDataCachedFactory;

@IocBean(create = "on_create")
public class TiSidebarService {

    @Inject("refer:io")
    private WnIo io;

    // @Inject("refer:sysAuthService")
    // protected WnAuthService auth;

    private WnObjDataCachedFactory<TiSidebarInput> cache;

    public void on_create() {
        this.cache = new WnObjDataCachedFactory<>(io);
    }

    private void __join_output(int depth,
                               TiSidebarInputItem inputItem,
                               List<TiSidebarOutputItem> list,
                               WnAuthSession sess,
                               NutBean tmplContext,
                               WnCheckRoleOfByName check,
                               WnExecutable runtime) {

        // 克隆一份自身
        TiSidebarInputItem inIt = inputItem.clone();

        // 检查权限
        if (inIt.hasRoles()) {
            for (Map.Entry<String, String> en : inIt.getRoles().entrySet()) {
                String roleName = en.getKey();
                String taPath = en.getValue();
                if (!check.exec(roleName, taPath)) {
                    return;
                }
            }
        }

        // 动态项目
        if (inIt.hasCommand()) {
            String cmdText = inIt.getCommand();
            cmdText = Tmpl.exec(cmdText, tmplContext);
            String re = Strings.trim(runtime.exec2(cmdText));

            // 空
            if ("null".equals(re))
                return;

            // 得到对象
            Object reObj = Json.fromJson(re);
            if (null == reObj)
                return;

            // 集合
            if (reObj instanceof Collection) {
                Collection<?> reList = (Collection<?>) reObj;
                for (Object ele : reList) {
                    NutMap o = Lang.obj2nutmap(ele);
                    TiSidebarOutputItem it = new TiSidebarOutputItem(depth, inIt, o);
                    // 计算子项目
                    TiSidebarInputItem inIt2 = inIt.clone();
                    __check_dynamic_item_children(depth,
                                                  inIt2,
                                                  sess,
                                                  tmplContext,
                                                  check,
                                                  runtime,
                                                  o,
                                                  it);
                    // 加入结果列表
                    list.add(it);
                }
            }
            // 单个对象
            else {
                NutMap o = Lang.obj2nutmap(reObj);
                TiSidebarOutputItem it = new TiSidebarOutputItem(depth, inIt, o);
                // 计算子项目
                __check_dynamic_item_children(depth,
                                              inIt,
                                              sess,
                                              tmplContext,
                                              check,
                                              runtime,
                                              o,
                                              it);
                // 加入结果列表
                list.add(it);
            }
        }
        // 组
        else if (inIt.isGroup() && inIt.hasItems()) {
            TiSidebarOutputItem grp = new TiSidebarOutputItem(depth, inIt, null);
            List<TiSidebarOutputItem> items = new LinkedList<>();
            for (TiSidebarInputItem subIt : inIt.getItems()) {
                __join_output(depth + 1, subIt, items, sess, tmplContext, check, runtime);
            }
            if (items.size() > 0) {
                grp.setItems(items);
                list.add(grp);
            }
        }
        // 静态项目
        else {
            WnObj o = null;
            if (inIt.hasPath()) {
                String path = inIt.getPath();
                path = Tmpl.exec(path, tmplContext);
                String aph = Wn.normalizeFullPath(path, sess);
                o = io.check(null, aph);
            }
            TiSidebarOutputItem it = new TiSidebarOutputItem(depth, inIt, o);
            list.add(it);
        }
    }

    private void __check_dynamic_item_children(int depth,
                                               TiSidebarInputItem inIt,
                                               WnAuthSession sess,
                                               NutBean tmplContext,
                                               WnCheckRoleOfByName check,
                                               WnExecutable runtime,
                                               NutMap o,
                                               TiSidebarOutputItem it) {
        if (inIt.hasItems()) {
            List<TiSidebarOutputItem> items = new LinkedList<>();
            for (TiSidebarInputItem subIt : inIt.getItems()) {
                // 这个子对象的路径需要格式化一下
                if (subIt.hasPath()) {
                    String ph = Tmpl.exec(subIt.getPath(), o);
                    subIt.setPath(ph);
                }
                // 计入
                __join_output(depth + 1, subIt, items, sess, tmplContext, check, runtime);
            }
            it.setItems(items);
        }
    }

    public TiSidebarOutput getOutput(TiSidebarInput input,
                                     WnAuthSession sess,
                                     WnCheckRoleOfByName check,
                                     WnExecutable runtime) {
        TiSidebarOutput output = new TiSidebarOutput();
        List<TiSidebarOutputItem> sidebar = new LinkedList<>();

        NutBean tmplContext = sess.toMapForClient();

        // 循环分析
        if (input.hasSidebar()) {
            for (TiSidebarInputItem inIt : input.getSidebar()) {
                this.__join_output(0, inIt, sidebar, sess, tmplContext, check, runtime);
            }
        }

        // 返回
        output.setSidebar(sidebar);
        return output;
    }

    public TiSidebarInput getInput(WnObj oSidebar) {
        if (null == oSidebar)
            return null;
        TiSidebarInput tsbi = cache.get(oSidebar, o -> {
            return io.readJson(oSidebar, TiSidebarInput.class);
        });
        return tsbi;
    }

}
