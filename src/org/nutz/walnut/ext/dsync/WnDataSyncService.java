package org.nutz.walnut.ext.dsync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.dsync.bean.WnDataSyncConfig;
import org.nutz.walnut.ext.dsync.bean.WnDataSyncDir;
import org.nutz.walnut.ext.dsync.bean.WnDataSyncItem;
import org.nutz.walnut.ext.dsync.bean.WnDataSyncTree;
import org.nutz.walnut.ext.dsync.bean.WnRestoreAction;
import org.nutz.walnut.ext.dsync.bean.WnRestoring;
import org.nutz.walnut.ext.dsync.bean.WnRestoreSettings;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.Wpath;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.archive.WnArchiveSummary;
import org.nutz.walnut.util.archive.WnArchiveWriting;
import org.nutz.walnut.validate.impl.AutoMatch;

public class WnDataSyncService {

    private WnIo io;

    private NutBean vars;

    public WnDataSyncService(WnSystem sys) {
        this(sys.io, sys.session);
    }

    public WnDataSyncService(WnIo io, WnAuthSession session) {
        this.io = io;
        this.vars = session.getVars();
    }

    public WnDataSyncService(WnIo io, NutBean vars) {
        this.io = io;
        this.vars = vars;
    }

    public String getPackageName(WnDataSyncConfig config, List<WnDataSyncTree> trees) {
        String sha1 = null;
        if (null != trees && !trees.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (WnDataSyncTree tree : trees) {
                sb.append(tree.getTreeObj().sha1());
            }
            sha1 = Lang.sha1(sb);
        }
        return getPackageName(config, sha1);
    }

    public String getPackageName(WnDataSyncConfig config, String sha1) {
        String majorName;
        if (null == sha1) {
            majorName = config.getName();
        }
        // 根据树的指纹，拼合一个总指纹
        else {
            majorName = config.getName() + "-" + sha1;
        }
        String suffixName = Ws.sBlank(config.getArchiveType(), "zip");
        return majorName + "." + suffixName;
    }

    /**
     * 根据索引树建立 ID 与路径的映射表。键是 ID，值是 Path
     * 
     * @param trees
     *            索引树
     * @return 映射表
     */
    public Map<String, String> buildIdPathMapping(List<WnDataSyncTree> trees) {
        Map<String, String> map = new HashMap<>();
        for (WnDataSyncTree tree : trees) {
            tree.joinIdPathMap(map);
        }
        return map;
    }

    public WnArchiveSummary restore(WnDataSyncConfig config,
                                    List<WnDataSyncTree> trees,
                                    WnRestoreSettings settings,
                                    WnOutputable log) {
        WnContext wc = Wn.WC();
        boolean so = wc.isSynctimeOff();

        // 建立一个 ID 与路径的映射表
        Map<String, String> idPaths = buildIdPathMapping(trees);

        // 准备一个缓存吧
        Map<String, WnObj> cachePathObj = new HashMap<>();

        // 准备返回值
        WnArchiveSummary sum = new WnArchiveSummary();
        try {
            // 关闭同步时间戳
            wc.setSynctimeOff(true);

            // 准备数据缓存主目录
            String aph = Wn.normalizeFullPath("~/.dsync/data/", vars);
            WnObj oDataHome = io.fetch(null, aph);

            // 循环上下文的树
            for (WnDataSyncTree tree : trees) {
                if (null != log) {
                    log.printlnf("RESTORE TREE[%s]:", tree.getName());
                }
                if (!tree.hasItems()) {
                    log.println(" ~ no items ~");
                    continue;
                }

                // 逐项还原
                for (WnDataSyncItem item : tree.getItems()) {
                    // 看看有木有对象
                    aph = Wn.normalizeFullPath(item.getPath(), vars);
                    WnObj o = io.fetch(null, aph);
                    String op = "=";
                    if (null == o) {
                        op = "+";
                        o = io.create(null, aph, item.getRace());
                    }

                    // 准备后续的命令
                    List<WnRestoreAction> actions = new LinkedList<>();

                    // 恢复元数据
                    NutBean meta = item.getMeta();
                    Object amo = meta;
                    if (null == meta || meta.isEmpty()) {
                        amo = false;
                    }
                    AutoMatch am = new AutoMatch(amo);
                    if (settings.force || (null != am && !am.match(o))) {
                        op += "M";
                        io.appendMeta(o, meta);
                    } else {
                        op += "-";
                    }

                    // 恢复内容
                    if (item.isFile()) {
                        String itSha1 = item.getSha1();
                        if (settings.force || !o.isSameSha1(itSha1)) {
                            op += "W";
                            // 得到缓存数据
                            WnObj oData = null;
                            if (null != oDataHome) {
                                oData = io.fetch(oDataHome, itSha1);
                            }
                            if (null != oData) {
                                io.copyData(oData, o);
                                config.joinRestoreActions(actions, o);
                            }
                        } else {
                            op += "f";
                        }
                    } else {
                        op += "d";
                    }

                    // 打印日志
                    if (null != log) {
                        log.printlnf(" %s> %s", op, item.toString());
                    }

                    // 执行后续命令
                    WnRestoring ing = new WnRestoring();
                    ing.io = io;
                    ing.vars = vars;
                    ing.obj = o;
                    ing.idPaths = idPaths;
                    ing.log = log;
                    ing.run = settings.run;
                    ing.actions = actions;
                    ing.cachePathObj = cachePathObj;
                    ing.invoke();
                }
            }
            // 恢复时间同步
        }
        finally {
            wc.setSynctimeOff(so);
        }
        return sum;
    }

    public WnObj genArchive(WnDataSyncConfig config,
                            List<WnDataSyncTree> trees,
                            boolean force,
                            WnOutputable log) {
        // String rph = String.format("~/.dsync/%s/", config.getName());
        // String aph = Wn.normalizeFullPath(rph, vars);
        // WnObj oSyncHome = io.createIfNoExists(null, aph, WnRace.DIR);

        String zipName = this.getPackageName(config, trees);
        String rph = String.format("~/.dsync/pkg/%s", zipName);
        String aph = Wn.normalizeFullPath(rph, vars);
        String zipPath = Wn.normalizeFullPath(aph, vars);
        WnObj oZip = io.fetch(null, zipPath);

        if (!force && null != oZip)
            return oZip;

        // 生成压缩包
        if (null == oZip) {
            oZip = io.createIfNoExists(null, zipPath, WnRace.FILE);
        }

        InputStream ins;
        OutputStream ops = null;
        WnArchiveWriting ag = null;

        if (null != log) {
            log.printlnf("Gen zip: %s", oZip.toString());
        }

        // 生成压缩包
        try {
            // 准备输出流
            ops = io.getOutputStream(oZip, 0);
            // File tmp = Files.createFileIfNoExists2("D:/tmp/zip/wntest.zip");
            // ops = Streams.fileOut(tmp);
            ag = config.createArchiveGenerating(ops);

            // 准备包里的两个目录

            // 阻止重复出现的 sha1
            Map<String, Boolean> sha1s = new HashMap<>();

            // 逐个处理每一颗树
            String enName;
            for (WnDataSyncTree tree : trees) {
                // 加入索引文件
                WnObj oTree = tree.getTreeObj();
                enName = oTree.name();
                if (null != log) {
                    log.printlnf(" + %s <- %s", enName, oTree.toString());
                }
                ins = io.getInputStream(oTree, 0);
                ag.addFileEntry(enName, ins, oTree.len());

                // 元数据文件
                WnObj oMeta = tree.getMetaObj();
                enName = "meta/" + oMeta.name();
                if (null != log) {
                    log.printlnf(" + %s <- %s", enName, oMeta.toString());
                }
                ins = io.getInputStream(oMeta, 0);
                ag.addFileEntry(enName, ins, oMeta.len());

                // 文件内容
                if (tree.hasItems()) {
                    for (WnDataSyncItem item : tree.getItems()) {
                        if (item.isFile() && item.hasSha1()) {
                            WnObj o = item.getObj();
                            String sha1 = o.sha1();
                            // 防空
                            if (Ws.isBlank(sha1)) {
                                continue;
                            }

                            // 防守：不要重复输出 SHA1
                            if (sha1s.containsKey(sha1)) {
                                continue;
                            }
                            sha1s.put(sha1, true);

                            enName = "data/" + sha1;
                            if (null != log) {
                                log.printlnf(" + %s <- %s", enName, o.toString());
                            }
                            ins = io.getInputStream(o, 0);
                            ag.addFileEntry(enName, ins, o.len());
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        finally {
            Streams.safeFlush(ag);
            Streams.safeClose(ag);
            Streams.safeClose(ops);
        }

        // 搞定
        return oZip;
    }

    public List<WnDataSyncTree> loadTrees(WnDataSyncConfig config, String sha1) {
        // 防守
        if (!config.hasDirs()) {
            return new LinkedList<>();
        }

        // 初始化返回只
        int dCount = config.countDirs();
        List<WnDataSyncTree> trees = new ArrayList<>(dCount);
        String aph;

        // 找到树的存储目录，如果不指定版本(SHA1)，那么就用 head
        String headBase;
        if (Ws.isBlank(sha1)) {
            headBase = "~/.dsync/head/" + config.getName() + "/";
        } else {
            headBase = "~/.dsync/cache/" + config.getName() + "/" + sha1 + "/";
        }

        // 逐个读取详情
        for (int i = 0; i < dCount; i++) {
            // 得到当前文件的配置项
            WnDataSyncDir dir = config.getDirs().get(i);

            String confName = config.getName();
            String key = dir.getKey();

            // 得到树的缓存文件
            String rph = headBase + dir.getKey() + ".tree";
            aph = Wn.normalizeFullPath(rph, vars);
            WnObj oTree = io.fetch(null, aph);
            // 木有树 ... 尴尬了
            if (null == oTree) {
                throw Er.createf("e.dsync.loadTree", rph);
            }
            // 读取树的内容
            WnDataSyncTree tree = new WnDataSyncTree();
            tree.load(io, oTree);
            tree.setName(confName, key);

            // 得到元数据缓存文件
            rph = headBase + "meta/" + key + ".json";
            String metaPath = Wn.normalizeFullPath(rph, vars);
            WnObj oMetas = io.fetch(null, metaPath);
            tree.setMetaObj(oMetas);

            // 读取元数据文件
            NutMap metas = io.readJson(oMetas, NutMap.class);
            tree.setDataSyncTime(metas.getLong("dsync_t", 0));

            // 为每个树项恢复元数据
            if (tree.hasItems()) {
                for (WnDataSyncItem item : tree.getItems()) {
                    NutMap bean = metas.getAs(item.getBeanSha1(), NutMap.class);
                    item.setBean(bean);

                    NutMap meta = metas.getAs(item.getMetaSha1(), NutMap.class);
                    item.setMeta(meta);
                }
            }

            // 记入
            trees.add(tree);
        }

        // 搞定
        return trees;
    }

    public List<WnDataSyncTree> genTrees(WnDataSyncConfig config, boolean force) {
        // 防守
        if (!config.hasDirs()) {
            return new LinkedList<>();
        }

        // 初始化返回值
        int dCount = config.countDirs();
        List<WnDataSyncTree> trees = new ArrayList<>(dCount);
        String aph;
        String headBase = "~/.dsync/head/" + config.getName() + "/";

        for (int i = 0; i < dCount; i++) {
            // 得到当前文件的配置项
            WnDataSyncDir dir = config.getDirs().get(i);

            String confName = config.getName();
            String key = dir.getKey();

            // 得到树的缓存文件
            String rph = headBase + dir.getKey() + ".tree";
            aph = Wn.normalizeFullPath(rph, vars);
            WnObj oTree = io.createIfNoExists(null, aph, WnRace.FILE);
            // 确保内容类型
            if (!oTree.isMime("text/plain")) {
                io.appendMeta(oTree, Lang.map("mime", "text/plain"));
            }

            // 得到元数据缓存文件
            rph = headBase + "meta/" + key + ".json";
            String metaPath = Wn.normalizeFullPath(rph, vars);
            WnObj oMetas = io.fetch(null, metaPath);

            // 得到目标要同步的目录
            aph = Wn.normalizeFullPath(dir.getPath(), vars);
            WnObj oDir = io.check(null, aph);
            // 确保同步目录标识了同步时间戳
            if (oDir.syncTime() <= 0) {
                io.appendMeta(oDir, Lang.map("synt", oDir.lastModified()));
            }
            long synt = oDir.syncTime();

            //
            // 准备读取树了
            //
            WnDataSyncTree tree;

            // 确定需要同步
            if (force || null == oMetas || oTree.getLong("dsync_t") != synt) {
                // 确保创建元数据文件
                if (null == oMetas) {
                    oMetas = io.createIfNoExists(null, metaPath, WnRace.FILE);
                }

                tree = this.buildTree(oDir, dir);
                tree.setDataSyncTime(synt);
                io.appendMeta(oTree, Lang.map("dsync_t", synt));

                // 记入缓存
                String s0 = tree.toContentString();
                io.writeText(oTree, s0);

                String s1 = tree.toMetaString(null);
                io.writeText(oMetas, s1);
            }
            // 读取内容
            else {
                tree = new WnDataSyncTree();
                tree.load(io, oTree);
            }

            // 记入
            tree.setTreeObj(oTree);
            tree.setMetaObj(oMetas);
            tree.setName(confName, key);
            trees.add(tree);
        }

        // 搞定
        return trees;
    }

    public void loadTreeItems(WnDataSyncTree tree) {
        if (null != tree && tree.hasItems()) {
            for (WnDataSyncItem item : tree.getItems()) {
                if (item.hasObj())
                    continue;
                String aph = Wn.normalizeFullPath(item.getPath(), vars);
                WnObj obj = io.fetch(null, aph);
                item.setObj(obj);
            }
        }
    }

    public void loadTreesItems(WnDataSyncTree... trees) {
        for (WnDataSyncTree tree : trees) {
            loadTreeItems(tree);
        }
    }

    public void loadTreesItems(List<WnDataSyncTree> trees) {
        for (WnDataSyncTree tree : trees) {
            loadTreeItems(tree);
        }
    }

    public WnDataSyncTree buildTree(WnObj oDir, WnDataSyncDir dir) {
        String homePath = vars.getString("HOME");
        if (Ws.isBlank(homePath)) {
            throw Er.create("e.dsync.var_without_home");
        }
        // 目录一定要以 "/" 结尾
        if (!homePath.endsWith("/")) {
            homePath += "/";
        }
        // 确保不超过主目录范围
        if (!oDir.path().startsWith(homePath)) {
            throw Er.create("e.dsync.dir_out_of_home", oDir.path() + " <-> " + homePath);
        }

        // 准备回调
        WnDataSyncTree tree = new WnDataSyncTree();
        tree.setDataSyncTime(oDir.syncTime());
        String HOME_PATH = homePath;
        Callback<WnObj> callback = new Callback<WnObj>() {
            public void invoke(WnObj o) {
                // 无视顶层文件
                if (dir.isIgnoreTop() && o.isSameId(oDir)) {
                    return;
                }
                // 无视隐藏文件夹
                if (dir.isIgnoreHidden() && o.isHidden()) {
                    return;
                }
                // 准备
                WnDataSyncItem dsi = new WnDataSyncItem(o, HOME_PATH);
                tree.addItem(dsi);
            }
        };

        // 准备遍历模式
        WalkMode mode = dir.isLeafOnly() ? WalkMode.LEAF_ONLY : WalkMode.DEPTH_NODE_FIRST;

        // 来吧
        io.walk(oDir, callback, mode, o -> {
            // 无视隐藏文件夹
            if (dir.isIgnoreHidden() && o.isHidden()) {
                return false;
            }
            // 无视数据集
            if (dir.isIgnoreThingSet() && o.isType("thing_set")) {
                return false;
            }

            return true;
        });

        // 搞定
        return tree;
    }

    public WnDataSyncConfig loadConfig(String name) {
        name = Ws.sBlank(name, "daync");
        if (!name.endsWith(".json")) {
            name += ".json";
        }
        String aph = Wn.normalizeFullPath("~/.dsync/" + name, vars);
        WnObj oConf = io.check(null, aph);
        WnDataSyncConfig conf = io.readJson(oConf, WnDataSyncConfig.class);
        conf.checkDirKeys();

        String configName = Wpath.getMajorName(name);
        conf.setName(configName);
        return conf;
    }

}
