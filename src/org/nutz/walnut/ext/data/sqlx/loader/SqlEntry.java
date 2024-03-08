package org.nutz.walnut.ext.data.sqlx.loader;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.walnut.util.Ws;

public class SqlEntry {

    static Pattern _P = Pattern.compile("^@([a-z0-9_]+)=(.+)$");

    public static List<SqlEntry> load(String input) {
        List<SqlEntry> list = new LinkedList<>();
        String[] lines = input.split("\r?\n");

        SqlEntry en = null;
        StringBuilder sb = null;
        for (String line : lines) {
            // 特殊注释行，就开始一个新实体
            if (line.startsWith("--")) {
                // 推入老实体
                if (null != en && en.hasName()) {
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
                    sb.append('\n');
                }
                sb.append(line);
            }
        }
        return list;
    }

    private String name;

    private SqlType type;

    private String[] defaultPick;

    private String[] defaultOmit;

    private Boolean defaultIgnoreNil;

    private String content;

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
