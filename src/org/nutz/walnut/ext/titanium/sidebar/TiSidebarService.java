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
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.ext.titanium.util.WnObjCachedFactory;
import org.nutz.walnut.util.Wn;

@IocBean
public class TiSidebarService {

    @Inject("refer:io")
    private WnIo io;

    @Inject("refer:sessionService")
    protected WnSessionService sess;

    @Inject("refer:usrService")
    protected WnUsrService usrs;

    private WnObjCachedFactory<TiSidebarInput> cache;

    public TiSidebarService() {
        this.cache = new WnObjCachedFactory<>();
    }

    private void __join_output(int depth,
                               TiSidebarInputItem inIt,
                               List<TiSidebarOutputItem> list,
                               WnSession sess,
                               WnCheckRoleOfByName check,
                               WnExecutable runtime) {
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

        // 组
        if (inIt.isGroup() && inIt.hasItems()) {
            TiSidebarOutputItem grp = new TiSidebarOutputItem(depth, inIt, null);
            List<TiSidebarOutputItem> items = new LinkedList<>();
            for (TiSidebarInputItem subIt : inIt.getItems()) {
                __join_output(depth + 1, subIt, items, sess, check, runtime);
            }
            if (items.size() > 0) {
                grp.setItems(items);
                list.add(grp);
            }
        }
        // 动态项目
        else if (inIt.hasCommand()) {
            String cmdText = inIt.getCommand();
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
                    list.add(it);
                }
            }
            // 单个对象
            else {
                NutMap o = Lang.obj2nutmap(reObj);
                TiSidebarOutputItem it = new TiSidebarOutputItem(depth, inIt, o);
                list.add(it);
            }
        }
        // 静态项目
        else {
            WnObj o = null;
            if (inIt.hasPath()) {
                String aph = Wn.normalizeFullPath(inIt.getPath(), sess);
                o = io.check(null, aph);
            }
            TiSidebarOutputItem it = new TiSidebarOutputItem(depth, inIt, o);
            list.add(it);
        }
    }

    public TiSidebarOutput getOutput(TiSidebarInput input,
                                     WnSession sess,
                                     WnCheckRoleOfByName check,
                                     WnExecutable runtime) {
        TiSidebarOutput output = new TiSidebarOutput();
        List<TiSidebarOutputItem> sidebar = new LinkedList<>();

        // 循环分析
        if (input.hasSidebar()) {
            for (TiSidebarInputItem inIt : input.getSidebar()) {
                this.__join_output(0, inIt, sidebar, sess, check, runtime);
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
