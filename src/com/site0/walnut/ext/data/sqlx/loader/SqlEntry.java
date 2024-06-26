package com.site0.walnut.ext.data.sqlx.loader;

import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.lang.Streams;
import com.site0.walnut.util.Ws;

public class SqlEntry {

    public static List<SqlEntry> load(InputStream ins) {
        try {
            Reader r = Streams.utf8r(ins);
            String str = Streams.readAndClose(r);
            return load(str);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    static Pattern _P = Pattern.compile("^@([a-z0-9_]+)\\s*=\\s*(.+)$");

    public static List<SqlEntry> load(String input) {
        String[] lines = input.split("\r?\n");
        return loadLines(lines);
    }

    public static List<SqlEntry> loadLines(String[] lines) {
        List<SqlEntry> list = new LinkedList<>();

        SqlEntry en = null;
        StringBuilder sb = null;
        for (String line : lines) {
            // 特殊注释行，就开始一个新实体
            if (line.startsWith("--")) {
                // 推入老实体： 如果标识了元数据，且已经开始读取内容行
                if (null != en && en.hasName() && sb.length() > 0) {
                    en.content = Ws.trim(sb);
                    list.add(en);
                    // 清理老实体
                    en = null;
                }
                // 开始新实体
                if (null == en) {
                    en = new SqlEntry();
                    sb = new StringBuilder();
                }

                // 对注释行进行分析
                String str = Ws.trim(line.substring(2));
                Matcher m = _P.matcher(str);
                if (m.find()) {
                    String key = m.group(1);
                    String val = m.group(2);
                    en.set(key, val);
                }

                // 继续下一行
                continue;
            }
            // 其他行，就作为 SQL 模板
            else if (null != sb) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(line);
            }
        }
        // 推入最后一个实体
        if (null != en && en.hasName()) {
            en.content = Ws.trim(sb);
            list.add(en);
            // 清理老实体
            en = null;
        }
        // 返回列表
        return list;
    }

    private String name;

    private SqlType type;

    private String[] defaultPick;

    private String[] defaultOmit;

    private Boolean defaultIgnoreNil;

    private String content;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (null != name) {
            sb.append("@[").append(this.name).append("] ");
        }
        if (null != type) {
            sb.append("<").append(this.type).append("> ");
        }
        if (null != defaultPick) {
            sb.append("pick=(").append(Json.toJson(defaultPick));
            sb.append("); ");
        }
        if (null != defaultOmit) {
            sb.append("omit=(").append(Json.toJson(defaultOmit));
            sb.append("); ");
        }
        if (null != this.defaultIgnoreNil && this.defaultIgnoreNil) {
            sb.append("!NIL");
        }
        if (null != this.content) {
            sb.append("\n   >> ").append(this.content);
        }
        return sb.toString();
    }

    public void set(String key, String val) {
        String str = Ws.trim(val);
        if ("name".equals(key)) {
            this.setName(str);
        } else if ("type".equals(key)) {
            String typeName = str.toUpperCase();
            this.type = SqlType.valueOf(typeName);
        } else if ("pick".equals(key)) {
            this.defaultPick = Ws.splitIgnoreBlank(str);
        } else if ("omit".equals(key)) {
            this.defaultOmit = Ws.splitIgnoreBlank(str);
        } else if ("ignoreNil".equalsIgnoreCase(key)) {
            this.defaultIgnoreNil = Castors.me().castTo(str, Boolean.class);
        }
    }

    public boolean hasName() {
        return !Ws.isBlank(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasType() {
        return null != this.type;
    }

    public SqlType getType() {
        return type;
    }

    public void setType(SqlType type) {
        this.type = type;
    }

    public String[] getDefaultPick() {
        return defaultPick;
    }

    public void setDefaultPick(String[] defaultPick) {
        this.defaultPick = defaultPick;
    }

    public String[] getDefaultOmit() {
        return defaultOmit;
    }

    public void setDefaultOmit(String[] defaultOmit) {
        this.defaultOmit = defaultOmit;
    }

    public Boolean getDefaultIgnoreNil() {
        return defaultIgnoreNil;
    }

    public void setDefaultIgnoreNil(Boolean defaultIgnoreNil) {
        this.defaultIgnoreNil = defaultIgnoreNil;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
