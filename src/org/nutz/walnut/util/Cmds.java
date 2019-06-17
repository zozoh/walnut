package org.nutz.walnut.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.TextTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.web.WebException;

public abstract class Cmds {

    /**
     * 考虑到文本行尾连接到命令行拆分
     * 
     * @param cmdText
     *            命令文本
     * @return 拆分到一个个逻辑命令行
     */
    public static String[] splitCmdLine(String cmdText) {
        // 首先处理行尾到连接符号
        String[] lines = Strings.trim(cmdText).split("\r?\n");

        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            int len = sb.length();
            // 有内容到话
            if (len > 0) {
                // 结尾是 '\\'
                if (sb.charAt(len - 1) == '\\') {
                    // 不计入回车，删除这个连接符
                    sb.setLength(len - 1);
                }
                // 计入一个回车
                else {
                    sb.append('\n');
                }
            }
            // 计入行到内容
            sb.append(line);
        }

        // 作为一行，拆分
        String str = sb.toString();
        return Strings.split(str, true, '\n', ';');
    }

    public static String getParamOrPipe(WnSystem sys,
                                        ZParams params,
                                        String key,
                                        boolean readPipeWhenBlank) {
        // 得到内容
        String str = params.get(key);
        if (((Strings.isBlank(str) && readPipeWhenBlank) || "true".equals(str)) && null != sys.in) {
            str = sys.in.readAll();
        }

        // 返回
        return Strings.trim(str);
    }

    public static String checkParamOrPipe(WnSystem sys,
                                          ZParams params,
                                          String key,
                                          boolean readPipeWhenBlank) {
        // 得到内容
        String str = getParamOrPipe(sys, params, key, readPipeWhenBlank);

        if (Strings.isBlank(str)) {
            throw Er.create("e.cmd.lack.param", key);
        }

        return str;

    }

    public static String getParamOrPipe(WnSystem sys, ZParams params, int index) {
        // 得到内容
        String str = params.val(index);
        if (Strings.isBlank(str) && null != sys.in) {
            str = sys.in.readAll();
        }

        // 返回
        return Strings.trim(str);
    }

    public static String checkParamOrPipe(WnSystem sys, ZParams params, int index) {
        // 得到内容
        String str = getParamOrPipe(sys, params, index);

        if (Strings.isBlank(str)) {
            throw Er.create("e.cmd.lack.param", "[" + index + "]");
        }

        return str;

    }

    // 静态帮助函数集合，不许实例化
    private Cmds() {}

    public static void output_objs(WnSystem sys,
                                   ZParams params,
                                   WnPager wp,
                                   List<? extends WnObj> list,
                                   boolean autoPath) {
        if (autoPath && (list.size() == 1 || params.is("P"))) {
            for (WnObj o : list) {
                if (null != o) {
                    o.path();
                    if (params.is("A")) {
                        List<WnObj> ancestors = new LinkedList<WnObj>();
                        o.loadParents(ancestors, false);
                        o.setv("ancestors", ancestors);
                    }
                }
            }
        }

        // 最后输出
        Cmds.output_beans(sys, params, wp, list);
    }

    private static List<NutMap> outs(ZParams params, List<? extends NutBean> list) {
        List<NutMap> outs = new ArrayList<NutMap>(list.size());
        for (NutBean o : list) {
            NutMap out = _obj_to_outmap(o, params);
            if (params.has("tree") && o.has("children")) {
                List<NutBean> children = o.getAsList("children", NutBean.class);
                out.setv("children", outs(params, children));
            }
            outs.add(out);
        }
        return outs;
    }

    public static void output_beans(WnSystem sys,
                                    ZParams params,
                                    WnPager wp,
                                    List<? extends NutBean> list) {
        // 生成输出列表
        List<NutMap> outs = outs(params, list);

        // // 指定按照 JSON 的方式输出
        // if (params.has("json")) {
        // output_objs_as_json(sys, params, wp, outs);
        // }
        // // 当没有更新，或者强制输出的时候，执行输出
        // else if (outs.size() > 0) {
        // 仅输出值
        if (params.is("V")) {
            output_objs_as_value(sys, params, outs);
        }
        // 按表格输出
        else if (params.has("t")) {
            output_objs_as_table(sys, params, wp, outs);
        }
        // 按照模板输出
        else if (params.has("tmpl")) {
            output_objs_by_tmpl(sys, params, wp, outs, "tmpl");
        }
        // 默认用 Json 的方法输出
        else {
            output_objs_as_json(sys, params, wp, outs);
        }
        // }
        // // 如果值为空
        //
        // else if (params.is("l") && params.is("json")) {
        // sys.out.println("[]");
        // }
    }

    public static void output_objs_by_tmpl(WnSystem sys,
                                           ZParams params,
                                           WnPager wp,
                                           List<NutMap> outs,
                                           String tmplKey) {
        Tmpl tmpl = Cmds.parse_tmpl(params.get(tmplKey));

        boolean show_index = params.is("i");

        // 主体
        int i = params.getInt("ibase", 0);
        for (NutMap map : outs) {
            if (show_index)
                sys.out.print("" + (i++) + "# ");
            String str = tmpl.render(map);
            sys.out.println(str);
        }
        // 尾部
        if (params.is("s")) {
            sys.out.println("---------------------------------------");
            // 是计算分页的
            if (null != wp && wp.countPage) {
                sys.out.printlnf("total %d/%d items, skip %d page %d/%d, %d per page",
                                 outs.size(),
                                 wp.sum_count,
                                 wp.skip,
                                 wp.pn,
                                 wp.sum_page,
                                 wp.pgsz);
            }
            // 就是显示列表
            else {
                sys.out.printlnf("total %d items", outs.size());
            }
        }
    }

    private static NutMap _obj_to_outmap(NutBean o, ZParams params) {
        if (null == o) {
            return null;
        }

        // zozoh 这个不判断不做了，用 gen_json_format 函数包含了这个逻辑
        // true 表示输出的时候，也显示双下划线开头的隐藏字段
        // boolean isShowAutoHide = params.is("H");

        // 字段过滤正则表达式
        Pattern p = null;
        boolean not = false;
        String regex = params.get("e");
        if (!Strings.isBlank(regex)) {
            if (regex.startsWith("!")) {
                not = true;
                regex = regex.substring(1);
            }
            p = Pattern.compile(regex);
        }

        // 依次判断字段
        NutMap map = new NutMap();
        for (String key : o.keySet()) {
            // 忽略自动隐藏字段
            // if (!isShowAutoHide && key.startsWith("__"))
            // continue;

            // 用正则表达式判断
            if (null != p) {
                if (p.matcher(key).matches()) {
                    if (!not)
                        map.put(key, o.get(key));
                } else if (not) {
                    map.put(key, o.get(key));
                }
            }
            // 那么一定要添加的
            else {
                map.put(key, o.get(key));
            }
        }
        return map;
    }

    public static void output_objs_as_value(WnSystem sys, ZParams params, List<NutMap> outs) {
        String sep = params.get("sep", "");
        for (NutMap map : outs) {
            sys.out.print(Lang.concat(sep, map.values()));
            if (params.is("N")) {
                sys.out.println();
            }
        }
        if (params.is("N"))
            sys.out.println();
    }

    public static void output_objs_as_table(WnSystem sys,
                                            ZParams params,
                                            WnPager wp,
                                            List<NutMap> outs) {
        String sCols = params.get("t");
        String[] aCols = Strings.splitIgnoreBlank(sCols);
        if (params.is("i")) {
            aCols = Lang.arrayFirst("#", aCols);
        }

        // 准备输出表
        TextTable tt = new TextTable(aCols.length);
        if (params.is("b")) {
            tt.setShowBorder(true);
        } else {
            tt.setCellSpacing(2);
        }
        // 加标题
        if (params.is("h")) {
            tt.addRow(aCols);
            tt.addHr();
        }
        // 主体
        int i = params.getInt("ibase", 0);
        for (NutMap map : outs) {
            List<String> cells = new ArrayList<String>(aCols.length);
            for (String key : aCols) {
                if ("#".equals(key)) {
                    cells.add("" + (i++));
                    continue;
                }
                Object v = Mapl.cell(map, key);
                cells.add(v == null ? null : v.toString());
            }
            tt.addRow(cells);
        }
        // 尾部
        if (params.is("s")) {
            tt.addHr();
        }
        // 输出
        sys.out.print(tt.toString());
        if (params.is("s")) {
            // 是计算分页的
            if (null != wp && wp.countPage) {
                sys.out.printlnf("total %d/%d items, skip %d page %d/%d, %d per page",
                                 outs.size(),
                                 wp.sum_count,
                                 wp.skip,
                                 wp.pn,
                                 wp.sum_page,
                                 wp.pgsz);
            }
            // 就是显示列表
            else {
                sys.out.printlnf("total %d items", outs.size());
            }
        }
    }

    public static void output_objs_as_json(WnSystem sys,
                                           ZParams params,
                                           WnPager wp,
                                           List<NutMap> outs) {
        JsonFormat fmt = Cmds.gen_json_format(params);

        // 要输出的 JSON 字符串
        String json = null;

        // 打印分页信息的 JSON 对象
        if (null != wp && wp.countPage) {
            NutMap re = new NutMap();
            re.setv("list", outs);
            re.setv("pager",
                    Lang.mapf("pn:%d,pgsz:%d,pgnb:%d,sum:%d,skip:%d,nb:%d,pgc:%d,count:%d",
                              wp.pn,
                              wp.pgsz,
                              wp.sum_page,
                              wp.sum_count,
                              wp.skip,
                              outs.size(),
                              wp.sum_page,
                              outs.size()));
            json = Json.toJson(re, fmt);
        }
        // 强制输出列表
        else if (params.is("l") || outs.size() > 1) {
            json = Json.toJson(outs, fmt);
        }
        // 显示一个单独对象
        else if (outs.size() == 1) {
            json = Json.toJson(outs.get(0), fmt);
        }

        // 输出
        sys.out.println(json);
    }

    public static JsonFormat gen_json_format(ZParams params) {
        String json = params.get("json");

        // 用 -cqn 方式
        if ("true".equals(json) || json == null) {
            JsonFormat fmt = params.is("c") ? JsonFormat.compact() : JsonFormat.forLook();
            fmt.setIgnoreNull(!params.is("n")).setQuoteName(params.is("q"));
            if (params.is("H"))
                fmt.setLocked("^__");
            return fmt;
        }
        // 更复杂的 json 格式设定
        else {
            return Json.fromJson(JsonFormat.class, json);
        }
    }

    public static String out_by_tmpl(String tmpl, NutBean context) {
        return Tmpl.exec(tmpl, "@", context, false);
    }

    public static Tmpl parse_tmpl(String tmpl) {
        return Tmpl.parse(tmpl, "@");
    }

    public static void joinObjByPath(WnSystem sys,
                                     final List<WnObj> list,
                                     WnObj p,
                                     String str,
                                     int mode) {
        // 分析路径
        str = Wn.normalizePath(str, sys);

        // 看看是否需要回到根
        if (str.startsWith("/") || null == p) {
            p = sys.io.getRoot();
        }

        // 将路径拆分成数组
        String[] ss = Strings.splitIgnoreBlank(str, "/");

        // 根节点
        if (ss.length == 0) {
            list.add(p);
            return;
        }
        // 试图按路径查找
        // 如果路径中有 id:xxx 那么就应该从这个位置开始
        // 尝试从后查找，如果有 id:xxx 那么就截断，因为前面的就木有意义了
        int off = 0;
        for (int i = ss.length - 1; i >= 0; i--) {
            String nm = ss[i];
            if (nm.startsWith("id:")) {
                try {
                    p = sys.io.checkById(nm.substring(3));
                }
                catch (WebException e) {
                    // 没找到，那么后面的路径接表找了
                    // 根据配置，看看是忽略呢，还是抛错
                    if (e.isKey("e.io.obj.noexists")) {
                        // 输出 null
                        if (Wn.Cmd.isNoExistsNull(mode)) {
                            list.add(null);
                            return;
                        }
                        // 忽略
                        else if (Wn.Cmd.isNoExistsIgnore(mode)) {
                            return;
                        }
                    }
                    // 没的处理，就抛出咯
                    throw e;
                }

                off = i + 1;
                break;
            }
        }

        // 直接干到结尾了
        if (off >= ss.length) {
            list.add(p);
            return;
        }

        // 递归查找
        __find_last_level_objs(sys.io, p, ss, off, list, mode);

    }

    private static void __find_last_level_objs(final WnIo io,
                                               WnObj p,
                                               final String[] ss,
                                               final int off,
                                               final List<WnObj> list,
                                               int mode) {
        String nm = ss.length > off ? ss[off] : null;

        // 当前目录
        if (".".equals(nm)) {
            __find_last_level_objs_handle(io, ss, off + 1, list, p, mode);
        }
        // 回退一级
        else if ("..".equals(nm)) {
            WnObj o = p.parent();
            __find_last_level_objs_handle(io, ss, off + 1, list, o, mode);
        }
        // 根据 ID
        else if (nm.startsWith("id:")) {
            try {
                WnObj o = io.checkById(nm.substring("id:".length()));
                __find_last_level_objs(io, o, ss, off + 1, list, mode);
            }
            catch (WebException e) {
                // 没找到，那么后面的路径接表找了
                // 根据配置，看看是忽略呢，还是抛错
                if (e.isKey("e.io.obj.noexists")) {
                    // 输出 null
                    if (Wn.Cmd.isNoExistsNull(mode)) {
                        list.add(null);
                        return;
                    }
                    // 忽略
                    else if (Wn.Cmd.isNoExistsIgnore(mode)) {
                        return;
                    }
                }
                // 没的处理，就抛出咯
                throw e;
            }
        }
        // 继续查找
        else {
            // WnQuery q = Wn.Q.pid(p.id()).setv("nm", nm);
            // io.each(q, new Each<WnObj>() {
            // public void invoke(int index, WnObj o, int length) {
            // __find_last_level_objs_handle(io, ss, off + 1, list, o);
            // }
            // });
            List<WnObj> children = io.getChildren(p, nm);
            // 木有，根据模式来处理
            if (children.isEmpty()) {
                // 输出 null
                if (Wn.Cmd.isNoExistsNull(mode)) {
                    list.add(null);
                    return;
                }
                // 忽略
                else if (Wn.Cmd.isNoExistsIgnore(mode)) {
                    return;
                }
                // 默认的，啥也不输出
            }
            // 处理每个子节点
            else {
                for (WnObj child : children) {
                    __find_last_level_objs_handle(io, ss, off + 1, list, child, mode);
                }
            }
        }
    }

    private static void __find_last_level_objs_handle(final WnIo io,
                                                      final String[] ss,
                                                      final int off,
                                                      final List<WnObj> list,
                                                      WnObj o,
                                                      int mode) {
        // 如果到了最后一层，才加入 list
        if (off >= ss.length) {
            if (null == o)
                list.add(io.getRoot());
            else
                list.add(o);
        }
        // 如果根本木有
        else if (null == o) {
            // 输出 null
            if (Wn.Cmd.isNoExistsNull(mode)) {
                list.add(null);
                return;
            }
            // 忽略
            else if (Wn.Cmd.isNoExistsIgnore(mode)) {
                return;
            }
        }
        // 否则继续递归
        else if (!o.isFILE()) {
            __find_last_level_objs(io, o, ss, off, list, mode);
        }
    }

    public static List<WnObj> evalCandidateObjsNoEmpty(WnSystem sys, String[] paths, int mode) {
        LinkedList<WnObj> list = new LinkedList<WnObj>();
        evalCandidateObjs(sys, paths, list, mode);
        checkCandidateObjsNoEmpty(paths, list);
        return list;
    }

    public static List<WnObj> evalCandidateObjs(WnSystem sys, String[] paths, int mode) {
        LinkedList<WnObj> list = new LinkedList<WnObj>();
        evalCandidateObjs(sys, paths, list, mode);
        return list;
    }

    public static WnObj evalCandidateObjsNoEmpty(WnSystem sys,
                                                 String[] paths,
                                                 final List<WnObj> list,
                                                 int mode) {
        WnObj re = evalCandidateObjs(sys, paths, list, mode);
        checkCandidateObjsNoEmpty(paths, list);
        return re;
    }

    public static WnObj evalCandidateObjs(WnSystem sys,
                                          String[] paths,
                                          final List<WnObj> list,
                                          int mode) {
        WnObj p = sys.getCurrentObj();
        return evalCandidateObjs(sys, p, paths, list, mode);
    }

    public static WnObj evalCandidateObjs(WnSystem sys,
                                          WnObj p,
                                          String[] paths,
                                          final List<WnObj> list,
                                          int mode) {
        // 计算要列出的目录
        // 没参数认为是当前目录
        if (paths.length == 0) {
            if (Wn.Cmd.isJoinCurrent(mode)) {
                list.add(p);
            }
        }
        // 否则根据路径归纳需要列的目录
        else {
            for (String val : paths) {
                joinObjByPath(sys, list, p, val, mode);
            }
        }

        return p;
    }

    public static void checkCandidateObjsNoEmpty(String[] args, List<WnObj> list) {
        if (list.isEmpty()) {
            throw Er.create("e.io.obj.noexists", Lang.concat(", ", args));
        }
    }
}
