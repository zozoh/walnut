package org.nutz.walnut.ext.mq;

import org.nutz.castor.Castors;
import org.nutz.lang.Encoding;
import org.nutz.lang.Strings;
import org.nutz.lang.meta.Pair;

public class WnMqMessage {

    private WnMqMsgType type;

    private String user;

    private String secret;

    private String body;

    public WnMqMessage() {
        this(WnMqMsgType.CMD, null);
    }

    public WnMqMessage(WnMqMsgType type) {
        this(type, null);
    }

    public WnMqMessage(WnMqMsgType type, String body) {
        this.type = type;
        this.body = body;
    }

    public WnMqMessage(String msg) {
        this();
        this.parseText(msg);
    }

    public void parseText(String msg) {
        String[] lines = msg.split("\r?\n");
        boolean lastIsBlank = true;
        int bodyIndex = 0;

        // 逐行扫描，一直遇到 body开始
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // 遇到空行
            if (Strings.isBlank(line)) {
                // 连续空行
                if (lastIsBlank) {
                    bodyIndex = i + 1;
                    break;
                }
                // 否则标记一下
                lastIsBlank = true;
                continue;
            }
            // 不是空行
            else {
                lastIsBlank = false;
            }

            // 遇到注释行
            if (line.startsWith("#")) {
                continue;
            }

            // 开头的元数据
            if (line.startsWith("@")) {
                Pair<String> pa = Pair.create(line.substring(1).trim());
                String name = pa.getName();
                String value = pa.getValue();
                // Type
                if ("type".equals(name)) {
                    this.setType(value);
                }
                // User
                else if ("user".equals(name)) {
                    this.setUser(value);
                }
                // secret
                else if ("secret".equals(name)) {
                    this.setSecret(value);
                }
                continue;
            }

            // 其他有内容的，那么就直接就是 Body 咯
            bodyIndex = i;
            break;
        }

        // 计算 Body
        int len = lines.length - bodyIndex;
        String body = Strings.join(bodyIndex, len, "\n", lines);
        this.setBody(body);
    }

    public WnMqMsgType getType() {
        return type;
    }

    public void setType(WnMqMsgType type) {
        this.type = type;
    }

    public void setType(String type) {
        if (null != type) {
            String ut = type.toUpperCase();
            this.type = Castors.me().castTo(ut, WnMqMsgType.class);
        } else {
            this.type = null;
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setDefaultUser(String user) {
        if (Strings.isBlank(this.user)) {
            this.user = user;
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setDefaultSecret(String secret) {
        if (Strings.isBlank(this.secret)) {
            this.secret = secret;
        }
    }

    public boolean hasBody() {
        return !Strings.isBlank(body);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public byte[] toBytes() {
        String str = this.toString();
        return str.getBytes(Encoding.CHARSET_UTF8);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        // .........................................
        if (null == type) {
            sb.append("@type=CMD\n");
        } else {
            sb.append(String.format("@type=%s\n", type.toString()));
        }
        // .........................................
        if (!Strings.isBlank(user)) {
            sb.append(String.format("@user=%s\n", user));
        }
        // .........................................
        if (!Strings.isBlank(secret)) {
            sb.append(String.format("@secret=%s\n", secret));
        }
        // .........................................
        if (sb.length() > 0) {
            sb.append("\n");
        }
        sb.append(this.body);
        return sb.toString();
    }
}
