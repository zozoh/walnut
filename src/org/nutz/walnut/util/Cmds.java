package org.nutz.walnut.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.TextTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.callback.WnStrToken;
import org.nutz.walnut.util.callback.WnStrTokenCallback;
import org.nutz.walnut.util.validate.WnMatch;

public abstract class Cmds {

    static char[] EC_CMD_ARGS = Wchar.array('n',
                                            '\n',
                                            'r',
                                            '\r',
                                            't',
                                            '\t',
                                            'b',
                                            '\b',
                                            '\'',
                                            '\'',
                                            '"',
                                            '"',
                                            '`',
                                            '`',
                                            '\\',
                                            '\\',
                                            ' ',
                                            ' ');
    static Wchar.EscapeTable ET_CMD_ARGS = Wchar.buildEscapeTable(EC_CMD_ARGS);

    public static char escapeChar(char c) {
        return ET_CMD_ARGS.get(c);
    }

    /**
     * 将一个单行命令，拆成一个个参数。其中，引号/转义字符会被用掉。
     * 
     * @param cmdLine
     *            单行命令
     * @return
     */
    public static String[] splitCmdArgs(String cmdLine) {
        List<String> items = new LinkedList<>();
        // 存储字符串的临时栈。逃逸字符都加入到里面
        StringBuilder stack = new StringBuilder();

        Ws.splitQuoteToken(cmdLine, "'\"", " \t\r\n", new WnStrTokenCallback() {
            public char escape(char c) {
                return ET_CMD_ARGS.get(c);
            }

            public void invoke(WnStrToken token) {
                switch (token.type) {
                // 引号
                case QUOTE:
                    // 前面的内容独立为一项
                    if (stack.length() > 0) {
                        items.add(stack.toString());
                    }
                    // 还有新内容，也成为一项
                    if (token.hasText()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(token.quoteC);
                        sb.append(token.text);
                        sb.append(token.quoteC);
                        items.add(sb.toString());
                    }
                    // 重置
                    stack.delete(0, stack.length());
                    break;
                // 普通文字
                case TEXT:
                    stack.append(token.text);
                    break;
                // 连续的引号
                // 分隔符
                // 会导致开启一个新项
                case SEPERATOR:
                    if (stack.length() > 0) {
                        items.add(stack.toString());
                        stack.delete(0, stack.length());
                    }
                    break;
                // 不可能
                default:
                    throw Lang.impossible();
                }
            }
        });

        if (stack.length() > 0) {
            items.add(stack.toString());
        }

        String[] re = new String[items.size()];
        int i = 0;
        for (String it : items) {
            re[i++] = Ws.trim(it);
        }
        return re;
    }

    static char[] EC_CMD_ATOMS = Wchar.array('|', '|');
    static Wchar.EscapeTable ET_CMD_ATOMS = Wchar.buildEscapeTable(EC_CMD_ATOMS);

    /**
     * 考虑到文本行尾连接到命令行拆分。其中引号/转义字符会被保留
     * 
     * @param cmdText
     *            命令文本
     * @return 拆分到一个个逻辑命令行
     */
    public static String[] splitCmdAtoms(String cmdText) {
        List<String> lines = new LinkedList<>();
        // 存储字符串的临时栈。逃逸字符都加入到里面
        StringBuilder stack = new StringBuilder();

        Ws.splitQuoteToken(cmdText, "`'\"", "|", new WnStrTokenCallback() {
            public char escape(char c) {
                return ET_CMD_ATOMS.get(c);
            }

            public void invoke(WnStrToken token) {
                switch (token.type) {
                // 引号
                case QUOTE:
                    stack.append(token.quoteC);
                    stack.append(token.text);
                    stack.append(token.quoteC);
                    break;
                // 普通文字
                // 结尾文字
                case TEXT:
                    stack.append(token.text);
                    break;
                // 分隔符
                // 会导致开启一个新项
                case SEPERATOR:
                    if (stack.length() > 0) {
                        lines.add(stack.toString());
                        stack.delete(0, stack.length());
                    }
                    break;
                // 不可能
                default:
                    throw Lang.impossible();
                }
            }
        });

        if (stack.length() > 0) {
            lines.add(stack.toString());
        }

        String[] re = new String[lines.size()];
        int i = 0;
        for (String line : lines) {
            re[i++] = Ws.trim(line);
        }
        return re;
    }

    static char[] EC_CMD_LINES = Wchar.array(';', ';', '\r', ' ', '\n', ' ');
    static Wchar.EscapeTable ET_CMD_LINES = Wchar.buildEscapeTable(EC_CMD_LINES);

    /**
     * 考虑到文本行尾连接到命令行拆分。其中引号/转义字符会被保留
     * 
     * @param cmdText
     *            命令文本
     * @return 拆分到一个个逻辑命令行
     */
    public static String[] splitCmdLines(String cmdText) {
        List<String> lines = new LinkedList<>();
        // 存储字符串的临时栈。逃逸字符都加入到里面
        StringBuilder stack = new StringBuilder();

        Ws.splitQuoteToken(cmdText, "`'\"", "\n;", new WnStrTokenCallback() {
            public char escape(char c) {
                return ET_CMD_LINES.get(c);
            }

            public void invoke(WnStrToken token) {
                switch (token.type) {
                // 引号
                case QUOTE:
                    stack.append(token.quoteC);
                    stack.append(token.text);
                    stack.append(token.quoteC);
                    break;
                // 普通文字
                // 结尾文字
                case TEXT:
                    stack.append(token.text);
                    break;
                // 分隔符
                // 会导致开启一个新项
                case SEPERATOR:
                    if (stack.length() > 0) {
                        lines.add(stack.toString());
                        stack.delete(0, stack.length());
                    }
                    break;
                // 不可能
                default:
                    throw Lang.impossible();
                }
            }
        });

        if (stack.length() > 0) {
            lines.add(stack.toString());
        }

        String[] re = new String[lines.size()];
        int i = 0;
        for (String line : lines) {
            re[i++] = Ws.trim(line);
        }
        return re;
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

    public static <T> T readConfig(WnSystem sys, ZParams params, Class<T> classOfT) {
        if (params.vals.length > 0) {
            String phConf = params.val_check(0);
            WnObj oConf = Wn.checkObj(sys, phConf);
            return sys.io.readJson(oConf, classOfT);
        }
        // 从标准输入读取
        String json = sys.in.readAll();
        return Json.fromJson(classOfT, json);
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

    private static List<NutBean> outs(ZParams params, List<? extends NutBean> list) {
        List<NutBean> outs = new ArrayList<>(list.size());
        WnMatch keyMatch = __gen_obj_key_match(params, "e");
        for (NutBean o : list) {
            NutBean out = Wobj.filterObjKeys(o, keyMatch);
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
        List<NutBean> outs = outs(params, list);

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
                                           List<NutBean> outs,
                                           String tmplKey) {
        WnTmpl tmpl = Cmds.parse_tmpl(params.get(tmplKey));

        boolean show_index = params.is("i");

        // 主体
        int i = params.getInt("ibase", 0);
        for (NutBean map : outs) {
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

    private static WnMatch __gen_obj_key_match(ZParams params, String key) {
        String str = params.getString(key);

        return Wobj.explainObjKeyMatcher(str);
    }

    public static void output_objs_as_value(WnSystem sys, ZParams params, List<NutBean> outs) {
        String sep = params.get("sep", "");
        for (NutBean map : outs) {
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
                                            List<? extends NutBean> outs) {
        String sCols = params.get("t");
        String[] cols = Strings.splitIgnoreBlank(sCols);

        boolean showBorder = params.is("b");
        boolean showHeader = params.is("h");
        boolean showSummary = params.is("s");
        boolean showIndex = params.is("i");
        int indexBase = params.getInt("ibase", 0);

        output_objs_as_table(sys,
                             wp,
                             outs,
                             cols,
                             showBorder,
                             showHeader,
                             showSummary,
                             showIndex,
                             indexBase);

    }

    public static void output_objs_as_table(WnSystem sys,
                                            WnPager wp,
                                            List<? extends NutBean> outs,
                                            String[] cols,
                                            boolean showBorder,
                                            boolean showHeader,
                                            boolean showSummary,
                                            boolean showIndex,
                                            int indexBase) {
        if (showIndex) {
            cols = Lang.arrayFirst("#", cols);
        }

        // 准备输出表
        TextTable tt = new TextTable(cols.length);
        if (showBorder) {
            tt.setShowBorder(true);
        } else {
            tt.setCellSpacing(2);
        }
        // 加标题
        if (showHeader) {
            tt.addRow(cols);
            tt.addHr();
        }
        // 主体
        int i = indexBase;
        for (NutBean map : outs) {
            List<String> cells = new ArrayList<String>(cols.length);
            for (String key : cols) {
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
        if (showSummary) {
            tt.addHr();
        }
        // 输出
        sys.out.print(tt.toString());
        if (showSummary) {
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
                                           List<NutBean> outs) {
        JsonFormat fmt = Cmds.gen_json_format(params);

        // 要输出的 JSON 字符串
        String json = null;

        // 打印分页信息的 JSON 对象
        if (null != wp && wp.countPage) {
            NutMap re = createQueryResult(wp, outs);
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

    public static NutMap createQueryResult(WnPager wp, List<?> list) {
        NutMap re = new NutMap();
        re.setv("list", list);
        re.setv("pager",
                Lang.mapf("pn:%d,pgsz:%d,pgnb:%d,sum:%d,skip:%d,nb:%d,pgc:%d,count:%d",
                          wp.pn,
                          wp.pgsz,
                          wp.sum_page,
                          wp.sum_count,
                          wp.skip,
                          list.size(),
                          wp.sum_page,
                          list.size()));
        return re;
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
        return WnTmpl.exec(tmpl, "@", context, false);
    }

    public static WnTmpl parse_tmpl(String tmpl) {
        return WnTmpl.parse(tmpl, "@");
    }

    private static void joinObjs(WnSystem sys,
                                 WnObj p,
                                 String[] path,
                                 int index,
                                 List<WnObj> list,
                                 int mode) {
        String name = path[index];
        // 中间路径，递归
        if (index < path.length - 1) {
            // 如果是 ID 类型，就获取一个对象
            if (name.startsWith("id:")) {
                String id = name.substring(3);
                WnObj o = sys.io.get(id);
                if (null != o) {
                    joinObjs(sys, o, path, index + 1, list, mode);
                }
            }
            // 其他的查询一下子节点
            else {
                List<WnObj> children = sys.io.getChildren(p, name);
                for (WnObj child : children) {
                    joinObjs(sys, child, path, index + 1, list, mode);
                }
            }
        }
        // 最后一个了，计入列表
        else {
            // 如果是 ID 类型，就获取一个对象
            if (name.startsWith("id:")) {
                String id = name.substring(3);
                WnObj o = sys.io.get(id);
                if (null != o) {
                    list.add(o);
                }
            }
            // 其他的查询一下子节点
            else {
                sys.io.eachChild(p, name, new Each<WnObj>() {
                    public void invoke(int index, WnObj ele, int length) {
                        list.add(ele);
                    }
                });
            }
        }
    }

    public static WnObj evalCandidateObjs(WnSystem sys,
                                          String[] paths,
                                          final List<WnObj> list,
                                          int mode) {
        WnObj cuo = sys.getCurrentObj();

        if (null != paths) {
            for (String ph : paths) {
                String aph = Wn.normalizePath(ph, sys);
                WnObj p = cuo;

                // 顶级路径
                if (aph.startsWith("/")) {
                    p = null;
                }

                // 如果有通配符，那么逐层进入
                if (aph.contains("*")) {
                    String[] phList = Strings.splitIgnoreBlank(aph, "/");
                    joinObjs(sys, p, phList, 0, list, mode);
                }
                // 就是一个路径，直接获取
                else {
                    WnObj o = Wn.getObj(sys, ph);
                    if (null == o) {
                        // 输出 null
                        if (Wn.Cmd.isNoExistsNull(mode)) {
                            list.add(null);
                        }
                        // 忽略
                        else if (Wn.Cmd.isNoExistsIgnore(mode)) {

                        }
                        // 抛错
                        else {
                            throw Er.create("e.io.noexist", ph);
                        }
                    } else {
                        list.add(o);
                    }
                }
            }
        }

        if (list.isEmpty() && Wn.Cmd.isJoinCurrent(mode)) {
            list.add(cuo);
        }

        return cuo;
    }

    public static List<WnObj> evalCandidateObjsNoEmpty(WnSystem sys, String[] paths, int mode) {
        LinkedList<WnObj> list = new LinkedList<WnObj>();
        evalCandidateObjs(sys, paths, list, mode);
        assertCandidateObjsNoEmpty(paths, list);
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
        assertCandidateObjsNoEmpty(paths, list);
        return re;
    }

    public static WnObj evalCandidateObjs(WnSystem sys,
                                          WnObj p,
                                          String[] paths,
                                          final List<WnObj> list,
                                          int mode) {
        throw Lang.noImplement();
        // // 计算要列出的目录
        // // 没参数认为是当前目录
        // if (paths.length == 0) {
        // if (Wn.Cmd.isJoinCurrent(mode)) {
        // list.add(p);
        // }
        // }
        // // 否则根据路径归纳需要列的目录
        // else {
        // for (String val : paths) {
        // joinObjByPath(sys, list, p, val, mode);
        // }
        // }
        //
        // return p;
    }

    public static void assertCandidateObjsNoEmpty(String[] args, List<WnObj> list) {
        if (list.isEmpty()) {
            throw Er.create("e.io.obj.noexists", Lang.concat(", ", args));
        }
    }
}
