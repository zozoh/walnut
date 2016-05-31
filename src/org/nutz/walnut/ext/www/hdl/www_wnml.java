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
import org.nutz.http.Http;
import org.nutz.json.Json;
import org.nutz.json.JsonException;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.WWWContext;
import org.nutz.walnut.ext.www.WWWHdl;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.WebException;

public class www_wnml implements WWWHdl {

    @Override
    public void invoke(WnSystem sys, WWWContext wwc) {
        // 解析输入的文件
        Document doc = Jsoup.parse(wwc.input);

        // 首先处理静态引入
        Elements eles = doc.select("include");
        for (Element ele : eles) {
            __do_include(sys, wwc, ele, wwc.context);
        }
        
        //System.out.println(doc);

        // 然后处理数据源
        // eles = doc.select("script.wn-datasource");
        // for (Element ele : eles) {
        // __do_datasource(sys, ele, wwc.context);
        // }

        // 处理所有的节点的属性和文本的占位符
        try {
            __do_node(sys, doc, wwc.context);
            sys.out.println(doc.toString());
        }
        // 看看是否会有 redirect 的错误
        catch (WebException e) {
            if (e.isKey("redirect.www.wnml")) {
                Element eRe = (Element) e.getReason();
                int code = Integer.parseInt(Strings.sBlank(eRe.attr("code"), "302"));
                String text = Strings.sBlank(eRe.attr("text"), Http.getStatusText(code));
                String url = Strings.sBlank(eRe.text(), "/");
                sys.out.printlnf("HTTP/1.1 %d %s", code, text);
                sys.out.printlnf("Location: %s", url);
                sys.out.println();
            }
            // 其他的错误，抛出去吧
            else {
                throw e;
            }
        }

    }

    private void __do_redirect(WnSystem sys, Element ele, NutMap c) {
        __do_element_text(ele, c);
        throw Er.create("redirect.www.wnml", ele);
    }

    private void __do_node(WnSystem sys, Node nd, NutMap c) {
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
            String tagName = ele.tagName();

            // 处理重定向，仅仅抛出错误，中断后续处理例程
            if ("redirect".equals(tagName)) {
                __do_redirect(sys, ele, c);
            }
            // 处理数据源
            else if ("script".equals(tagName) && ele.hasClass("wn-datasource")) {
                __do_datasource(sys, ele, c);
            }
            // <if>
            else if ("if".equals(tagName)) {
                __do_if(sys, ele, c);
            }
            // <each>
            else if ("each".equals(tagName)) {
                __do_each(sys, ele, c);
            }
            // <choose>
            else if ("choose".equals(tagName)) {
                __do_choose(sys, ele, c);
            }
            // 普通的元素
            else {
                // 属性
                __do_node_attr(ele, c);

                // 子
                Node[] children = this.__get_children_array(ele);
                for (Node child : children) {
                    __do_node(sys, child, c);
                }
            }
        }
        // 不显示出来的元素
        else if (nd instanceof DataNode) {
            __do_node_attr(nd, c);
            __do_data_node_text((DataNode) nd, c);
        }

    }

    private void __do_data_node_text(DataNode dnd, NutMap c) {
        String txt = dnd.getWholeData();
        String str = Tmpl.exec(txt, c);
        dnd.setWholeData(str);
    }

    private void __do_element_text(Element ele, NutMap c) {
        String txt = ele.text();
        String str = Tmpl.exec(txt, c);
        ele.text(str);
    }

    private void __do_each(final WnSystem sys, final Element ele, NutMap c) {
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
                        __do_node(sys, newNode, loopC);
                    }
                }
            });
        }

        // 最后移除模板
        ele.remove();
    }

    private boolean __do_if(WnSystem sys, Element ele, NutMap c) {
        String test = ele.attr("test");
        Object re = El.eval(Lang.context(c), test);
        boolean b = null == re ? false : Castors.me().castTo(re, Boolean.class);
        // 输出
        if (b) {
            // 处理自己所有的子
            for (Node nd : ele.childNodes()) {
                __do_node(sys, nd, c);
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

    private void __do_choose(WnSystem sys, Element ele, NutMap c) {
        boolean matched = false;
        // 依次判断
        for (Element child : ele.children()) {
            // 已经匹配上了，就删除
            if (matched) {
                child.remove();
                continue;
            }

            String tagName = child.tagName();

            // 到了默认值
            if ("otherwise".equals(tagName)) {
                for (Node nd : child.childNodes())
                    __do_node(sys, nd, c);
                child.unwrap();
                matched = true;
            }
            // 如果是 when
            else if ("when".equals(tagName)) {
                matched = __do_if(sys, child, c);
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

        // 如果是绝对路径
        if (path.startsWith("/")) {
            path = Wn.appendPath(c.getString("SITE_HOME"), path);
        }

        // 读取目标
        WnObj o = sys.io.check(wwc.oCurrent, path);
        String str = sys.io.readText(o);
        Document doc = Jsoup.parse(str);

        // 复制Head所有的内容到自己的 head
        Node[] children = __get_children_array(doc.head());
        Element eHead = ele.ownerDocument().head();
        for (Node nd : children) {
            eHead.appendChild(nd);
        }

        // 复制Body所有的内容到自己之后
        children = __get_children_array(doc.body());
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

        String cmdTmpl = Strings.trim(ele.data());
        String cmdText = Tmpl.exec(cmdTmpl, c, false);

        String str = sys.exec2(cmdText);

        // JSON
        if ("json".equalsIgnoreCase(type)) {
            Object oJson;
            try {
                oJson = Json.fromJson(str);
            }
            // 如果失败了，那么这个命令返回的不是合法的 JSON
            // 那么会看看 "dftobj" 属性有木有声明，如果没有，则相当于 null
            catch (JsonException e) {
                String dftobj = ele.attr("dftobj");
                if (!Strings.isBlank(dftobj)) {
                    oJson = Json.fromJson(dftobj);
                } else {
                    oJson = null;
                }
            }
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
