package org.nutz.walnut.ext.mq;

import org.nutz.castor.Castors;
import org.nutz.lang.Encoding;
import org.nutz.lang.Strings;
import org.nutz.lang.meta.Pair;

public class WnMqMessage {

    private String topic;

    private WnMqMsgType type;

    private String body;

    public WnMqMessage() {
        this("sys", WnMqMsgType.CMD, null);
    }

    public WnMqMessage(String topic, String body) {
        this(topic, WnMqMsgType.CMD, body);
    }

    public WnMqMessage(String topic, WnMqMsgType type, String body) {
        this.topic = topic;
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
                // Topic
                if ("topic".equals(name)) {
                    this.setTopic(value);
                }
                // Type
                else if ("type".equals(name)) {
                    this.setType(value);
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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

    public byte[] getBodyBytes() {
        return body.getBytes(Encoding.CHARSET_UTF8);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!Strings.isBlank(topic)) {
            sb.append(String.format("@topic=%s\n", topic));
        }
        if (null == type) {
            sb.append("@type=CMD\n");
        } else {
            sb.append(String.format("@type=%s\n", type.toString()));
        }
        if (sb.length() > 0) {
            sb.append("\n");
        }
        sb.append(this.body);
        return sb.toString();
    }
}
