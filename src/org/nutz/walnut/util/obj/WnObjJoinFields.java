package org.nutz.walnut.util.obj;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Ws;

/**
 * 描述了对象或数据集查询时，需要用到的字段连接设置。譬如
 * 
 * <pre>
 * event_id=id@~/events[evd_inner,evd_client:xyz]
 * 
 * {fromKey}={targetKey}@{targetPath}[{JoinField}...]
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnObjJoinFields {

    private String fromKey;

    private String targetKey;

    private String targetPath;

    private JoinField[] fields;

    public WnObjJoinFields(String input) {
        String REG = "^([a-zA-Z0-9_]+)"
                     + "="
                     + "([a-zA-Z0-9_]+)"
                     + "@"
                     + "([^\\[]+)"
                     + "\\["
                     + "([^\\]]+)"
                     + "\\]";
        Pattern P = Pattern.compile(REG);
        Matcher m = P.matcher(Ws.trim(input));
        if (!m.find()) {
            throw Er.create("e.obj.join.InvalidInput", input);
        }
        this.fromKey = m.group(1);
        this.targetKey = m.group(2);
        this.targetPath = m.group(3);
        String flds = m.group(4);
        String[] ss = Ws.splitIgnoreBlank(flds);
        this.fields = new JoinField[ss.length];
        for (int i = 0; i < ss.length; i++) {
            String s = ss[i];
            this.fields[i] = new JoinField(s);

        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(fromKey);
        sb.append('=');
        sb.append(targetKey);
        sb.append('@');
        sb.append(targetPath);
        sb.append('[');
        for (int i = 0; i < fields.length; i++) {
            JoinField jf = fields[i];
            if (i > 0) {
                sb.append(',');
            }
            sb.append(jf);
        }
        sb.append(']');
        return sb.toString();
    }

    public boolean isJoinable() {
        return this.hasFields() && this.hasFromKey() && this.hasTargetKey() && this.hasTargetPath();
    }

    public boolean hasFromKey() {
        return !Ws.isBlank(fromKey);
    }

    public boolean hasTargetPath() {
        return !Ws.isBlank(targetKey);
    }

    public boolean hasTargetKey() {
        return !Ws.isBlank(targetKey);
    }

    public boolean hasFields() {
        return null != fields && fields.length > 0;
    }

    /**
     * 如果： <code>evd_inner:a</code> 那么
     * <ul>
     * <li><code>fromName</code> is <code>evd_inner</code>
     * <li><code>toName</code> is <code>a</code>
     * </ul>
     * 有时候 <cod>toName</code> 是空的，那么表示字段名并不改变
     */
    public static class JoinField {
        String fromName;
        String toName;

        JoinField(String input) {
            String[] ss = Ws.splitIgnoreBlank(input, ":");
            if (ss.length > 1) {
                this.fromName = ss[0];
                this.toName = ss[1];
            } else {
                this.fromName = ss[0];
            }
        }

        @Override
        public String toString() {
            if (Ws.isBlank(toName)) {
                return this.fromName;
            }
            return this.fromName + ":" + this.toName;
        }
    }

    public String getFromKey() {
        return fromKey;
    }

    public void setFromKey(String fromKey) {
        this.fromKey = fromKey;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }

    public JoinField[] getFields() {
        return fields;
    }

    public void setFields(JoinField[] fields) {
        this.fields = fields;
    }

}
