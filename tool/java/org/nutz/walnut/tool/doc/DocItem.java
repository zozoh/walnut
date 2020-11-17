package org.nutz.walnut.tool.doc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.Regex;

public class DocItem implements Comparable<DocItem> {

    private String name;

    private String title;

    private String key;

    private String path;

    private String homePath;

    protected File dHome;

    protected File file;

    public DocItem(File dHome, File f) {
        BufferedReader br = null;
        try {
            this.dHome = dHome;
            this.file = f;
            this.homePath = dHome.getCanonicalPath();
            this.name = Files.getMajorName(f);
            if (f.isFile()) {
                br = Streams.buffr(Streams.fileInr(f));
                String line = null;
                boolean inHead = false;
                while ((line = br.readLine()) != null) {
                    String trim = Strings.trim(line);
                    if ("---".equals(line)) {
                        inHead = !inHead;
                        if (!inHead)
                            break;
                        continue;
                    }
                    if (inHead) {
                        Matcher m = Regex.getPattern("^title\\s*:\\s*(.+)$").matcher(trim);
                        if (m.find()) {
                            this.title = m.group(1).trim();
                            continue;
                        }
                        m = Regex.getPattern("^key\\s*:\\s*(.+)$").matcher(trim);
                        if (m.find()) {
                            this.key = m.group(1).trim();
                            continue;
                        }
                    }
                }
            }

            this.path = f.getCanonicalPath();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(br);
        }
    }

    public void joinString(StringBuilder sb) {
        if (null == key) {
            String rph = Disks.getRelativePath(this.homePath, this.path);
            sb.append('[').append(this.getDisplayTitle()).append(']');
            sb.append('(').append(rph).append(')');
        }
        // 都有
        else {
            sb.append(String.format("[%s][%s]", this.getDisplayTitle(), key));
        }
    }

    public void joinRefers(StringBuilder sb) {
        if (null != this.key && null != this.path) {
            String rph = Disks.getRelativePath(this.homePath, this.path);
            sb.append(String.format("[%s]: %s", key, rph));
            sb.append('\n');
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.joinString(sb);
        return sb.toString();
    }

    @Override
    public int compareTo(DocItem o) {
        String k0 = this.key;
        String k1 = o.key;

        // 没 Key 就相等
        if (null == k0 && null == k1)
            return this.name.compareTo(o.name);

        // 否则有 key 的大
        if (null == k0)
            return -1;

        if (null == k1)
            return 1;

        // 如果短的以长的开始，比较级别
        if (k0.startsWith(k1) || k1.startsWith(k0)) {
            // 有 Key 计算级别
            int l0 = k0.split("-").length;
            int l1 = k1.split("-").length;

            if (l0 == l1) {
                return k0.compareTo(k1);
            }

            return l0 - l1;
        }
        
        return this.name.compareTo(o.name);

    }

    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDisplayTitle() {
        return Strings.sBlank(this.title, this.name);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
