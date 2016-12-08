package org.nutz.walnut.ext.app.bean;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.segment.CharSegment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.MultiLineProperties;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnRace;

/**
 * 对于 app 配置信息的解析类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AppInfo {

    public String id;

    public String name;

    public List<AppDataItem> dataItems;

    public List<AppApiItem> apiItems;

    public List<AppWxhookItem> wxhookItems;
    
    public NutMap envs;

    public void parseAndClose(Reader reader, Context c) {
        try {
            parse(reader, c);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(reader);
        }
    }

    public void parse(Reader reader, Context c) throws IOException {
        MultiLineProperties mp = new MultiLineProperties(reader);

        // 解析基本属性
        id = mp.get("appid");
        name = mp.get("appnm");
        if (Strings.isBlank(name) && !Strings.isBlank(id)) {
            int pos = id.lastIndexOf('.');
            if (pos >= 0) {
                name = id.substring(pos + 1);
            } else {
                name = id;
            }
        }

        // 建立模板字符串上下文
        c.set("appid", id);
        c.set("appnm", name);

        // 处理数据
        this.dataItems = new LinkedList<AppDataItem>();
        String sData = mp.get("data");
        if (!Strings.isBlank(sData)) {
            __parse_data(sData, c);
        }

        // 处理 httpapi
        this.apiItems = new LinkedList<AppApiItem>();
        String sApi = mp.get("httpapi");
        if (!Strings.isBlank(sApi)) {
            __parse_httpapi(sApi, c);
        }

        // 处理微信
        this.wxhookItems = new LinkedList<AppWxhookItem>();
        String sWxhook = mp.get("wxhook");
        if (!Strings.isBlank(sWxhook)) {
            __parse_wxhook(sWxhook, c);
        }

        // 处理环境变量
        String sEnvs = mp.get("env");
        if (!Strings.isBlank(sEnvs)) {
            sEnvs = Segments.replace(sEnvs, c);
            this.envs = Json.fromJson(NutMap.class, sEnvs);
        }
    }

    private final static Pattern P_WXHOOK_BEGIN = Pattern.compile("^@([^:]+)(:(true|false))?$");

    private void __parse_wxhook(String str, Context c) {
        String[] lines = Strings.splitIgnoreBlank(str, "\n");
        AppWxhookItem item = null;
        StringBuilder sb = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // 注释行忽略
            if (line.startsWith("//"))
                continue;

            // 遇到开头
            Matcher m = P_WXHOOK_BEGIN.matcher(line);
            if (m.find()) {
                if (null != item) {
                    if (null != sb) {
                        item.addCommand(Segments.replace(sb.toString(), c));
                        sb = null;
                    }
                    wxhookItems.add(item);
                }
                item = new AppWxhookItem();
                item.id = Segments.replace(Strings.trim(m.group(1)), c);
                item.context = "true".equals(m.group(3));
                // 下一行就是属性
                if (i < (lines.length - 1)) {
                    line = lines[++i];
                    item.match = Json.fromJson(line);
                }
            }
            // 遇到命令行
            else if (line.startsWith(">")) {
                if (null != sb) {
                    item.addCommand(Segments.replace(sb.toString(), c));
                }
                sb = new StringBuilder();
                if (line.endsWith("\\")) {
                    line = Strings.trim(line.substring(1, line.length() - 1));
                } else {
                    line = Strings.trim(line.substring(1)) + " ";
                }
                sb.append(line);
            }
            // 其他的组合到档期命令行
            else {
                if (line.endsWith("\\"))
                    sb.append(line.substring(0, line.length() - 1));
                else
                    sb.append(line).append(' ');
            }
        }

        // 最后一个项目
        if (null != item) {
            if (null != sb) {
                item.addCommand(Segments.replace(sb.toString(), c));
            }
            wxhookItems.add(item);
        }
    }

    private final static Pattern P_API_HEADER = Pattern.compile("^[ \t]*[+][ \t]*([^=]+)[ \t]*=[ \t]*(.+)$");
    private final static Pattern P_API_META = Pattern.compile("^[ \t]*-[ \t]*([^= \t]+)[ \t]*=[ \t]*(.+)$");

    private void __parse_httpapi(String str, Context c) {

        String[] lines = Strings.splitIgnoreBlank(str, "\n");
        AppApiItem item = null;
        StringBuilder sb = null;

        boolean across_domain = false;

        for (String line : lines) {
            // 注释行忽略
            if (line.startsWith("//"))
                continue;
            // 全局属性
            if ("%across-domain".equals(line)) {
                across_domain = true;
                continue;
            }
            // 遇到了数据定义的开始
            if (line.startsWith("@")) {
                if (null != item) {
                    if (null != sb) {
                        item.addCommand(Segments.replace(sb.toString(), c));
                        sb = null;
                    }
                    apiItems.add(item);
                }
                item = new AppApiItem();
                item.path = Segments.replace(Strings.trim(line.substring(1)), c).trim();
                if (item.path.contains(" ")) {
                    String[] tmp = item.path.split(" ", 2);
                    if (tmp.length == 2) {
                        item.path = tmp[0];
                        item.when = tmp[1];
                    }
                }
                item.headers = new HashMap<String, String>();
                item.metas = new NutMap();

                if (across_domain) {
                    item.headers.put("Access-Control-Allow-Origin", "*");
                }

                continue;
            }

            // sb == null 表示进入命令行读取模式
            if (null == sb) {
                // 遇到http头据行
                Matcher m = P_API_HEADER.matcher(line);
                if (m.find()) {
                    String key = Strings.trim(m.group(1));
                    String val = Segments.replace(Strings.trim(m.group(2)), c);
                    item.headers.put(key.toUpperCase(), val);
                    continue;
                }

                /// 遇到元数据行
                m = P_API_META.matcher(line);
                if (m.find()) {
                    String key = Strings.trim(m.group(1));
                    String val = m.group(2);
                    item.metas.put(key, __str_to_obj(val, c));
                    continue;
                }
            }
            // 遇到命令行
            if (line.startsWith(">")) {
                if (null != sb) {
                    item.addCommand(Segments.replace(sb.toString(), c));
                }
                sb = new StringBuilder();
                if (line.endsWith("\\")) {
                    line = Strings.trim(line.substring(1, line.length() - 1));
                } else {
                    line = Strings.trim(line.substring(1)) + " ";
                }
                sb.append(line);
            }
            // 其他的组合到档期命令行
            else {
                if (line.endsWith("\\"))
                    sb.append(line.substring(0, line.length() - 1));
                else
                    sb.append(line).append(' ');
            }
        }
        // 最后一个项目
        if (null != item) {
            if (null != sb) {
                item.addCommand(Segments.replace(sb.toString(), c));
            }
            apiItems.add(item);
        }
    }

    private final static Pattern P_DATA_BEGIN = Pattern.compile("^@([^ ]+)[ ]+(.+)$");
    private final static Pattern P_DATA_META = Pattern.compile("^[ \t]*-[ \t]*([^= \t]+)[ \t]*=[ \t]*(.+)$");

    private void __parse_data(String str, Context c) {
        String[] lines = str.split("\r?\n");

        AppDataItem item = null;
        String key = null;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // 注释行和空行忽略
            if (line.startsWith("//") || Strings.isBlank(line))
                continue;

            // 遇到了数据定义的开始
            Matcher m = P_DATA_BEGIN.matcher(line);
            if (m.find()) {
                if (null != item) {
                    if (null != key) {
                        item.metas.put(key, __str_to_obj(sb.toString(), c));
                        key = null;
                        sb = new StringBuilder();
                    }
                    dataItems.add(item);
                }
                item = new AppDataItem();
                item.race = WnRace.valueOf(m.group(1).toUpperCase());
                item.path = Segments.replace(m.group(2), c);
                item.metas = new NutMap();
                continue;
            }
            // 遇到文件内容
            if (line.matches("^[-]{6,} *begin *$")) {
                if (null != key) {
                    item.metas.put(key, __str_to_obj(sb.toString(), c));
                    key = null;
                    sb = new StringBuilder();
                }
                // 一直读取到结束
                item.content = new StringBuilder();
                i++;
                for (; i < lines.length; i++) {
                    line = lines[i];
                    if (line.matches("^[-]{6,} *end *$"))
                        break;
                    line = line.replaceAll("(\\\\)([*@\\\\])", "$2");
                    if (line.contains("${img}")) // TODO 先临时解决一下
                        line = new CharSegment(line).render(c).toString();
                    item.content.append(line).append('\n');
                }

                continue;
            }
            // 遇到copy数据 TODO
            // if(line.matches(regex)){
            //
            // }
            // 遇到元数据行
            m = P_DATA_META.matcher(line);
            if (m.find()) {
                if (null != key) {
                    item.metas.put(key, __str_to_obj(sb.toString(), c));
                    sb = new StringBuilder();
                }
                key = Strings.trim(m.group(1));
                String val = m.group(2);
                sb.append(val);
            }
            // 其他的行附加到值
            else {
                sb.append(line);
            }
        }
        // 最后一个项目
        if (null != item) {
            if (null != key) {
                item.metas.put(key, __str_to_obj(sb.toString(), c));
            }
            dataItems.add(item);
        }
    }

    private Object __str_to_obj(String tmpl, Context c) {
        String s = Segments.replace(Strings.trim(tmpl), c);
        // 整数
        if (s.matches("^[0-9]+$"))
            return Integer.valueOf(s);

        // 长整数
        if (s.matches("^[0-9]+L$"))
            return Long.valueOf(s);

        // 浮点
        if (s.matches("^[0-9]*[.][0-9]+f$"))
            return Float.valueOf(s);

        // 双精度浮点
        if (s.matches("^[0-9]*[.][0-9]+$"))
            return Double.valueOf(s);

        // 布尔
        if (s.matches("^(true|false)$"))
            return Boolean.valueOf(s);

        // 如果是 JSON 对象
        if (Strings.isQuoteBy(s, '{', '}') || Strings.isQuoteBy(s, '[', ']')) {
            return Json.fromJson(s);
        }

        // 默认是字符串
        return s;
    }
}
