package org.nutz.walnut.util;

import java.util.LinkedList;

import org.nutz.lang.Strings;

public abstract class Wpath {

    /**
     * @param path
     *            全路径
     * @return 文件或者目录名
     */
    public static String getName(String path) {
        if (!Strings.isBlank(path)) {
            int pos = path.replace('\\', '/').lastIndexOf('/');
            if (pos != -1)
                return path.substring(pos + 1);
        }
        return path;
    }

    /**
     * 获取文件主名。 即去掉后缀的名称
     * 
     * @param path
     *            文件路径
     * @return 文件主名
     */
    public static String getMajorName(String path) {
        int len = path.length();
        int l = 0;
        int r = len;
        for (int i = r - 1; i > 0; i--) {
            if (r == len)
                if (path.charAt(i) == '.') {
                    r = i;
                }
            if (path.charAt(i) == '/' || path.charAt(i) == '\\') {
                l = i + 1;
                break;
            }
        }
        return path.substring(l, r);
    }

    /**
     * 获取文件后缀名，不包括 '.'，如 'abc.gif','，则返回 'gif'
     * 
     * @param path
     *            文件路径
     * @return 文件后缀名
     */
    public static String getSuffixName(String path) {
        if (null == path)
            return null;
        path = path.replace('\\', '/');
        int p0 = path.lastIndexOf('.');
        int p1 = path.lastIndexOf('/');
        if (-1 == p0 || p0 < p1)
            return "";
        return path.substring(p0 + 1);
    }

    /**
     * 获取文件后缀名，包括 '.'，如 'abc.gif','，则返回 '.gif'
     * 
     * @param path
     *            文件路径
     * @return 文件后缀
     */
    public static String getSuffix(String path) {
        if (null == path)
            return null;
        path = path.replace('\\', '/');
        int p0 = path.lastIndexOf('.');
        int p1 = path.lastIndexOf('/');
        if (-1 == p0 || p0 < p1)
            return "";
        return path.substring(p0);
    }

    /**
     * 将多个路径拼合成一个路径，他会自动去除重复的 "/"
     * 
     * <pre>
     * appendPath("a","b")  => "a/b"
     * appendPath("/a","b/c")  => "/a/b/c"
     * appendPath("/a/","/b/c")  => "/a/b/c"
     * </pre>
     * 
     * @param phs
     *            路径数组
     * @return 拼合后的路径
     */
    public static String appendPath(String... phs) {
        String[] paths = Wlang.without(phs, null);
        if (null != paths && paths.length > 0) {
            // paths[0] = "/";
            String str = Ws.join(paths, "/");
            String[] ss = Ws.splitIgnoreBlank(str, "/");
            str = Ws.join(ss, "/").toString();
            if (paths[0].startsWith("/")) {
                return "/" + str;
            }
            return str;
        }
        return null;
    }

    /**
     * 修改路径
     * 
     * @param path
     *            路径
     * @param newName
     *            新名称
     * @return 新路径
     */
    public static String renamePath(String path, String newName) {
        if (!Strings.isBlank(path)) {
            int pos = path.replace('\\', '/').lastIndexOf('/');
            if (pos > 0)
                return path.substring(0, pos) + "/" + newName;
        }
        return newName;
    }

    /**
     * @param path
     *            路径
     * @return 父路径
     */
    public static String getParent(String path) {
        if (Strings.isBlank(path))
            return path;
        int pos = path.replace('\\', '/').lastIndexOf('/');
        if (pos > 0)
            return path.substring(0, pos);
        return "/";
    }

    /**
     * 将两个路径比较，得出相对路径
     * 
     * @param base
     *            基础路径，以 '/' 结束，表示目录
     * @param path
     *            相对文件路径，以 '/' 结束，表示目录
     * @return 相对于基础路径对象的相对路径
     */
    public static String getRelativePath(String base, String path) {
        return getRelativePath(base, path, "./");
    }

    /**
     * 将两个路径比较，得出相对路径
     * 
     * @param base
     *            基础路径，以 '/' 结束，表示目录
     * @param path
     *            相对文件路径，以 '/' 结束，表示目录
     * @param equalPath
     *            如果两个路径相等，返回什么，通常为 "./"。 你也可以用 "" 或者 "." 或者随便什么字符串来表示
     * 
     * @return 相对于基础路径对象的相对路径
     */
    public static String getRelativePath(String base, String path, String equalPath) {
        // 如果两个路径相等
        if (base.equals(path) || "./".equals(path) || ".".equals(path)) {
            return equalPath;
        }

        // 开始判断
        String[] bb = Ws.splitIgnoreBlank(getCanonicalPath(base), "[\\\\/]");
        String[] ff = Ws.splitIgnoreBlank(getCanonicalPath(path), "[\\\\/]");
        int len = Math.min(bb.length, ff.length);
        int pos = 0;
        for (; pos < len; pos++)
            if (!bb[pos].equals(ff[pos]))
                break;

        // 证明路径是相等的
        if (len == pos && bb.length == ff.length) {
            return equalPath;
        }

        // 开始查找不同
        int dir = 1;
        if (base.endsWith("/"))
            dir = 0;

        StringBuilder sb = new StringBuilder();
        sb.append(Ws.repeat("../", bb.length - pos - dir));
        sb.append(Ws.join(ff, "/", pos));
        if (path.endsWith("/"))
            sb.append('/');
        return sb.toString();
    }

    /**
     * 获取两个路径从头部开始计算的交集
     * 
     * @param ph0
     *            路径1
     * @param ph1
     *            路径2
     * @param dft
     *            如果两个路径完全没有相交，那么返回什么
     * @return 两个路径的交集
     */
    public static String getIntersectPath(String ph0, String ph1, String dft) {
        // 木可能有交集
        if (null == ph0 || null == ph1)
            return dft;

        String[] ss0 = Ws.splitIgnoreBlank(ph0, "[\\\\/]");
        String[] ss1 = Ws.splitIgnoreBlank(ph1, "[\\\\/]");

        int pos = 0;
        int len = Math.min(ss0.length, ss1.length);
        for (; pos < len; pos++) {
            if (!ss0[pos].equals(ss1[pos]))
                break;
        }

        // 木有交集
        if (pos == 0)
            return dft;

        // 得到
        String re = Ws.join(ss0, "/", 0, pos);

        // 需要补全后面的 "/" 吗
        if (ph0.endsWith("/") && ph1.endsWith("/"))
            return re + "/";

        return re;
    }

    /**
     * 整理路径。 将会合并路径中的 ".."
     * 
     * @param path
     *            路径
     * @return 整理后的路径
     */
    public static String getCanonicalPath(String path) {
        if (Ws.isBlank(path))
            return path;

        String[] pa = Ws.splitIgnoreBlank(path, "[\\\\/]");
        LinkedList<String> paths = new LinkedList<String>();
        for (String s : pa) {
            if ("..".equals(s)) {
                if (paths.size() > 0)
                    paths.removeLast();
                continue;
            }
            if (".".equals(s)) {
                // pass
            } else {
                paths.add(s);
            }
        }

        String s = Ws.join(paths, "/");
        if (path.startsWith("/")) {
            return "/" + s;
        }
        if (path.endsWith("/")) {
            return s + "/";
        }
        return s;
    }

}
