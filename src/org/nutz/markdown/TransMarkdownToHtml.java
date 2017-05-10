package org.nutz.markdown;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;

public class TransMarkdownToHtml {

    private String html;

    private int index;

    private List<MdBlock> blocks;

    public TransMarkdownToHtml() {
        this.html = "";
        this.index = 0;
        this.blocks = new ArrayList<>(50);
    }

    // 增加一个帮助函数
    private MdBlock tryPush(MdBlock B) {
        if (null != B.type) {
            blocks.add(B);
            return new MdBlock();
        }
        return B;
    }

    // 定义内容输出函数
    private String __B_to_html(MdBlock B) {
        String html = "";
        boolean isFirstLine = true;
        for (String line : B.content) {
            if (isFirstLine) {
                isFirstLine = false;
            } else {
                html += "<br>\n";
            }
            html += __line_to_html(line);
        }
        return html;
    }

    private String __line_to_html(String str) {
        String reg = "(\\*(.+)\\*)"
                     + "|(\\*\\*(.+)\\*\\*)"
                     + "|(__(.+)__)"
                     + "|(~~(.+)~~)"
                     + "|(`(.+)`)"
                     + "|(!\\[(.*)\\]\\((.+)\\))"
                     + "|(\\[(.*)\\]\\((.+)\\))"
                     + "|(https?:\\/\\/[^ ]+)";
        Pattern REG = Pattern.compile(reg);
        Matcher m = REG.matcher(str);
        int pos = 0;
        String html = "";
        while (m.find()) {
            // console.log(m)
            if (pos < m.start()) {
                html += str.substring(pos, m.start());
            }
            // EM: *xxx*
            if (null != m.group(1)) {
                html += "<em>" + m.group(2) + "</em>";
            }
            // B: **xxx**
            else if (null != m.group(3)) {
                html += "<b>" + m.group(4) + "</b>";
            }
            // B: __xxx__
            else if (null != m.group(5)) {
                html += "<b>" + m.group(6) + "</b>";
            }
            // DEL: ~~xxx~~
            else if (null != m.group(7)) {
                html += "<del>" + m.group(8) + "</del>";
            }
            // CODE: `xxx`
            else if (null != m.group(9)) {
                html += "<code>" + m.group(10) + "</code>";
            }
            // IMG: ![](xxxx)
            else if (null != m.group(11)) {
                html += "<img alt=\"" + m.group(12) + "\" src=\"" + m.group(13) + "\">";
            }
            // A: [](xxxx)
            else if (null != m.group(14)) {
                html += "<a href=\""
                        + m.group(16)
                        + "\">"
                        + Strings.sBlank(m.group(15), m.group(16))
                        + "</a>";
            }
            // A: http://xxxx
            else if (null != m.group(17)) {
                html += "<a href=\"" + m.group(17) + "\">" + m.group(17) + "</a>";
            }

            // 唯一下标
            pos = m.end();
        }
        if (pos < str.length()) {
            html += str.substring(pos);
        }
        return html;
    }

    private void __B_to_blockquote(MdBlock B) {
        this.html += "\n<blockquote>";
        this.html += __B_to_html(B);
        // 循环查找后续的嵌套块
        for (this.index++; this.index < this.blocks.size(); this.index++) {
            MdBlock B2 = this.blocks.get(this.index);
            if ("quote" == B2.type && B2.level > B.level) {
                __B_to_blockquote(B2);
            } else {
                break;
            }
        }
        this.html += "\n</blockquote>";
        this.index--;
    }

    private void __B_to_list(MdBlock B) {
        this.html += "\n<" + B.type + ">";
        this.html += "\n<li>" + __B_to_html(B);
        // 循环查找后续的列表项，或者是嵌套
        for (this.index++; this.index < this.blocks.size(); this.index++) {
            MdBlock B2 = this.blocks.get(this.index);
            // 继续增加
            if (B.type == B2.type && B2.level == B.level) {
                this.html += "</li>\n<li>" + __B_to_html(B2);
            }
            // 嵌套
            else if (B2.level > B.level && B2.isType("^(OL|UL)$")) {
                __B_to_list(B2);
            }
            // 不属于本列表，退出吧
            else {
                break;
            }
        }
        this.html += "</li>";
        this.html += "\n</" + B.type + ">";
        this.index--;
    };

    public String trans(String str) {
        /*
         * 首先将文本拆分成段落： { type : "H|P|code|OL|UL|hr|Th|Tr|quote|empty", indent :
         * 1, content:["line1","line2"], codeType : null, cellAligns : ["left",
         * "center", "right"] }
         */
        blocks.clear();
        String[] lines = str.split("\r?\n");

        // 准备第一段
        MdBlock B = new MdBlock();
        boolean lastLineIsBlankLink = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trim = Strings.trim(line);
            int indent = Strings.countStrHeadIndent(line, 4);

            // 来吧，判断类型
            // 空段落
            if (Strings.isEmpty(trim)) {
                // 之前如果是 code，那么增加进去
                if (B.isType("^(code|UL|OL)$")) {
                    B.content.add("");
                }
                // 否则如果有段落，就结束它
                else {
                    B = this.tryPush(B);
                }
            }
            // 标题: H
            else if (line.matches("^#+ .+$")) {
                B = this.tryPush(B);
                B.type = "H";
                B.level = Strings.countStrHeadChar(line, '#');
                B.content.add(Strings.trim(line.substring(B.level)));
            }
            // 代码: code
            else if (line.matches("^```.*$")) {
                B = this.tryPush(B);
                B.type = "code";
                B.codeType = Strings.sBlank(Strings.trim(trim.substring(3)), null);
                for (i++; i < lines.length; i++) {
                    line = lines[i];
                    if (line.matches("^```.*$")) {
                        break;
                    }
                    B.content.add(line);
                }
                B = this.tryPush(B);
            }
            // 水平线: hr
            else if (line.matches("^ *[=-]{3,} *$")) {
                B = this.tryPush(B);
                B.type = "hr";
                B = this.tryPush(B);
            }
            // 表格分隔符: T
            else if ("P" == B.type
                     && B.content.size() == 1
                     && B.content.get(0).indexOf("|") > 0
                     && line.matches("^[ |:-]{6,}$")) {
                // 修改之前段落的属性
                B.type = "Th";
                B.setContent(Strings.splitIgnoreBlank(B.content.get(0), "[|]"));

                // 解析自己，分析单元格的 align
                B.cellAligns = Strings.splitIgnoreBlank(trim, "[|]");
                for (int x = 0; x < B.cellAligns.length; x++) {
                    String align = B.cellAligns[x].replaceAll("[ ]+", "");
                    Matcher m = Pattern.compile("^(:)?([-]+)(:)?$").matcher(align);
                    if (m.find()) {
                        boolean qL = !Strings.isBlank(m.group(1));
                        boolean qR = !Strings.isBlank(m.group(3));
                        if (qL && qR) {
                            B.cellAligns[x] = "center";
                        } else {
                            B.cellAligns[x] = qR ? "right" : "left";
                        }
                    }
                }

                // 推入
                B = this.tryPush(B);

                // 标识后续类型为 Tr
                B.type = "Tr";
            }
            // 有序列表: OL
            else if ((!B.hasType() || B.isType("^(OL|UL)$")) && trim.matches("^[0-9a-z][.].+$")) {
                B = this.tryPush(B);
                B.type = "OL";
                B.level = indent;
                B.content.add(Strings.trim(trim.substring(trim.indexOf('.') + 1)));
            }
            // 无序列表: UL
            else if ((!B.hasType() || B.isType("^(OL|UL)$")) && trim.matches("^[*+-][ ].+$")) {
                B = this.tryPush(B);
                B.type = "UL";
                B.level = indent;
                B.content.add(Strings.trim(trim.substring(1)));
            }
            // 缩进表示的代码
            else if (indent > 0) {
                // 只有空段落，才表示开始 code
                if (!B.hasType()) {
                    B.type = "code";
                    B.content.add(Strings.shiftIndent(line, 1, 4));
                }
                // 否则就要加入进去
                else {
                    B.content.add(trim);
                }
            }
            // 引用: quote
            else if (trim.startsWith(">")) {
                B = this.tryPush(B);
                B.type = "quote";
                B.level = Strings.countStrHeadChar(trim, '>');
                B.content.add(Strings.trim(trim.substring(B.level)));
            }
            // 普通段落融合到之前的块
            else if (B.isType("^(OL|UL|quote|P)$") && (!lastLineIsBlankLink || indent > B.level)) {
                B.content.add(trim);
            }
            // 将自己作为表格行
            else if ("Tr" == B.type) {
                B.setContent(Strings.splitIgnoreBlank(trim, "[|]"));
                B = this.tryPush(B);
                B.type = "Tr";
            }
            // 默认是普通段落 : P
            else {
                B = this.tryPush(B);
                B.type = "P";
                B.content.add(trim);
            }
            // 记录上一行
            lastLineIsBlankLink = Strings.isEmpty(trim);
        }

        // 处理最后一段
        B = this.tryPush(B);

        // 逐个输出段落
        for (; this.index < this.blocks.size(); this.index++) {
            B = this.blocks.get(this.index);

            // 标题: H
            if ("H" == B.type) {
                this.html += "\n<h"
                             + B.level
                             + ">"
                             + __line_to_html(B.content.get(0))
                             + "</h"
                             + B.level
                             + ">\n";
            }
            // 代码: code
            else if ("code" == B.type) {
                this.html += "\n<pre"
                             + (B.hasCodeType() ? " code-type=\"" + B.codeType + "\">" : ">");
                this.html += Strings.join("\n", B.content).replace("<", "&lt;");
                this.html += "</pre>";
            }
            // 列表: OL | UL
            else if ("OL" == B.type || "UL" == B.type) {
                __B_to_list(B);
            }
            // 水平线: hr
            else if ("hr" == B.type) {
                this.html += "\n<hr>";
            }
            // 表格
            else if ("Th" == B.type) {
                this.html += "\n<table>";

                // 记录表头
                MdBlock THead = B;
                String[] aligns = THead.cellAligns;
                if (null == aligns)
                    aligns = new String[0];

                // 输出表头
                this.html += "\n<thead>\n<tr>";
                int x = 0;
                for (String line : B.content) {
                    String align = "left";
                    if (x < aligns.length)
                        align = aligns[x];
                    this.html += "\n<th"
                                 + ("left".equals(align) ? " align=\"" + align + "\">" : ">");
                    this.html += __line_to_html(line);
                    this.html += "</th>";
                    x++;
                }
                this.html += "\n</tr>\n</thead>";

                // 输出表体
                this.html += "\n<tbody>";
                for (this.index++; this.index < this.blocks.size(); this.index++) {
                    B = this.blocks.get(this.index);
                    if ("Tr" == B.type) {
                        this.html += "\n<tr>";
                        x = 0;
                        for (String line : B.content) {
                            String align = "left";
                            if (x < aligns.length)
                                align = aligns[x];
                            this.html += "\n<td"
                                         + ("left".equals(align) ? " align=\"" + align + "\">"
                                                                 : ">");
                            this.html += __line_to_html(line);
                            this.html += "</td>";
                            x++;
                        }
                        this.html += "\n</tr>";
                    }
                    // 否则退出表格模式
                    else {
                        break;
                    }
                }
                this.html += "\n</tbody>";

                // 退回一个块
                this.index--;

                // 结束表格
                this.html += "\n</table>";
            }
            // 引用: quote
            else if ("quote" == B.type) {
                __B_to_blockquote(B);
            }
            // 默认是普通段落 : P
            else {
                this.html += "\n<p>";
                this.html += __B_to_html(B);
                this.html += "\n</p>";
            }
        }

        // 最后返回
        return this.html;
    }

}
