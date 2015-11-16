package org.nutz.walnut.ext.www.hdl;

import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.nutz.castor.Castors;
import org.nutz.el.El;
import org.nutz.json.Json;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.WWWContext;
import org.nutz.walnut.ext.www.WWWHdl;
import org.nutz.walnut.impl.box.WnSystem;

public class www_wnml implements WWWHdl {

    @Override
    public void invoke(WnSystem sys, WWWContext wwc) {
        // 创建全局上下文
        NutMap c = new NutMap();

        // 解析输入的文件
        Document doc = Jsoup.parse(wwc.input);

        // 首先处理数据源和静态引入
        Elements eles = doc.select(".wn-datasource, include");
        for (Element ele : eles) {
            // // 处理静态引入
            if ("include".equals(ele.tagName())) {
                __do_include(sys, wwc, ele, c);
            }
            // 数据源
            else {
                __do_datasource(sys, ele, c);
            }
        }

        // 填充上默认全局上下文变量
        c.put("grp", sys.se.group());
        c.put("fnm", wwc.fnm);
        c.put("rs", "/gu/rs");

        // 处理所有的节点的属性和文本的占位符
        __do_node(doc, c);

        // 最后输出
        sys.out.println(doc.toString());

    }

    private void __do_node(Node nd, NutMap c) {
        // 文本
        if (nd instanceof TextNode) {
            TextNode tnd = (TextNode) nd;
            String txt = tnd.text();
            String str = Tmpl.exec(txt, c);
            tnd.text(str);
        }
        // 元素
        else if (nd instanceof Element) {
            Element ele = (Element) nd;

            // <if>
            if ("if".equals(ele.tagName())) {
                __do_if(ele, c);
            }
            // <each>
            else if ("each".equals(ele.tagName())) {
                __do_each(ele, c);
            }
            // <choose>
            else if ("choose".equals(ele.tagName())) {
                __do_choose(ele, c);
            }
            // 普通的元素
            else {
                // 属性
                __do_node_attr(ele, c);

                // 子
                Node[] children = this.__get_children_array(ele);
                for (Node child : children) {
                    __do_node(child, c);
                }
            }
        }
        // 数据
        else if (nd instanceof DataNode) {
            // 属性
            __do_node_attr(nd, c);

            // 内容
            DataNode dnd = (DataNode) nd;
            String txt = dnd.getWholeData();
            String str = Tmpl.exec(txt, c);
            dnd.setWholeData(str);
        }

    }

    private void __do_each(final Element ele, NutMap c) {
        final String varName = Strings.sBlank(ele.attr("var"), "_obj");
        String itemsKey = ele.attr("items");

        // 要迭代的对象
        Object items = Mapl.cell(c, itemsKey);

        // 如果有内容，那么就开始迭代
        if (null != items) {
            final NutMap loopC = new NutMap().attach(c);
            // 每个子元素都要迭代
            final Node[] children = this.__get_children_array(ele);
            Lang.each(items, new Each<Object>() {
                public void invoke(int index, Object val, int length) {
                    loopC.put(varName, val);
                    for (Node child : children) {
                        Node newNode = child.clone();
                        ele.after(newNode);
                        __do_node(newNode, loopC);
                    }
                }
            });
        }

        // 最后移除模板
        ele.remove();
    }

    private boolean __do_if(Element ele, NutMap c) {
        String test = ele.attr("test");
        Object re = El.eval(Lang.context(c), test);
        boolean b = null == re ? false : Castors.me().castTo(re, Boolean.class);
        // 输出
        if (b) {
            // 处理自己所有的子
            for (Node nd : ele.childNodes()) {
                __do_node(nd, c);
            }

            // 删除自身
            ele.unwrap();
        }
        // 不输出
        else {
            ele.remove();
        }
        // 返回结果
        return b;
    }

    private void __do_choose(Element ele, NutMap c) {
        boolean matched = false;
        // 依次判断
        for (Element child : ele.children()) {
            // 已经匹配上了，就删除
            if (matched) {
                child.remove();
            }

            String tagName = ele.tagName();

            // 到了默认值
            if ("otherwise".equals(tagName)) {
                for (Node nd : child.childNodes())
                    __do_node(nd, c);
                child.unwrap();
                matched = true;
            }
            // 如果是 when
            else if ("when".equals(tagName)) {
                matched = __do_if(child, c);
            }
            // 其他的移除
            else {
                child.remove();
            }

        }

        // 最后去掉 choose
        ele.unwrap();
    }

    private void __do_node_attr(Node nd, NutMap c) {
        for (Attribute attr : nd.attributes()) {
            String val = attr.getValue();
            String str = Tmpl.exec(val, c);
            attr.setValue(str);
        }
    }

    private void __do_include(WnSystem sys, WWWContext wwc, Element ele, NutMap c) {
        String path = Tmpl.exec(ele.attr("path"), c);

        // 读取目标
        WnObj o = sys.io.check(wwc.oCurrent, path);
        String str = sys.io.readText(o);
        Document doc = Jsoup.parse(str);

        // 复制到自己之后
        Node[] children = __get_children_array(doc.body());
        for (Node nd : children) {
            ele.after(nd);
        }

        // 移除自身
        ele.remove();
    }

    private Node[] __get_children_array(Element ele) {
        List<Node> children = ele.childNodes();
        Node[] list = new Node[children.size()];
        children.toArray(list);
        return list;
    }

    @SuppressWarnings("unchecked")
    private void __do_datasource(WnSystem sys, Element ele, NutMap c) {
        String name = Strings.trim(ele.attr("name"));
        String type = Strings.trim(Strings.sBlank(ele.attr("type"), "json"));

        // 字符串必须要有 name，否则加不到上下文里呀
        if (Strings.isBlank(name) && !"json".equalsIgnoreCase(type)) {
            return;
        }

        String cmdText = Strings.trim(ele.data());

        String str = sys.exec2(cmdText);

        // JSON
        if ("json".equalsIgnoreCase(type)) {
            Object oJson = Json.fromJson(str);
            // 没有 name，并且是 Map
            if (Strings.isBlank(name) && oJson instanceof Map<?, ?>) {
                c.putAll((Map<String, Object>) oJson);
            }
            // 否则添加一项
            else if (!Strings.isBlank(name)) {
                c.put(name, oJson);
            }
        }
        // 字符串
        else {
            c.put(name, str);
        }

        // 执行完毕，数据源元素需要删除
        ele.remove();
    }

}
