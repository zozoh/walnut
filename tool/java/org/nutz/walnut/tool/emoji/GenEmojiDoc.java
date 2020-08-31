package org.nutz.walnut.tool.emoji;

import java.io.File;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;

public class GenEmojiDoc {

    public static void main(String[] args) throws Exception {
        File dHome = Files.findFile("D:/tmp/emoji");
        File fHtml = Files.getFile(dHome, "EmojiXD.html");
        File fOut = Files.getFile(dHome, "emoji.md");
        Files.createFileIfNoExists(fOut);
        String hr = Strings.dup('-', 60);

        StringBuilder sb = new StringBuilder();

        // 解析
        Document doc = Jsoup.parse(fHtml, "UTF-8");

        // 找到主容器
        Element main = doc.select(".container.max-width-4").get(0);

        // 开始循环
        Elements eles = main.select("h2, div.clearfix");
        Iterator<Element> it = eles.iterator();
        while (it.hasNext()) {
            Element ele = it.next();

            // 标题
            if (ele.tagName().equals("h2")) {
                outf(sb, "%s\n", hr);
                outf(sb, "# %s\n", ele.text());
            }
            // 子分类
            else {
                // 找到标题
                String head = ele.select("h3.h3").get(0).text();
                // 分析
                Elements emojis = ele.select("a div.emoji");
                int amount = emojis.size();
                outf(sb, "\n## %s: `%d`\n\n", head, amount);

                // 打印子分类下的表情
                int i = 0;
                int width = 16;
                outf(sb, "```\n");
                Iterator<Element> it2 = emojis.iterator();
                while (it2.hasNext()) {
                    if (i > 0 && (i % width) == 0) {
                        outf(sb, "\n");
                    }
                    Element ee = it2.next();
                    outf(sb, ee.text().trim() + " ");
                    i++;
                }
                outf(sb, "\n```\n\n");
            }
        }

        // 写入文件
        Files.write(fOut, sb);

    }

    static void outf(StringBuilder sb, String fmt, Object... args) {
        String str = String.format(fmt, args);
        sb.append(str);
        System.out.print(str);
    }

}
