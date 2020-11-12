package org.nutz.walnut.util.upload;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.lang.util.Regex;

public class FormField {

    private HttpFormFieldType type;

    private String name;

    private String fileName;

    private String contentType;

    public FormField() {}

    public FormField(String str) {
        this.parse(str);
    }

    private static final String REG = "(form-data)"
                                      + "|(name=\"(((?<=\\\\)\"|[^\"])*)\")"
                                      + "|(filename=\"(((?<=\\\\)\"|[^\"])*)\")";
    private static final Pattern P = Regex.getPattern(REG);

    /**
     * 解析 HTML Form Upload 的字段头，其格式可能是
     * 
     * <pre>
     * Content-Disposition: form-data; name="f1"; filename="hello.txt"
     * Content-Type: text/plain
     * </pre>
     * 
     * 亦可能是
     * 
     * <pre>
     * Content-Disposition: form-data; name="age"
     * </pre>
     * 
     * 总之会按行解析，并会自动设置 type
     * 
     * @param str
     *            字段头
     * @return 自身
     */
    public FormField parse(String str) {
        // 逐行解析
        String[] lines = Strings.splitIgnoreBlank(str, "\r?\n");
        for (String line : lines) {
            // 首先取到本行键与值
            int pos = line.indexOf(':');
            if (pos <= 0) {
                continue;
            }

            String key = line.substring(0, pos).trim();
            String val = line.substring(pos + 1).trim();

            // 对于 Content-Disposition 深入分析
            if ("Content-Disposition".equalsIgnoreCase(key)) {
                Matcher m = P.matcher(val);
                while (m.find()) {
                    String name = m.group(3);
                    if (null != name)
                        this.name = name.replace("\\\"", "\"");
                    String fnm = m.group(6);
                    if (null != fnm)
                        this.fileName = fnm.replace("\\\"", "\"");
                }
                if (!Strings.isBlank(this.fileName)) {
                    this.asFile();
                } else {
                    this.asText();
                }
            }
            // 对于 Content-Disposition 设值
            else if ("Content-Type".equals(key)) {
                this.contentType = val;
            }
            // 其他的就无视吧
        }

        return this;
    }

    public boolean isText() {
        return this.type == HttpFormFieldType.TEXT;
    }

    public boolean isFile() {
        return this.type == HttpFormFieldType.FILE;
    }

    public HttpFormFieldType getType() {
        return type;
    }

    public void setType(HttpFormFieldType type) {
        this.type = type;
    }

    public void asText() {
        this.type = HttpFormFieldType.TEXT;
    }

    public void asFile() {
        this.type = HttpFormFieldType.FILE;
    }

    public boolean isName(String name) {
        if (Strings.isBlank(this.name))
            return Strings.isBlank(name);
        return this.name.equals(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasFileName() {
        return !Strings.isBlank(this.fileName);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean hasContentType() {
        return !Strings.isBlank(this.contentType);
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
