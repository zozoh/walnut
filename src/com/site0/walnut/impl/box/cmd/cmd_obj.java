package com.site0.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnPager;
import com.site0.walnut.util.ZParams;

public class cmd_obj extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args,
                                       "iocnqhbslAVNPHQ",
                                       "^(IfNoExists|mine|pager|ExtendDeeply|hide|treeFlat)$");

        WnPager wp = new WnPager(params);

        NutMap sort = null;
        if (params.has("sort")) {
            sort = Lang.map(params.check("sort"));
        }

        // 如果是要更新，首先分析一下更新参数
        NutMap u_map = null;
        boolean pipeHasBeenUsed = false;
        if (params.has("u")) {
            String json = params.get("u");
            // 标准输入里读取
            if ("true".equals(json)) {
                json = sys.in.readAll();
                pipeHasBeenUsed = true;
            }
            // 解析 Map
            try {
                u_map = Lang.map(json);
            }
            catch (Exception e) {
                u_map = new NutMap();
            }
        }

        // 计算要列出的要处理的对象
        List<WnObj> list = new LinkedList<WnObj>();

        // 创建新对象
        if (params.has("new")) {
            String json = params.get("new", "{}");
            List<NutMap> metaNewList = null;
            NutMap meta = null;
            // 类似 cat xxx | obj -new -u 的方式创建对象
            if ("true".equals(json)) {
                // 从管道读取
                if (!pipeHasBeenUsed) {
                    json = Strings.trim(sys.in.readAll());
                    // 是数组
                    if (Strings.isQuoteBy(json, '[', ']')) {
                        metaNewList = Json.fromJsonAsList(NutMap.class, json);
                    }
                    // 就是一个单个对象咯
                    else {
                        meta = Lang.map(json);
                    }
                }
                // 管道已经被用了，直接搞个新的吧
                else {
                    meta = new NutMap();
                }
            }
            // 解析 JSON
            else {
                meta = Lang.map(json);
            }

            // 执行创建：单个对象
            if (null != meta) {
                WnObj o = __do_new(sys, params, meta);

                if (null == u_map) {
                    u_map = meta;
                } else {
                    u_map.putAll(meta);
                }

                // 记录到列表以备后续操作
                list.add(o);
            }
            // 执行创建：批量
            else if (null != metaNewList) {
                list = this.__do_new_list(sys, params, metaNewList);
            }

        }
        // 确保某一路径存在
        else if (params.has("check")) {
            String path = Wn.normalizeFullPath(params.check("check"), sys);

            WnRace race = params.getEnum("race", WnRace.class);
            if (null == race)
                race = WnRace.FILE;

            WnObj o = sys.io.createIfNoExists(null, path, race);
            list.add(o);

        }
        // 给定的路径
        else if (params.vals.length > 0) {
            List<WnObj> parents = __query_by_path(sys, args, params, sort);
            // 指定了匹配条件，就用这个再依次查一遍
            if (params.has("match")) {
                for (WnObj oP : parents) {
                    list.addAll(__query_by_match(sys, params, oP, wp, sort));
                }
            }
            // 否则就算是最终结果了
            else {
                list.addAll(parents);
            }

            // 指定了限制数量
            if (wp.limit > 0 && wp.limit < list.size()) {
                list = list.subList(0, wp.limit);
            }
        }
        // 指定查询
        else if (params.has("match")) {
            list = __query_by_match(sys, params, null, wp, sort);
        }
        // 全都没有，那么看看 u_map 里是不是有 id
        else if (null != u_map && u_map.has("id")) {
            String id = u_map.getString("id");
            list.add(sys.io.checkById(id));
        }
        // 啥都木有，那就用当前路径吧
        else {
            list.add(sys.getCurrentObj());
        }

        // 如果是按组查询
        String groupCount = params.get("GroupCount");
        if (!Strings.isBlank(groupCount)) {
            __do_groupCount(sys, params, list, groupCount);
            return;
        }

        // 需要递归展开所有的子
        if (params.has("ExtendFilter")) {
            List<WnObj> list2 = new LinkedList<WnObj>();
            String json = params.get("ExtendFilter");
            NutMap flt = "true".equals(json) ? new NutMap() : Lang.map(json);

            WnQuery q = new WnQuery();
            NutMap by = Lang.map(params.get("ExtendBy", "{}"));
            q.setAll(by);

            if (null != sort)
                q.sort(sort);

            boolean ExtendDeeply = params.is("ExtendDeeply", true);

            // 逐个展开结果列表
            for (WnObj o : list) {
                __do_extend(sys, list2, flt, q, o, ExtendDeeply);
            }

            // 指向新的结果
            list = list2;
        }

        // 返回一棵Tree, 根据指定层数递归展开
        if (params.has("tree")) {
            list = __do_tree(sys, params, sort, list);
        }

        // 返回祖先节点
        if (params.has("an")) {
            list = __do_ancestor(params, list);
        }

        // 执行更新
        if (null != u_map) {
            __do_update(sys, u_map, list);
        }

        // 执行 push 操作 ..
        boolean _is_push = params.has("push");
        if (_is_push) {
            __do_push(sys, params, list);
        }

        // 执行 pop 操作 ..
        boolean _is_pop = params.has("pop");
        if (_is_pop) {
            __do_pop(sys, params, list);
        }

        // 执行 set 操作
        boolean _is_set = params.has("set");
        if (_is_set) {
            __do_set(sys, params, list);
        }

        // 输出前，是否过滤所有的隐藏文件
        if (params.is("hide")) {
            List<WnObj> list2 = new ArrayList<WnObj>(list.size());
            for (WnObj o : list)
                if (!o.isHidden())
                    list2.add(o);
            list = list2;
        }

        // 最后执行输出
        if (((null == u_map && !_is_push && !_is_pop) || params.is("o")) && !params.is("Q")) {
            Cmds.output_objs(sys, params, wp, list, true);
        }

    }

    private List<WnObj> __do_ancestor(ZParams params, List<WnObj> list) {
        // 确保输出为列表
        params.setv("l", true);
        // 首先得到自己，然后读取自己的祖先节点
        if (!list.isEmpty()) {
            // 得到过滤条件
            NutMap anuntil = null;
            if (params.has("anuntil")) {
                anuntil = params.getMap("anuntil");
            }
            // 加载
            WnObj oSelf = list.get(0);
            LinkedList<WnObj> ans = new LinkedList<WnObj>();
            oSelf.loadParents(ans, false);
            list = new LinkedList<WnObj>();
            // 寻找到根节点
            WnObj oTop = null;
            if (ans.size() > 0) {
                ListIterator<WnObj> it = ans.listIterator(ans.size());
                WnObj oAn = it.previous();
                // 判断一下是否到根了
                while (true) {
                    // 木了
                    if (!it.hasPrevious()) {
                        oTop = oAn;
                        break;
                    }
                    // 可以被匹配
                    if (null != anuntil && anuntil.match(oAn)) {
                        oTop = oAn;
                        break;
                    }
                    // 那就是节点咯
                    list.add(0, oAn);
                    oAn = it.previous();
                }
            }
            // 根据条件判断一下是否增加自己
            String anMode = params.getString("an");

            // noroot 则表示返回: [节点1] [节点2] [自己]
            if ("noroot".equals(anMode)) {
                list.add(oSelf);
            }
              // nodes 则表示返回: [节点1] [节点2]
            else if ("nodes".equals(anMode)) {}
            // full 则表示返回: [根] [节点1] [节点2] [自己]
            else if ("full".equals(anMode)) {
                list.add(oSelf);
                list.add(0, oTop);
            }
            // 默认 noself 则表示返回: [根] [节点1] [节点2]
            else {
                list.add(0, oTop);
            }
        }
        return list;
    }

    private void __do_extend(WnSystem sys,
                             List<WnObj> list2,
                             NutMap flt,
                             WnQuery q,
                             WnObj o,
                             final boolean ExtendDeeply) {
        // 匹配的就展开
        if (o.isDIR() && flt.match(o)) {
            q.setv("pid", o.id());
            sys.io.each(q,
                        (int i, WnObj child, int len) -> {
                            // 设置父
                            child.setParent(o);

                            // 深层递归展开
                            if (ExtendDeeply) {
                                __do_extend(sys, list2, flt, q, child, ExtendDeeply);
                            }
                            // 仅仅展开一层
                            else {
                                child.setParent(o);
                                list2.add(child);
                            }
                        });
        }
        // 没匹配，加入到结果里
        else {
            list2.add(o);
        }
    }

    private List<WnObj> __do_tree(WnSystem sys, ZParams params, NutMap sort, List<WnObj> list) {
        List<WnObj> list2 = new LinkedList<WnObj>();

        String json = params.get("tree");
        NutMap flt = "true".equals(json) ? new NutMap() : Lang.map(json);

        WnQuery q = new WnQuery();
        NutMap by = Lang.map(params.get("treeBy", "{}"));
        q.setAll(by);

        if (null != sort)
            q.sort(sort);

        int treeDepth = params.getInt("treeDepth", 1);

        // 逐个展开结果列表
        for (WnObj o : list) {
            __do_tree_in_loop(sys, list2, flt, q, o, treeDepth, 0);
        }

        // 是否抹平 Tree
        if (params.is("treeFlat")) {
            List<WnObj> list3 = new LinkedList<>();
            for (WnObj o : list2) {
                __flat_tree_in_loop(o, list3);
            }
            list2 = list3;
        }

        // 搞定返回
        return list2;
    }

    @SuppressWarnings("unchecked")
    private void __flat_tree_in_loop(WnObj o, List<WnObj> output) {
        List<WnObj> children = (List<WnObj>) o.remove("children");
        output.add(o);
        if (null != children && children.size() > 0) {
            for (WnObj child : children) {
                __flat_tree_in_loop(child, output);
            }
        }
    }

    private void __do_tree_in_loop(WnSystem sys,
                                   List<WnObj> resultList,
                                   NutMap flt,
                                   WnQuery q,
                                   WnObj o,
                                   int wantDepth,
                                   int currDepth) {
        // 匹配的就展开
        if (o.isDIR() && flt.match(o) && (0 == wantDepth || wantDepth > currDepth)) {
            // 准备子节点
            List<WnObj> children = new ArrayList<WnObj>();
            o.setv("children", children);

            q.setv("pid", o.id());
            sys.io.each(q,
                        (int i, WnObj child, int len) -> {
                            // 设置父
                            child.setParent(o);
                            // 深层递归展开
                            __do_tree_in_loop(sys,
                                              children,
                                              flt,
                                              q,
                                              child,
                                              wantDepth,
                                              currDepth + 1);
                        });
        }
        // 加到结果集里
        resultList.add(o);
    }

    private void __do_groupCount(WnSystem sys,
                                 ZParams params,
                                 List<WnObj> list,
                                 String groupCount) {
        WnTmpl tmpl = WnTmpl.parse(groupCount);

        Map<String, Integer> re = new TreeMap<String, Integer>();

        // 开始归纳
        for (WnObj o : list) {
            String key = tmpl.render(o);
            Integer n = re.get(key);
            if (null == n) {
                re.put(key, 1);
            } else {
                re.put(key, n + 1);
            }
        }

        // 输出结果，到此为止
        JsonFormat fmt = Cmds.gen_json_format(params);
        sys.out.println(Json.toJson(re, fmt));
    }

    static abstract class PopEach implements Each<Object> {

        protected List<Object> vList;

        PopEach setVList(List<Object> vList) {
            this.vList = vList;
            return this;
        }

        static PopEach create(String str) {

            PopEach pe = PopEachByEmpty.TRY(str);
            if (null != pe)
                return pe;

            pe = PopEachByIndex.TRY(str);
            if (null != pe)
                return pe;

            pe = PopEachByNumber.TRY(str);
            if (null != pe)
                return pe;

            pe = PopEachByValue.TRY(str);
            if (null != pe)
                return pe;

            pe = PopEachBySet.TRY(str);
            if (null != pe)
                return pe;

            pe = PopEachByPattern.TRY(str);
            if (null != pe)
                return pe;

            throw Er.create("e.cmd.obj.pop.invalidOpt", str);
        }

    }

    // - "i:3" 表示 0 base下标，即第四个
    // - "i:-1" 表示最后一个
    // - "i:-2" 表示倒数第二个
    static class PopEachByIndex extends PopEach {

        private static final Pattern REGEX = Pattern.compile("^i:([-]?\\d+)$");

        static PopEach TRY(String str) {
            Matcher m = REGEX.matcher(str);
            if (m.find())
                return new PopEachByIndex(Integer.parseInt(m.group(1)));
            return null;
        }

        private int index;

        PopEachByIndex(int index) {
            this.index = index;
        }

        @Override
        public void invoke(int i, Object ele, int length) {
            // 下标从前面数
            if (index >= 0) {
                if (index != i) {
                    vList.add(ele);
                }
            }
            // 下标从后面数
            else {
                if (index + length != i) {
                    vList.add(ele);
                }
            }
        }
    }

    // - "n:3" 表示从后面弹出最多三个
    // - "n:-1" 表示从开始处弹出最多一个
    static class PopEachByNumber extends PopEach {

        private static final Pattern REGEX = Pattern.compile("^n:([-]?\\d+)$");

        static PopEach TRY(String str) {
            Matcher m = REGEX.matcher(str);
            if (m.find())
                return new PopEachByNumber(Integer.parseInt(m.group(1)));
            return null;
        }

        private int n;

        PopEachByNumber(int n) {
            this.n = n;
        }

        @Override
        public void invoke(int i, Object ele, int length) {
            // 从后面弹
            if (n >= 0) {
                if (i >= (length - n)) {
                    Lang.Break();
                } else {
                    vList.add(ele);
                }
            }
            // 从前面弹
            else {
                if (i >= (n * -1)) {
                    vList.add(ele);
                }
            }
        }
    }

    // - "v:xyz" 表示弹出内容为 'xyz' 的项目
    // - "!v:xyz" 表示弹出内容不为 'xyz' 的项目
    static class PopEachByValue extends PopEach {

        private static final Pattern REGEX = Pattern.compile("^(!?)v:(.+)$");

        static PopEach TRY(String str) {
            Matcher m = REGEX.matcher(str);
            if (m.find())
                return new PopEachByValue(m.group(2), !"!".equals(m.group(1)));
            return null;
        }

        private Object v;

        // !v -> false, v-> true
        private boolean match_for_remove;

        PopEachByValue(Object v, boolean match_for_remove) {
            this.v = v;
            this.match_for_remove = match_for_remove;
        }

        @Override
        public void invoke(int i, Object ele, int length) {
            if (Lang.equals(ele, v) ^ match_for_remove) {
                vList.add(ele);
            }
        }
    }

    // - "l:a,b" 表示弹出半角逗号分隔的列表里的值
    // - "!l:a,b" 表示弹出不在半角逗号分隔的列表里的值
    static class PopEachBySet extends PopEach {

        private static final Pattern REGEX = Pattern.compile("^(!?)l:(.+)$");

        static PopEach TRY(String str) {
            Matcher m = REGEX.matcher(str);
            if (m.find())
                return new PopEachBySet(m.group(2), !"!".equals(m.group(1)));
            return null;
        }

        private HashSet<String> set;

        // !v -> false, v-> true
        private boolean match_for_remove;

        PopEachBySet(String vs, boolean match_for_remove) {
            this.set = new HashSet<String>();
            String[] ss = Strings.splitIgnoreBlank(vs);
            for (String s : ss)
                this.set.add(s);
            this.match_for_remove = match_for_remove;
        }

        @Override
        public void invoke(int i, Object ele, int length) {
            if ((null != ele && set.contains(ele.toString())) ^ match_for_remove) {
                vList.add(ele);
            }
        }
    }

    // - "e:^a.*" 表示弹出被正则表达式匹配的项目
    // - "!e:^a.*" 表示弹出没有被正则表达式匹配的项目
    static class PopEachByPattern extends PopEach {

        private static final Pattern REGEX = Pattern.compile("^(!?)e:(.+)$");

        static PopEach TRY(String str) {
            Matcher m = REGEX.matcher(str);
            if (m.find())
                return new PopEachByPattern(m.group(2), !"!".equals(m.group(1)));
            return null;
        }

        private Pattern ptn;

        // !v -> false, v-> true
        private boolean match_for_remove;

        PopEachByPattern(String regex, boolean match_for_remove) {
            this.ptn = Pattern.compile(regex);
            this.match_for_remove = match_for_remove;
        }

        @Override
        public void invoke(int i, Object ele, int length) {
            if ((null != ele && ptn.matcher(ele.toString()).find()) ^ match_for_remove) {
                vList.add(ele);
            }
        }
    }

    // - "" 表示删除全部空数据项目
    static class PopEachByEmpty extends PopEach {

        static PopEach TRY(String str) {
            if (Strings.isEmpty(str))
                return new PopEachByEmpty();
            return null;
        }

        @Override
        public void invoke(int i, Object ele, int length) {
            if (null != ele && !Strings.isBlank(ele.toString())) {
                vList.add(ele);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void __do_set(WnSystem sys, ZParams params, List<WnObj> list) {
        // 得到要 set 的值
        String json = Cmds.getParamOrPipe(sys, params, "set", false);
        NutMap setMap = Lang.map(json);

        // 处理每个对象
        for (WnObj o : list) {

            // 对每个对象对应的 key 执行操作
            for (Map.Entry<String, Object> en : setMap.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();

                // val 必须为 Map
                if (!(val instanceof Map)) {
                    throw Er.create("e.cmd.obj.set.nomap", key + ":" + Json.toJson(val));
                }

                Map map = (Map) val;

                // 得到原对象的值
                Object valueInObj = o.get(key);

                // 原来的值也是 Map，那么合并
                if (null != valueInObj && (valueInObj instanceof Map)) {
                    Map mapInObj = (Map) valueInObj;
                    for (Object key2 : map.keySet()) {
                        Object val2 = map.get(key2);
                        // 移除
                        if (null == val2) {
                            mapInObj.remove(key2);
                        }
                        // 修改
                        else {
                            mapInObj.put(key2, val2);
                        }
                    }
                }
                // 否则替换
                else {
                    valueInObj = map;
                }

                // 更新到对象里
                o.setv(key, valueInObj);
            }

            // 更新的正则表达式
            String regex = "^(" + Lang.concat("|", setMap.keySet()) + ")$";

            // 执行更新
            sys.io.set(o, regex);
        }

    }

    private void __do_pop(WnSystem sys, ZParams params, List<WnObj> list) {
        // 得到要 pop 的值
        String json = Cmds.getParamOrPipe(sys, params, "pop", false);
        NutMap popMap = Lang.map(json);

        // 处理每个对象
        for (WnObj o : list) {

            // 对每个对象对应的 key 执行操作
            for (Map.Entry<String, Object> en : popMap.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();

                // 准备一个空列表准备来拼合值
                List<Object> vList = new LinkedList<Object>();

                // null 表示清除全部数据，那就啥也别干了
                // 否则具体看看 val 是几个意思
                if (null != val) {
                    // 准备回调函数
                    Each<Object> callback = PopEach.create(val.toString()).setVList(vList);

                    // 搞一遍
                    Lang.each(o.get(key), callback);
                }

                // 更新到对象里
                o.setv(key, vList.isEmpty() ? null : vList);
            }

            // 更新的正则表达式
            String regex = "^(" + Lang.concat("|", popMap.keySet()) + ")$";

            // 执行更新
            sys.io.set(o, regex);
        }
    }

    private void __do_push(WnSystem sys, ZParams params, List<WnObj> list) {
        // 得到要 push 的值
        String json = Cmds.getParamOrPipe(sys, params, "push", false);
        NutMap pushMap = Lang.map(json);

        // 是否唯一
        boolean pushUniq = params.is("push_uniq", true);

        // 处理每个对象
        for (WnObj o : list) {

            // 对每个对象对应的 key 执行操作
            for (Map.Entry<String, Object> en : pushMap.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();

                // 准备一个空列表准备来拼合值
                List<Object> vList = new LinkedList<Object>();

                // 首先搞一下原来的值
                HashSet<Object> memo = pushUniq ? new HashSet<Object>() : null;

                // 准备回调函数
                Each<Object> callback = new Each<Object>() {
                    @Override
                    public void invoke(int index, Object v, int len) {
                        // 唯一值的过滤
                        if (null != memo) {
                            if (memo.contains(v))
                                return;
                            memo.add(v);
                        }
                        // 添加
                        vList.add(v);
                    }
                };

                // 搞一遍旧值
                Lang.each(o.get(key), callback);

                // 搞一遍新值
                // Lang.each(val, callback);
                vList.add(val);

                // 更新到对象里
                o.setv(key, vList);
            }

            // 更新的正则表达式
            String regex = "^(" + Lang.concat("|", pushMap.keySet()) + ")$";

            // 执行更新
            sys.io.set(o, regex);
        }
    }

    private WnObj __do_new(WnSystem sys, ZParams params, NutMap meta) {
        boolean ifNoExists = params.is("IfNoExists");

        // 得到父对象
        WnObj oP = null;
        // 根据路径
        if (params.vals.length > 0) {
            oP = Wn.checkObj(sys, params.vals[0]);
        }

        WnObj currentObj = sys.getCurrentObj();

        return __do_new_for_meta(sys, oP, currentObj, meta, ifNoExists);
    }

    private List<WnObj> __do_new_list(WnSystem sys, ZParams params, List<NutMap> metaList) {
        boolean ifNoExists = params.is("IfNoExists");

        // 得到父对象
        WnObj oP = null;
        // 根据路径
        if (params.vals.length > 0) {
            oP = Wn.checkObj(sys, params.vals[0]);
        }

        WnObj currentObj = sys.getCurrentObj();

        // 准备返回
        List<WnObj> list = new ArrayList<>(metaList.size());
        for (NutMap meta : metaList) {
            WnObj o = __do_new_for_meta(sys, oP, currentObj, meta, ifNoExists);
            list.add(o);
            if (!meta.isEmpty()) {
                sys.io.appendMeta(o, meta);
            }
        }

        // 返回结果
        return list;
    }

    private WnObj __do_new_for_meta(WnSystem sys,
                                    WnObj oP,
                                    WnObj currentObj,
                                    NutMap meta,
                                    boolean ifNoExists) {
        // 根据自身决定 pid
        if (null == oP) {
            String pid = meta.getString("pid");
            if (null != pid) {
                oP = sys.io.checkById(pid);
            }
            // 采用当前路径
            else {
                oP = currentObj;
            }
        }

        // 确定对象类型
        WnRace race = meta.getAs("race", WnRace.class, WnRace.FILE);
        // 创建对象
        WnObj o;
        if (ifNoExists) {
            o = sys.io.createIfNoExists(oP, meta.getString("nm", "${id}"), race);
        } else {
            o = sys.io.create(oP, meta.getString("nm", "${id}"), race);
        }

        // 如果定义了类型,自动获取 mime
        if (o.isFILE() && meta.has("tp") && !meta.has("mime"))
            meta.setOrRemove("mime", sys.io.mimes().getMime(meta.getString("tp")));

        // 准备更新的元数据
        meta.remove("id");
        meta.remove("pid");
        meta.remove("race");
        meta.remove("nm");
        meta.remove("ct");
        meta.remove("lm");
        // TODO 临时remove掉，因为wn.io.eval_dn冲突
        meta.remove("d1");
        meta.remove("d0");
        return o;
    }

    private List<WnObj> __query_by_path(WnSystem sys, String[] args, ZParams params, NutMap sort) {
        List<WnObj> list = new LinkedList<WnObj>();

        // 模式
        int mode = 0;
        if (params.has("noexists")) {
            if ("null".equals(params.get("noexists"))) {
                mode |= Wn.Cmd.NOEXISTS_NULL;
            } else {
                mode |= Wn.Cmd.NOEXISTS_IGNORE;
            }
        }

        // 根据模式计算候选对象
        Cmds.evalCandidateObjs(sys, params.vals, list, mode);

        // 不是强制列表模式的时候，检查是否候选对象列表为空
        if (!params.is("l")) {
            Cmds.assertCandidateObjsNoEmpty(params.vals, list);
        }

        // 排序
        if (null != sort) {
            __do_sort(sort, list);
        }

        return list;
    }

    private List<WnObj> __query_by_match(WnSystem sys,
                                         ZParams params,
                                         WnObj oP,
                                         WnPager wp,
                                         NutMap sort) {
        String json = Cmds.getParamOrPipe(sys, params, "match", true);
        WnQuery q = new WnQuery();
        if (!Strings.isBlank(json)) {
            // 条件是"或"
            if (Strings.isQuoteBy(json, '[', ']')) {
                List<NutMap> ors = Json.fromJsonAsList(NutMap.class, json);
                q.addAll(ors);
            }
            // 条件是"与"
            else {
                q.add(Lang.map(json));
            }
        }

        // 如果指定了父对象 ...
        if (null != oP)
            q.setv("pid", oP.id());

        // 如果指明了本域查询
        if (params.is("mine")) {
            q.setv("d0", "home").setv("d1", sys.getMyGroup());
        }

        // 添加更多条件
        // if (!"root".equals(sys.se.group()))
        // q.setv("d1", sys.se.group());

        // 设置分页信息
        wp.setupQuery(sys, q);

        // 设置排序
        if (null != sort)
            q.sort(sort);

        return sys.io.query(q);
    }

    private void __do_update(WnSystem sys, NutMap u_map, List<WnObj> list) {
        u_map.remove("id");

        // TODO 这里是不是要检查一下重名啊!

        if (!u_map.isEmpty()) {
            // 将日期的字符串，搞一下
            for (Map.Entry<String, Object> en : u_map.entrySet()) {
                Object v = en.getValue();
                if (null != v && v instanceof String) {
                    String s = v.toString();
                    Object v2 = Wn.fmt_str_macro(s);
                    en.setValue(v2);
                }
            }

            // 对每个对象执行更新
            for (WnObj o : list) {
                sys.io.appendMeta(o, u_map);
            }
        }
    }

    private void __do_sort(NutMap sort, List<WnObj> list) {
        final NutMap smap = sort;
        Collections.sort(list, new Comparator<WnObj>() {
            @Override
            public int compare(WnObj o1, WnObj o2) {
                for (String key : smap.keySet()) {
                    Object v1 = o1.get(key);
                    Object v2 = o2.get(key);

                    if (null == v1 && null == v2)
                        continue;

                    // 排序因子
                    int E = smap.getInt(key) > 0 ? 1 : -1;

                    // 有一个为 null
                    if (null == v1)
                        return E;

                    if (null == v2)
                        return E * -1;

                    // 数字字段
                    if (v1 instanceof Number && v2 instanceof Number) {
                        double re = ((Number) v1).doubleValue() - ((Number) v2).doubleValue();

                        if (re == 0.0) {
                            continue;
                        }

                        if (re > 0.0)
                            return E;
                        else
                            return E * -1;
                    }
                    // 其他的当做字符串
                    String s1 = v1.toString();
                    String s2 = v2.toString();
                    return s1.compareTo(s2) * E;

                }
                return 0;
            }
        });
    }

}
