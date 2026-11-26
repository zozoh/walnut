package com.site0.walnut.ext.data.pebble;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Paths;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.util.Wn;

import io.pebbletemplates.pebble.loader.Loader;

/**
 * 让 Pebble 直接读 Walnut 对象树里的模板文件。
 *
 * <p>
 * 路径规则（对应 `man pebble`）：
 * <ul>
 * <li>`~/xxx`、`/home/xxx` → 按 Walnut 绝对路径解析
 * <li>其余 → 相对当前模板所在目录（`resolveRelativePath` 的 anchor）
 * </ul>
 *
 * <p>
 * 不做模板缓存：Walnut 的 IO 层自带读缓存，这里只负责把文件读成文本。
 */
public class WnPebbleLoader implements Loader<String> {

    private final WnIo io;

    private final WnSession se;

    public WnPebbleLoader(WnIo io, WnSession se) {
        this.io = io;
        this.se = se;
    }

    /**
     * 把模板名归一成对象树里的绝对路径：展开 `~` 与 `./`，并折叠 `..`
     */
    public String toPath(String name) {
        String ph = (null == se) ? name : Wn.normalizeFullPath(name, se);
        return collapse(ph);
    }

    @Override
    public Reader getReader(String cacheKey) {
        WnObj o = io.fetch(null, toPath(cacheKey));
        if (null == o) {
            throw Er.create("e.cmd.pebble.tpl.noexists", cacheKey);
        }
        return new StringReader(io.readText(o));
    }

    @Override
    public void setCharset(String charset) {
        // 由 WnIo 负责字符集
    }

    @Override
    public void setPrefix(String prefix) {
        // 不支持前缀
    }

    @Override
    public void setSuffix(String suffix) {
        // 模板名自带后缀
    }

    @Override
    public String resolveRelativePath(String relativePath, String anchorPath) {
        // 绝对路径原样
        if (isAbsolute(relativePath)) {
            return toPath(relativePath);
        }
        // 相对当前模板所在目录
        String base = toPath(anchorPath);
        int pos = base.lastIndexOf('/');
        String dir = pos > 0 ? base.substring(0, pos) : "";
        return collapse(dir + "/" + relativePath);
    }

    @Override
    public String createCacheKey(String templateName) {
        return templateName;
    }

    @Override
    public boolean resourceExists(String templateName) {
        return null != io.fetch(null, toPath(templateName));
    }

    private static boolean isAbsolute(String ph) {
        return ph.startsWith("/") || ph.startsWith("~");
    }

    private static String collapse(String ph) {
        if (null == ph || ph.isEmpty()) {
            return ph;
        }
        boolean asDir = ph.endsWith("/");
        String s = Paths.get(ph).normalize().toString().replace('\\', '/');
        if (asDir && !s.endsWith("/")) {
            s += "/";
        }
        return s;
    }
}
