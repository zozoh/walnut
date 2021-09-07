package org.nutz.walnut.ext.media.ooml.tmpl;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapText;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;

/**
 * 针对一个占位符，跨越多个 w:r 节点的问题，用这个逻辑封装。适用于:
 * <ul>
 * <li><code>NORMAL</code> 普通展位符
 * <li><code>CHECKBOX</code> 选择框
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class OomlWRunList {

    private List<CheapElement> runNodes;

    private List<OomlWPlaceholder> placeholders;

    public OomlWRunList() {
        this.runNodes = new LinkedList<>();
        this.placeholders = new LinkedList<>();
    }

    public int load(CheapElement wP) {
        this.runNodes.clear();
        List<CheapElement> nodes = wP.getChildElements(el -> el.isTagName("w:r"));
        this.runNodes.addAll(nodes);
        return this.runNodes.size();
    }

    public int prepare() {
        StringBuilder sb = new StringBuilder();
        int count = runNodes.size();
        int[] offsets = new int[count + 1];
        //
        // 循环查找 <w:r> ， 将内容拼合为字符串，并设置下标
        //
        int index = 0;
        int offset = 0;
        for (CheapElement wR : runNodes) {
            List<CheapElement> wTs = wR.getChildElements(el -> el.isTagName("w:t"));
            if (null == wTs || wTs.isEmpty()) {
                continue;
            }
            CheapElement wT = wTs.get(0);
            String s = wT.getText();
            sb.append(s);
            offsets[index] = offset;

            // 下移
            index++;
            offset += s.length();
        }
        offsets[index] = offset;
        //
        // 准备正则表达式，依次解析
        //
        String input = sb.toString();
        /**
         * <pre>
         0: "${变量名<checkbox>==true?false:it}"
         1: "变量名"
         2: "<checkbox>"
         3: "checkbox"
         4: "==true"
         5: "true"
         6: "?false"
         7: "false"
         8: ":it"
         9: "it"
         * </pre>
         */
        Pattern P = Pattern.compile("[$][{]([^}<?=:]+)(<([^>]+)>)?(==([^:?}]+))?([?]([^:}]*))?(:([^}]+))?[}]");
        Matcher m = P.matcher(input);
        offset = 0;
        while (m.find()) {
            // 找到占位信息
            int p0 = m.start();
            int p1 = m.end();

            OomlWPhMark rBegin = new OomlWPhMark().update(offsets, p0);
            OomlWPhMark rEnd = new OomlWPhMark().update(offsets, p1 - 1);

            // 得到占位符信息
            String varName = m.group(1);
            String typeName = m.group(3);
            String testVal = m.group(5);
            String dftVal = m.group(7);
            String itemName = m.group(9);

            // 第一个标记对应的项目
            CheapElement wR = this.getRunNode(rBegin.index);
            CheapElement rPr = wR.getFirstChildElement("w:rPr");

            // 创建占位符
            OomlWPlaceholder wph = new OomlWPlaceholder();
            wph.setName(varName);
            wph.setType(typeName);
            wph.setBoolTest(testVal);
            wph.setDefaultValue(dftVal);
            wph.setItemName(itemName);
            wph.setRunProperty(rPr);
            wph.setRunBegin(rBegin);
            wph.setRunEnd(rEnd);

            // 记入
            placeholders.add(wph);
        }

        return placeholders.size();
    }

    public void explain(NutBean vars) {
        // 循环处理占位符
        for (OomlWPlaceholder ph : this.placeholders) {
            // 普通占位符
            if (ph.isNormal()) {
                this.explainNormal(ph, vars);
            }
            // 选择框占位符
            else if (ph.isCheckbox()) {
                this.explainCheckbox(ph, vars);
            }
            // 其他的不支持
        }
    }

    public boolean splitWrTfromPos(CheapElement wR, int pos) {
        CheapElement wT = wR.getFirstChildElement("w:t");
        CheapText wTnode = (CheapText) wT.getFirstChild(node -> node.isText());
        String str = wTnode.getText();
        if (pos >= str.length()) {
            return true;
        }
        String s2 = str.substring(pos);
        wTnode.setText(s2);
        return false;
    }

    public CheapText splitWrTtoPos(CheapElement wR, int pos) {
        CheapElement wT = wR.getFirstChildElement("w:t");
        CheapText wTnode = (CheapText) wT.getFirstChild(node -> node.isText());
        String str = wTnode.getText();
        String s2 = str.substring(0, pos);
        wTnode.setText(s2);
        return wTnode;
    }

    public void explainNormal(OomlWPlaceholder ph, NutBean vars) {
        // 获取变量值
        String val = vars.getString(ph.getName(), ph.getDefaultValue());
        val = Ws.sBlank(val, "-Empty-");

        // 得到开始与结束的标记 <w:r>
        OomlWPhMark rBegin = ph.getRunBegin();
        OomlWPhMark rEnd = ph.getRunEnd();
        CheapElement wrBegin = this.getRunNode(rBegin.index);
        CheapElement wrEnd = this.getRunNode(rEnd.index);

        // 如果根本就是一个节点
        if (wrBegin == wrEnd) {
            CheapElement wT = wrBegin.getFirstChildElement("w:t");
            CheapText wTnode = (CheapText) wT.getFirstChild(node -> node.isText());
            String str = wTnode.getText();
            StringBuilder sb = new StringBuilder();
            sb.append(str.substring(0, rBegin.offset));
            sb.append(val);
            sb.append(str.substring(rEnd.offset + 1));
            String s2 = sb.toString();
            wTnode.setText(s2);
        }
        // 那么就要进行一下稍微复杂点的操作了
        else {
            // 将开始标记所在的 <w:r>，切分前一半，并将文本节点 <w:t> 后面追加上变量值
            CheapText wTbegin = this.splitWrTtoPos(wrBegin, rBegin.offset);
            wTbegin.appendText(val);

            // 删除开始标记所在元素至结束标记中间的节点
            int n = this.runNodes.size();
            if (n > 2) {
                CheapElement[] rNodes = new CheapElement[n];
                this.runNodes.toArray(rNodes);
                int lastI = rEnd.index;
                for (int i = rBegin.index + 1; i < lastI; i++) {
                    CheapElement node = rNodes[i];
                    node.remove();
                }
            }

            // 将结束占位符，切分后一半
            // 如果都空了，那么就整个删掉
            if (this.splitWrTfromPos(wrEnd, rEnd.offset + 1)) {
                wrEnd.remove();
            }
        }
    }

    public void explainCheckbox(OomlWPlaceholder ph, NutBean vars) {
        // 获取变量值
        throw Wlang.noImplement();
    }

    public List<CheapElement> getRunNodes() {
        return runNodes;
    }

    public void setRunNodes(List<CheapElement> runNodes) {
        this.runNodes = runNodes;
    }

    public CheapElement getRunNode(int index) {
        return this.runNodes.get(index);
    }

    public OomlWPlaceholder getPlaceholder(int index) {
        return this.placeholders.get(index);
    }

    public List<OomlWPlaceholder> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(List<OomlWPlaceholder> placeholders) {
        this.placeholders = placeholders;
    }

}
