package org.nutz.walnut.impl.box.cmd;

import java.io.BufferedInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class cmd_zip extends JvmExecutor {

    private Log log = Logs.get();

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "frd", "^(list|hide)$");
        if (params.vals.length < 2) {
            throw Err.create("e.cmd.zip.miss_args");
        }

        String dest = params.vals[0];
        String[] srcArr = new String[params.vals.length - 1];
        for (int i = 1; i < params.vals.length; i++) {
            srcArr[i - 1] = params.vals[i];
        }

        String destPath = Wn.normalizeFullPath(dest, sys);
        WnObj destObj = sys.io.fetch(null, destPath);
        if (destObj != null && !params.is("f")) {
            throw Err.create("e.cmd.zip.dest.exist");
        }

        if (destObj != null && destObj.isDIR()) {
            throw Err.create("e.cmd.zip.dest.isdir");
        }

        // 目标文件
        destObj = sys.io.createIfNoExists(null, destPath, WnRace.FILE);

        // 计算要列出的要处理的对象
        List<WnZipEntry> ens = new ArrayList<>();
        List<WnObj> srclist = new ArrayList<WnObj>();
        if (params.has("match")) {
            WnPager wp = new WnPager(params);
            for (String srcPath : srcArr) {
                String dirPath = Wn.normalizeFullPath(srcPath, sys);
                WnObj dirObj = sys.io.fetch(null, dirPath);
                srclist.addAll(queryByMatch(sys, params, dirObj, wp));
            }
        } else {
            if (params.is("r")) {
                for (String path : srcArr) {
                    String sourcePath = Wn.normalizeFullPath(path, sys);
                    WnObj wobj = sys.io.check(null, sourcePath);
                    if (wobj.isFILE()) {
                        ens.add(new WnZipEntry(wobj, wobj));
                    } else {
                        sys.io.walk(wobj, new Callback<WnObj>() {
                            public void invoke(WnObj obj) {
                                // 无视隐藏文件
                                if (params.is("hide"))
                                    if (obj.isHidden())
                                        Lang.Continue();
                                ens.add(new WnZipEntry(obj, wobj));
                            }
                        }, WalkMode.BREADTH_FIRST);
                    }
                }
            } else {
                srclist.addAll(Cmds.evalCandidateObjsNoEmpty(sys, srcArr, 0));
            }
        }
        if (params.is("list")) {
            for (int i = 0; i < srclist.size(); i++) {
                WnObj srcObj = srclist.get(i);
                sys.out.printlnf("%s", srcObj.name());
            }
        } else {
            try (OutputStream out = sys.io.getOutputStream(destObj, 0);) {
                ZipOutputStream zipOut = new ZipOutputStream(out);
                String entryName = null;
                NoFlushOutputStream proxyOut = new NoFlushOutputStream(zipOut);
                for (WnObj srcObj : srclist) {
                    try {
                        if (srcObj.isDIR()) {
                            continue;
                        }
                        if (destObj.id().equals(srcObj.id())) {
                            continue;
                        }
                        entryName = srcObj.name();
                        // 创建Zip条目
                        ZipEntry entry = new ZipEntry(entryName);
                        zipOut.putNextEntry(entry);

                        try (BufferedInputStream bis = new BufferedInputStream(sys.io.getInputStream(srcObj,
                                                                                                     0))) {
                            Streams.write(proxyOut, bis);
                        }
                        ;
                        zipOut.closeEntry();
                    }
                    catch (Throwable e) {
                        sys.out.print("error at " + srcObj.name());
                    }
                }
                for (WnZipEntry wzen : ens) {
                    if (params.is("d"))
                        sys.out.printlnf("add: %s from %s", wzen.getName(), wzen.wobj.path());
                    ZipEntry entry = new ZipEntry(wzen.getName());
                    zipOut.putNextEntry(entry);
                    if (!wzen.wobj.isDIR()) {
                        try (BufferedInputStream bis = new BufferedInputStream(sys.io.getInputStream(wzen.wobj,
                                                                                                     0))) {
                            Streams.write(proxyOut, bis);
                        }
                        ;
                    }
                    zipOut.closeEntry();
                }
                zipOut.flush();
                zipOut.finish();
            }
            catch (Throwable e) {
                log.error(e.getMessage(), e);
                throw Err.create(e, "e.cmd.zip.error");
            }
        }
    }

    private List<WnObj> queryByMatch(WnSystem sys, ZParams params, WnObj oP, WnPager wp) {
        String json = params.get("match", "{}");
        WnQuery q = new WnQuery();
        // 条件是"或"
        if (Strings.isQuoteBy(json, '[', ']')) {
            List<NutMap> ors = Json.fromJsonAsList(NutMap.class, json);
            q.addAll(ors);
        }
        // 条件是"与"
        else {
            q.add(Lang.map(json));
        }

        // 如果指定了父对象 ...
        if (null != oP)
            q.setv("pid", oP.id());

        q.setv("d0", "home").setv("d1", sys.getMyGroup());

        // 最大不能超过10w
        if (wp.limit <= 0) {
            wp.limit = Math.max(wp.limit, 100000);
        }

        // 设置分页信息
        wp.setupQuery(sys, q);

        return sys.io.query(q);
    }

    public static class WnZipEntry {
        public WnObj wobj;
        public WnObj root;

        public WnZipEntry() {}

        public WnZipEntry(WnObj wobj, WnObj root) {
            this.wobj = wobj;
            this.root = root;
        }

        public String getName() {
            if (wobj == root)
                return wobj.name();
            return wobj.path().substring(root.path().length() + 1) + (wobj.isDIR() ? "/" : "");
        }
    }

    public static class NoFlushOutputStream extends FilterOutputStream {

        public NoFlushOutputStream(OutputStream out) {
            super(out);
        }

        public void flush() throws IOException {
            // nop
        }
    }
}
