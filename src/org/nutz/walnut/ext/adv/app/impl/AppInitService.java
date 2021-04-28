package org.nutz.walnut.ext.adv.app.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.adv.app.bean.init.AppInitGroup;
import org.nutz.walnut.ext.adv.app.bean.init.AppInitItem;

public class AppInitService {

    public AppInitGroup parse(String str) {
        return parse(str, null);
    }

    public void process(AppInitContext ac) {
        if (!ac.group.hasItems())
            return;

        int hrSize = 50;
        if (null != ac.outPrefix) {
            hrSize -= ac.outPrefix.length();
        }
        String HR = Strings.dup('-', hrSize);
        if (ac.group.hasTitle()) {
            ac.println(HR);
            ac.println("APP INIT");
            ac.println(ac.group.getTitle());
        }
        ac.println(HR);

        for (AppInitItem item : ac.group.getItems()) {
            AppInitProcessor pc = ac.getProcessor(item);
            AppInitItemContext ing = ac.createProcessing(item);
            ing.printItem(item);
            pc.process(ing);
            ing.println(HR);
        }

    }

    public AppInitGroup parse(String str, NutBean vars) {
        AppInitGroup group = new AppInitGroup();

        // 逐行解析
        String[] lines = str.split("\r?\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // 注释行与空行
            if (line.startsWith("#") || Strings.isBlank(line))
                continue;

            // 开启一个段
            if (line.startsWith("@")) {
                AppInitItem item = new AppInitItem();
                i = this.loadItem(lines, i, item, vars);
                group.addItem(item);
            }
        }

        return group;
    }

    private static final String REGEX = "^@(\\w+)\\s*"
                                        + "([^\\s]+)?\\s*"
                                        + "('([^']*)')?\\s*"
                                        + "(\\{[^}]*\\})?\\s*"
                                        + "(->\\s*(.+)\\s*)?$";

    private static final Pattern _P0 = Regex.getPattern(REGEX);

    private static final Pattern _P1 = Regex.getPattern("^([%?])(COPY|TMPL)(\\{.+\\})?([:>])\\s*(.+)?$");

    private int loadItem(String[] lines, int index, AppInitItem item, NutBean vars) {
        // 解析首行
        String line = lines[index];
        Matcher m = _P0.matcher(line);

        if (!m.find()) {
            throw Er.create("e.cmd.app_init.invalid_line", "Line " + index + " : " + line);
        }

        /**
         * <pre>
          * ------------------------------------------------------------
        * 匹配 '@API ~/a/b/c.txt 'i18n:xxx/<fas-xxx>/cross/respJson'{x:100,y50} -> /mnt/abc/xx.txt'
        * ------------------------------------------------------------
        * 0/82  Regin:0/82
        *  0:[  0, 82) @API ~/a/b/c.txt 'i18n:xxx/<fas-xxx>/cross/respJson'{x:100,y50} -> /mnt/abc/xx.txt
        *  1:[  1,  4) API
        *  2:[  5, 16) ~/a/b/c.txt
        *  3:[ 17, 52) 'i18n:xxx/<fas-xxx>/cross/respJson'
        *  4:[ 18, 51) i18n:xxx/<fas-xxx>/cross/respJson
        *  5:[ 52, 63) {x:100,y50}
        *  6:[ 64, 82) -> /mnt/abc/xx.txt
        *  7:[ 67, 82) /mnt/abc/xx.txt
         * </pre>
         */
        item.setType(m.group(1));
        item.setPath(m.group(2), vars);
        item.addQuickMeta(m.group(4), vars);
        item.setProperties(m.group(5), vars);
        item.setLinkPath(m.group(7), vars);

        // 准备收集元数据
        List<String> metaLine = new LinkedList<>();

        // 寻找到内容行，或者下一个段
        Matcher mc = null;
        for (int i = index + 1; i < lines.length; i++) {
            line = lines[i];
            // 记录当前下标
            index = i;
            // 遇到注释行，直接退出
            if (line.startsWith("#")) {
                break;
            }
            // 下一个段开始，需要回退一步
            if (line.startsWith("@")) {
                index--;
                break;
            }
            // 遇到内容段
            mc = _P1.matcher(line);
            if (mc.find()) {
                break;
            }
            // 其他的加入元数据行
            metaLine.add(line);
            mc = null;
        }

        // 分析元数据
        String json = Strings.trim(Strings.join("\n", metaLine));
        item.addMeta(json, vars);

        // 解析内容行
        if (null != mc) {
            // 看看行首
            // ------------------------------------------------------------
            // 匹配 '%TMPL{x:100}> path/to/file'
            // ------------------------------------------------------------
            // 0/26 Regin:0/26
            // 0:[ 0, 26) %TMPL{x:100}> path/to/file
            // 1:[ 0, 1) %
            // 2:[ 1, 5) TMPL
            // 3:[ 5, 12) {x:100}
            // 4:[ 12, 13) >
            // 5:[ 14, 26) path/to/file
            item.setOverrideContent("%".equals(mc.group(1)));
            item.setContentAsTmpl("TMPL".equals(mc.group(2)));
            item.setContentFileVars(mc.group(3), vars);
            // 读取磁盘上的文件
            if (">".equals(mc.group(4))) {
                String rph = Strings.trim(mc.group(5));
                item.setContentFilePath(rph);
            }
            // 继续读取内容，直到 %END%
            else {
                StringBuilder sb = new StringBuilder();
                for (index++; index < lines.length; index++) {
                    line = lines[index];
                    String trim = Strings.trim(line);
                    // 结束了
                    if ("%END%".equals(trim)) {
                        break;
                    }
                    // 附加内容
                    else {
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append(line);
                    }
                }
                item.setContent(sb.toString());
            }

        }

        // 返回下标
        return index;
    }

}
