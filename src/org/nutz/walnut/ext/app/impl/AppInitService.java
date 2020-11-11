package org.nutz.walnut.ext.app.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.app.bean.init.AppInitGroup;
import org.nutz.walnut.ext.app.bean.init.AppInitItem;

public class AppInitService {

    public AppInitGroup parse(String str) {
        return parse(str, null);
    }

    public void process(AppInitContext aic) {
        if (!aic.group.hasItems())
            return;
        for (AppInitItem item : aic.group.getItems()) {
            AppInitProcessor pc = aic.getProcessor(item);
            AppInitItemContext ing = aic.createProcessing(item);
            pc.process(ing);
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
                                        + "(\\{[^}]*\\})?\\s*"
                                        + "([^\\s]+)?\\s*"
                                        + "(->\\s*(.+)\\s*)?$";

    private static final Pattern _P0 = Regex.getPattern(REGEX);

    private static final Pattern _P1 = Regex.getPattern("^([%?])(COPY|TMPL)([:>])\\s*(.+)?$");

    private int loadItem(String[] lines, int index, AppInitItem item, NutBean vars) {
        // 解析首行
        String line = lines[index];
        Matcher m = _P0.matcher(line);

        if (!m.find()) {
            throw Er.create("e.cmd.app_init.invalid_line", "Line " + index + " : " + line);
        }

        // ------------------------------------------------------------
        // 匹配 '@FILE{title:'haha', icon:'fas-box'} ~/a/b/c.txt ->
        // /mnt/abc/xx.txt'
        // ------------------------------------------------------------
        // 0/66 Regin:0/66
        // 0:[ 0, 66) @FILE{title:'haha', icon:'fas-box'} ~/a/b/c.txt ->
        // /mnt/abc/xx.txt
        // 1:[ 1, 5) FILE
        // 2:[ 5, 35) {title:'haha', icon:'fas-box'}
        // 3:[ 36, 47) ~/a/b/c.txt
        // 4:[ 48, 66) -> /mnt/abc/xx.txt
        // 5:[ 51, 66) /mnt/abc/xx.txt

        item.setType(m.group(1));
        item.setProperties(m.group(2), vars);
        item.setPath(m.group(3), vars);
        item.setLinkPath(m.group(5), vars);

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
        item.setMeta(json, vars);

        // 解析内容行
        if (null != mc) {
            // 看看行首
            // ------------------------------------------------------------
            // 匹配 '?COPY> path/to/file'
            // ------------------------------------------------------------
            // 0/19 Regin:0/19
            // 0:[ 0, 19) ?COPY> path/to/file
            // 1:[ 0, 1) ?
            // 2:[ 1, 5) COPY
            // 3:[ 5, 6) >
            // 4:[ 7, 19) path/to/file
            item.setOverrideContent("%".equals(mc.group(1)));
            item.setContentAsTmpl("TMPL".equals(mc.group(2)));
            // 读取磁盘上的文件
            if (">".equals(mc.group(3))) {
                String rph = Strings.trim(mc.group(4));
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
