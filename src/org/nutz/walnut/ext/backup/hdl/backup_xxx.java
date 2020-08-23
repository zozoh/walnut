package org.nutz.walnut.ext.backup.hdl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.sshd.common.util.io.NullOutputStream;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.FileVisitor;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.backup.BackupDumpContext;
import org.nutz.walnut.ext.backup.BackupPackage;
import org.nutz.walnut.ext.backup.BackupRestoreContext;
import org.nutz.walnut.ext.backup.WobjLine;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.util.Wn;

public abstract class backup_xxx {

    public void dump(BackupDumpContext ctx) {
        Log log = ctx.log;
        WnIo io = ctx.sys.io;

        // 输出一下上下文,如果是debug的话
        if (log.isDebugEnabled())
            log.debug(ctx);
        // 创建临时文件夹
        File tmpDir = new File(System.getProperty("java.io.tmpdir") + "/walnut/dump/" + R.UU32());
        log.debugf("tmp dir : %s", tmpDir.getAbsolutePath());
        tmpDir.mkdirs();
        ctx.tmpdir = tmpDir;
        // 记录文件数量
        int[] count = new int[1];
        // 记录数据总量
        long[] fdata_sum = new long[1];
        List<ZipFile> zipHolder = new ArrayList<>();
        try {
            // 首先,准备一个临时文件,用于存放对象列表
            try (FileWriter fw_objs = new FileWriter(new File(tmpDir, "objs.txt"))) {
                // 缓存sha1列表
                for (String path : ctx.includes) {
                    io.walk(io.check(null, Wn.normalizeFullPath(path, ctx.sys)), wobj -> {
                        dump_walk(count, fdata_sum, wobj, fw_objs, ctx);
                    }, WalkMode.DEPTH_LEAF_FIRST);
                }
                fw_objs.flush();
            }
            // 前面刷屏了,这里再输出一下
            log.debugf("exp tmp dir : %s", tmpDir.getAbsolutePath());
            log.debugf("wobj count=%d", count[0]);
            log.debugf("sha1 count=%d", ctx.sha1Set.size());
            log.debugf("fdata  sum=%s", fdata_sum[0] / 1024 + "kb");

            // 压缩之
            String dst = Wn.normalizeFullPath(ctx.dst, ctx.sys);
            log.info("dst path = " + dst);
            if (ctx.dry) {
                try (OutputStream out = new NullOutputStream();) {
                    dump_write_zip(out, tmpDir, log);
                }
            } else {
                WnObj dstWnObj = io.createIfNoExists(null, dst, WnRace.FILE);
                if (io.exists(null, Wn.normalizeFullPath("~/.dump/to_local", ctx.se))) {
                    try (OutputStream out = new FileOutputStream("/data/dump/" + R.UU32() + ".zip")) {
                        dump_write_zip(out, tmpDir, log);
                    }
                }
                else {
                    try (OutputStream out = io.getOutputStream(dstWnObj, 0)) {
                        dump_write_zip(out, tmpDir, log);
                    }
                }
                io.appendMeta(dstWnObj, new NutMap("backup_config", ctx));
            }
        }
        catch (IOException e) {
            log.warn("write something fail", e);
        }
        finally {
            if (!ctx.keepTemp)
                Files.deleteDir(tmpDir);
            for (ZipFile zip : zipHolder) {
                Streams.safeClose(zip);
            }
        }

    }

    public void dump_walk(int[] count,
                          long[] fdata_sum,
                          WnObj wobj,
                          FileWriter fw_objs,
                          BackupDumpContext ctx) {
        try {
            String wobj_path = wobj.path();
            // 过滤一下肯定不需要的文件
            if (wobj_path.contains("/.dump/")) {
                return;
            }
            // 只包含特定模式的路径?
            if (!ctx._includePatterns.isEmpty()) {
                for (Pattern pattern : ctx._includePatterns) {
                    if (!pattern.matcher(wobj_path).find()) {
                        ctx.log.debugf("++ skip : %s", wobj_path);
                        return;
                    }
                }
            }
            // 排除特定路径?
            if (ctx.excludes != null && !ctx.excludes.isEmpty()) {
                for (String exclude : ctx.excludes) {
                    if (wobj_path.startsWith(exclude)) {
                        ctx.log.debugf("++ skip : %s", wobj_path);
                        return;
                    }
                }
            }
            // 排除特定的路径模式?
            if (!ctx._excludePatterns.isEmpty()) {
                for (Pattern pattern : ctx._excludePatterns) {
                    if (pattern.matcher(wobj_path).find()) {
                        ctx.log.debugf("++ skip : %s", wobj_path);
                        return;
                    }
                }
            }
            // 挂载类的也不需要
            if (wobj.mount() != null && wobj.data() != null) {
                ctx.log.debugf("++ skip : %s", wobj_path);
                return;
            }
            ctx.log.debugf("++ add : %s", wobj_path);
            count[0]++;
            // 仅需要输出文件的sha1
            if (wobj.isFILE() && wobj.sha1() != null) {
                // 只有文件的sha1是有用的
                boolean re = ctx.sha1Set.add(wobj.sha1());
                if (re) {
                    BackupPackage pkg = searchSha1(wobj.sha1(), null, ctx.prevPackages);
                    if (null == pkg) {
                        String path = wobj.sha1().substring(0, 2) + "/" + wobj.sha1().substring(2);
                        File tmp = new File(ctx.tmpdir, "bucket/" + path);
                        Files.createFileIfNoExists(tmp);
                        try (FileOutputStream out = new FileOutputStream(tmp)) {
                            ctx.sys.io.readAndClose(wobj, out);
                        }
                        catch (Exception e) {
                            ctx.log.warn("write bucket failed", e);
                        }
                        fdata_sum[0] += tmp.length();
                    } else {
                        ctx.log.debugf("sha1[%s] exists in Prev Package", wobj.sha1());
                    }
                }
            }
            // 算出sha1
            String obj_str = Json.toJson(wobj, JsonFormat.full());
            String o_sha1 = Lang.sha1(obj_str);
            File f = new File(ctx.tmpdir,
                              "objs/" + o_sha1.substring(0, 2) + "/" + o_sha1.substring(2));
            if (f.exists() && f.length() > 0) {
                // nop
            } else {
                Files.write(f, obj_str);
            }
            // 格式 $id:$path:$obj_sha1:$data_sha1
            String line = new WobjLine(wobj.id(),
                                        wobj.path().substring(ctx.base.length()),
                                        o_sha1,
                                        Strings.sBlank(wobj.sha1())).toString() + "\r\n";
            fw_objs.write(line);
        }
        catch (IOException e) {
            ctx.log.warn("write something fail", e);
        }
    }

    public void dump_write_zip(OutputStream out, File tmpDir, Log log) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(out, Encoding.CHARSET_UTF8);
        zos.setLevel(1); // 快速压缩
        dump_zip_add(zos, new File(tmpDir, "objs.txt"), log, tmpDir);
        Disks.visitFile(new File(tmpDir, "objs"), new FileVisitor() {
            public void visit(File file) {
                dump_zip_add(zos, file, log, tmpDir);
            }
        }, null);
        Disks.visitFile(new File(tmpDir, "bucket"), new FileVisitor() {
            public void visit(File file) {
                dump_zip_add(zos, file, log, tmpDir);
            }
        }, null);
        zos.flush();
        zos.finish();
        zos.close();
    }

    public void dump_zip_add(ZipOutputStream zos, File file, Log log, File tmpDir) {
        if (file.isDirectory())
            return;
        String name = Disks.getRelativePath(tmpDir, file);
        log.debugf("add to zip : %s", name);
        try {
            ZipEntry en = new ZipEntry(name);
            zos.putNextEntry(en);
            try (FileInputStream fis = new FileInputStream(file)) {
                Streams.write(zos, fis);
            }
            zos.closeEntry();
        }
        catch (IOException e) {
            log.warn("fail at " + name, e);
        }
    }

    public void restore(BackupRestoreContext ctx) {
        // 首先,创建一个ZipFile的列表,用于持有所有已经创建的Zip对象
        WnSystem sys = ctx.sys;
        Log log = ctx.log;
        WnIo io = sys.io;
        try {
            BackupPackage main = readBackupPackage(io, ctx.main, true);
            WnObj pkgWobj = sys.io.check(null, ctx.main);
            // 现在, 如果id需要强制还原的话, 要先检查一下id是否合法
            if (ctx.force_id) {
                boolean flag = false;
                for (WobjLine wobj : main.lines) {
                    WnObj sys_wobj = io.get(wobj.id);
                    if (sys_wobj != null && !wobj.path.equals(sys_wobj.path())) {
                        log.errorf("id[%s] is exist and path isn't same!! mypath=%s syspath=%s",
                                   wobj.id,
                                   wobj.path,
                                   sys_wobj.path());
                        flag = true;
                    }
                }
                if (flag) {
                    log.error("error found, exit");
                    return;
                }
            }
            // 然后, 检查一下是不是所有sha1都能找到.
            Map<String, BackupPackage> sha1Map = new HashMap<>();
            // 因为要支持增量备份,所有需要把历史备份包也加载一下
            boolean flag = false;
            for (WobjLine wobjLine : main.lines) {
                if (wobjLine.fdata_sha1 == null)
                    continue;
                log.debugf("checking sha1 = %s", wobjLine.fdata_sha1);
                BackupPackage pkg = searchSha1(wobjLine.fdata_sha1, main, ctx.prevPackages);
                if (pkg == null) {
                    flag = true;
                    log.error("miss content sha1= " + wobjLine.fdata_sha1);
                } else {
                    sha1Map.put(wobjLine.fdata_sha1, pkg);
                }
            }
            if (flag) {
                if (ctx.ignore_sha1_miss)
                    log.warn("some sha1 is missing, but ignore it.");
                else {
                    log.error("some sha1 is missing, exit!!!");
                    return;
                }
            }
            // 全部ok? 排序, 目录在前, 文件在后
            main.objs.sort(new Comparator<WnObj>() {
                public int compare(WnObj o1, WnObj o2) {
                    if (o1.race() == o2.race())
                        return o1.path().compareTo(o2.path());
                    if (o1.isDIR()) // 那o2肯定不是DIR
                        return -1;
                    if (o2.isDIR())
                        return 1;
                    return 0; // 都是File或者Link, 无所谓了
                }
            });
            // 遍历文件夹, 先把文件夹统统建起了
            NutMap backup_config = pkgWobj.getAs("backup_config", NutMap.class);
            if (backup_config == null) {
                backup_config = new NutMap();
            }
            String base = ctx.base;
            if (Strings.isBlank(base))
                base = backup_config.getString("base", "");
            for (WnObj wobj : main.objs) {
                String sha1 = wobj.sha1();
                // 原本的路径
                String originPath = wobj.path();
                // 相对路径
                String rpath = originPath.substring(base.length());
                String dstPath = ctx.target + rpath;
                WnObj dstWnObj = io.fetch(null, dstPath);
                // 确保父文件夹存在
                WnObj parent_wobj = null;
                if (dstPath.lastIndexOf('/') != 0) {
                    String ppath = dstPath.substring(0, dstPath.lastIndexOf('/'));
                    parent_wobj = io.createIfNoExists(null, ppath, WnRace.DIR);
                }
                // 然后,看看id是否要恢复
                if (ctx.force_id) {
                    if (dstWnObj != null && !dstWnObj.isDIR()) {
                        io.delete(dstWnObj);
                        dstWnObj = null;
                    }
                    if (dstWnObj == null) {
                        log.debugf("create by id   : %s -> %s", rpath, dstPath);
                        dstWnObj = io.createById(parent_wobj, wobj.id(), wobj.name(), wobj.race());
                    }
                } else {
                    if (dstWnObj == null) {
                        log.debugf("create by path : %s -> %s", rpath, dstPath);
                        dstWnObj = io.createIfNoExists(null, dstPath, wobj.race());
                    }
                }
                if (wobj.isFILE()) {
                    // 写入数据
                    BackupPackage pkg = sha1Map.get(sha1);
                    if (pkg == null) {
                        log.warnf("miss data : %s  : %s", rpath, wobj.sha1());
                    }
                    else {
                        log.debugf("restore data   : %s : %s", rpath, wobj.sha1());
                        if (!readAndWrite(pkg, sha1, io, dstWnObj, log)) {
                            log.warnf("miss data   : %s : %s", rpath, wobj.sha1());
                        }
                    }
                }
                log.debugf("restore meta   : %s -> %s", rpath, dstPath);
                wobj.clearRWMetaKeys();
                io.appendMeta(dstWnObj, Lang.filter(wobj, null, null, "^(id|race|d0|d1|nm|pid|ph|sha1|data|c|m|g)$", null));
            }
        }
        catch (Exception e) {
            log.warn("something happen", e);
        }
    }

    public static BackupPackage searchSha1(String sha1,
                                           BackupPackage main,
                                           List<BackupPackage> prevs) {
        if (main != null && main.sha1Set.contains(sha1))
            return main;
        for (BackupPackage prev : prevs) {
            if (prev.sha1Set.contains(sha1))
                return prev;
        }
        return null;
    }
    
    public static BackupPackage _readBackupPackage(InputStream ins, boolean readObjs) throws IOException {
        ZipInputStream zis = new ZipInputStream(ins, Encoding.CHARSET_UTF8);
        BackupPackage bzip = new BackupPackage();
        ZipEntry en;
        long time_for_expired = Wn.now() + 30000;
        while (true) {
            en = zis.getNextEntry();
            if (en == null)
                break;
            if (en.isDirectory())
                continue;
            if ("objs.txt".equals(en.getName())) {
                BufferedReader br = new BufferedReader(new InputStreamReader(zis,
                                                                             Encoding.CHARSET_UTF8));
                while (true) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    if (line.trim().isEmpty())
                        continue;
                    WobjLine wline = new WobjLine(line);
                    bzip.lines.add(wline);
                    if (wline.fdata_sha1 != null)
                        bzip.sha1Set.add(wline.fdata_sha1);
                }
            } else if (readObjs && en.getName().startsWith("objs/")) {
                WnObj wobj = Json.fromJson(WnBean.class, new InputStreamReader(zis, Encoding.CHARSET_UTF8));
                if (wobj.isExpiredBy(time_for_expired))
                    continue;
                bzip.objs.add(wobj);
            } else {
                break;
            }
        }
        return bzip;
    }

    public static BackupPackage readBackupPackage(WnIo io, String path, boolean readObjs) {
        WnObj self = io.check(null, path);
        try (InputStream ins = io.getInputStream(self, 0)) {
            BackupPackage pkg = _readBackupPackage(ins, readObjs);
            pkg.self = self;
            return pkg;
        } catch (Exception e) {
            throw Lang.wrapThrow(e);
        }
    }
    
    public static boolean readAndWrite(BackupPackage pkg, String sha1, WnIo io, WnObj wobj, Log log) {
        if (sha1.equals(wobj.sha1()))
            return true;
        File tmp = checkZipTmpFile(pkg.self, io, log);
        try (ZipFile zip = new ZipFile(tmp)) {
            String path = "bucket/" + sha1.substring(0, 2) + "/" + sha1.substring(2);
            io.writeAndClose(wobj, zip.getInputStream(zip.getEntry(path)));
            return true;
        } catch (Throwable e) {
            log.warn("something happen!!", e);
        }
        return false;
    }
    
    public static File checkZipTmpFile(WnObj wobj, WnIo io, Log log) {
        File tmp = new File("/tmp/bk/" + wobj.id() + ".zip");
        if (!tmp.exists()) {
            try (InputStream ins = io.getInputStream(wobj, 0)) {
                Files.write(Files.createFileIfNoExists(tmp), ins);
            } catch (Throwable e) {
                log.warn("something happen!!", e);
            }
            //ctx.tmpFiles.add(tmp.getAbsolutePath());
        }
        return tmp;
    }
}
