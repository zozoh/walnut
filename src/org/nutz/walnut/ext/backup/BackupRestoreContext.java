package org.nutz.walnut.ext.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.nutz.log.Log;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.impl.box.WnSystem;

public class BackupRestoreContext {

    /**
     * 根路径
     */
    public String base;

    public boolean force_id;
    public boolean ignore_sha1_miss;

    /**
     * 需要包含的路径
     */
    public List<String> includes;

    /**
     * 需要包含的文件路径模式
     */
    public List<String> includePatterns;

    /**
     * 需要排除的路径
     */
    public List<String> excludes;

    /**
     * 需要排除的文件路径模式
     */
    public List<String> excludePatterns;

    /**
     * 输出的格式,例如zip, tgz, 仅为最外层的压缩格式
     */
    public String main;

    /**
     * 是否详细输出
     */
    public boolean debug;

    /**
     * 仅模拟运行,不输出最终的压缩包
     */
    public boolean dry;

    public List<String> prevs;

    // 支撑运行所需要的对象
    public transient WnSystem sys;
    public transient WnSession se;
    public transient Log log;
    public transient List<BackupPackage> prevPackages;
    public transient Set<String> sha1Set;
    public transient List<Pattern> _includePatterns = new ArrayList<>();
    public transient List<Pattern> _excludePatterns = new ArrayList<>();
}
