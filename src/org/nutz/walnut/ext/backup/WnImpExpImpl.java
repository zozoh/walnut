package org.nutz.walnut.ext.backup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
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
import org.nutz.log.Log;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

@IocBean(name="wnImpExp")
public class WnImpExpImpl implements WnImpExp {
    
    @Inject
    protected WnIo io;

    @SuppressWarnings("resource")
    @Override
    public void exp(String root, ZParams params, Log log, WnSession se) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir")  + "/walnut/dump/" + R.UU32());
        log.info("exp tmp dir : " + tmpDir.getAbsolutePath());
        log.info(">> " + Json.toJson(params));
        tmpDir.mkdirs();
        final FileWriter fw_objs;
        int[] count = new int[1];
        long[] fdata_sum = new long[1];
        List<ZipFile> zipHolder = new ArrayList<>();
        try {
            // 如果指定了老的更新包路径/文件夹, 加载之,用于检索sha1
            List<ZipFile> oldZips = getSecondaryZips(params.get("zips"), zipHolder);
            // 首先,准备一个临时文件,用于存放对象列表
            fw_objs = new FileWriter(new File(tmpDir, "objs.txt"));
            // 缓存sha1列表
            Set<String> sha1Set = new HashSet<>();
            io.walk(io.check(null, root), wobj -> {
                try {
                    // 过滤一下肯定不需要的文件
                	if (wobj.path().contains("/.dump/")) {
                		return;
                	}
                    if (wobj.mount() != null && wobj.data() != null) {
                        log.debug("skip >> " + wobj.path());
                        return;
                    }
                    log.debug("export >> " + wobj.path());
                    count[0] ++;
                    // TODO 过滤已过期/快过期的文件?
                    if (wobj.isFILE() && wobj.sha1() != null) {
                        // 只有文件的sha1是有用的
                        boolean re = sha1Set.add(wobj.sha1());
                        if (re) {
                            ZipFile szip = searchSha1(wobj.sha1(), null, oldZips);
                            if (null == szip) {
                                String path = wobj.sha1().substring(0, 2) + "/" + wobj.sha1().substring(2);
                                File tmp = new File(tmpDir, "bucket/" + path);
                                Files.createFileIfNoExists(tmp);
                                try (FileOutputStream out = new FileOutputStream(tmp)) {
                                    io.readAndClose(wobj, out);
                                }
                                catch (Exception e) {
                                    log.warn("write bucket failed", e);
                                }
                                fdata_sum[0] += tmp.length();
                            } else {
                                log.debugf("sha1[%s] exists in Secondary Zips", wobj.sha1());
                            }
                        }
                    }
                    // 算出sha1
                    String obj_str = Json.toJson(wobj, JsonFormat.full());
                    String o_sha1 = Lang.sha1(obj_str);
                    File f = new File(tmpDir, "objs/" + o_sha1.substring(0, 2) + "/" + o_sha1.substring(2));
                    if (f.exists() && f.length() > 0) {
                        // nop
                    } else {
                        Files.write(f, obj_str);
                    }
                    // 格式 $id:$path:$obj_sha1:$data_sha1
                    String line = String.format("%s:%s:%s:%s\r\n", wobj.id(), wobj.path(), o_sha1, Strings.sBlank(wobj.sha1()));
                    fw_objs.write(line);
                }
                catch (IOException e) {
                    log.warn("write something fail", e);
                }
            }, WalkMode.DEPTH_LEAF_FIRST);
            // 刷新缓存并关闭objs.txt文件
            fw_objs.flush();
            fw_objs.close();
            // 前面刷屏了,这里再输出一下
            if (log.isDebugEnabled()) {
                log.info("exp tmp dir : " + tmpDir.getAbsolutePath());
            }
            log.info("wobj count=" + count[0]);
            log.info("sha1 count=" + sha1Set.size());
            log.info("fdata sum=" + fdata_sum[0] / 1024  + "kb");
            
            // 压缩之
            String zippath = params.get("dst");
            if (Strings.isBlank(zippath)) {
                zippath = Wn.normalizeFullPath("~/.dump/" + R.UU32() + ".zip", se);
            } else {
            	zippath = Wn.normalizeFullPath(zippath, se);
            }
            log.info("zippath = " + zippath);
            Files.createFileIfNoExists(zippath);
            try (FileOutputStream fos = new FileOutputStream(zippath)) {
                ZipOutputStream zos = new ZipOutputStream(fos, Encoding.CHARSET_UTF8);
                Disks.visitFile(tmpDir, new FileVisitor() {
                    public void visit(File file) {
                        if (file.isDirectory())
                            return;
                        String name = Disks.getRelativePath(tmpDir, file);
                        log.debug("add to zip >> " + name);
                        try {
                            ZipEntry en = new ZipEntry(name);
                            zos.putNextEntry(en);
                            try (FileInputStream fis = new FileInputStream(file)){
                                Streams.write(zos, fis);
                            }
                            zos.closeEntry();
                        }
                        catch (IOException e) {
                            log.warn("fail at " + name, e);
                        }
                    }
                }, null);
                zos.flush();
                zos.finish();
                zos.close();
            }
            // 然后输出校验值
            String sha1 = Lang.sha1(zippath);
            String sha256 = Lang.sha256(zippath);
            Files.write(zippath + ".sha1", sha1);
            Files.write(zippath + ".sha256", sha256);
            log.debug("sha1   : " + sha1);
            log.debug("sha256 : " + sha256);
        }
        catch (IOException e) {
            log.warn("write something fail", e);
        }
        finally {
            if (!params.is("keep"))
                Files.deleteDir(tmpDir);
            for (ZipFile zip : zipHolder) {
                Streams.safeClose(zip);
            }
        }
        
    }

    @Override
    public void imp(String imppath, String root, ZParams params, Log log) {
        // 首先,创建一个ZipFile的列表,用于持有所有已经创建的Zip对象
        List<ZipFile> zipHolder = new ArrayList<>();
        try {
            String zips = params.get("zips");
            ZipFile mainZip = new ZipFile(imppath);
            zipHolder.add(mainZip);
            // 首先, 把objs.txt读进来
            ZipEntry en = mainZip.getEntry("objs.txt");
            if (en == null) {
                log.error("main zip don't have objs.txt");
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(mainZip.getInputStream(en), Encoding.CHARSET_UTF8));
            List<WobjLine> objs = new ArrayList<>();
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                if (line.trim().isEmpty())
                    continue;
                objs.add(new WobjLine(line));
            }
            // 现在, 如果id需要强制还原的话, 要先检查一下id是否合法
            if (params.is("force_id", false)) {
                boolean flag = false;
                for (WobjLine wobj : objs) {
                    WnObj sys_wobj = io.get(wobj.id);
                    if (sys_wobj != null && !wobj.path.equals(sys_wobj.path())) {
                        log.errorf("id[%s] is exist and path isn't same!! mypath=%s syspath=%s", wobj.id, wobj.path, sys_wobj.path());
                        flag = true;
                    }
                }
                if (flag) {
                    log.error("error found, exit");
                }
            }
            // 然后, 检查一下是不是所有sha1都能找到.
            // 因为要支持增量备份,所有需要把历史备份包也加载一下
            List<ZipFile> oldZips = getSecondaryZips(zips, zipHolder);
            boolean flag = false;
            for (WobjLine wobjLine : objs) {
                if (wobjLine.fdata_sha1 == null)
                    continue;
                log.debugf("checking sha1 = %s", wobjLine.fdata_sha1);
                ZipFile zip = searchSha1(wobjLine.fdata_sha1, mainZip, oldZips);
                if (zip == null) {
                    flag = true;
                    log.error("miss content sha1= " + wobjLine.fdata_sha1);
                }
            }
            if (flag) {
                if (params.is("ignore_sha1_miss"))
                    log.warn("some sha1 is missing, but ignore it...");
                else {
                    log.error("some sha1 is missing, exit!!!");
                    return;
                }
            }
            // 全部ok? 读取WnObj对象,逐个还原
            
        }
        catch (Exception e) {
			log.warn("something happen", e);
		}
        finally {
            for (ZipFile zip : zipHolder) {
                Streams.safeClose(zip);
            }
        }
    }
    
    public static ZipFile searchSha1(String sha1, ZipFile mainZip, List<ZipFile> secondaryZips) {
        String key = "bucket/" + sha1.substring(0, 2) + "/" + sha1.substring(2);
        if (mainZip != null && mainZip.getEntry(key) != null)
            return mainZip;
        for (ZipFile zip : secondaryZips) {
            if (zip.getEntry(key) != null)
                return zip;
        }
        return null;
    }

    public static List<ZipFile> getSecondaryZips(String paths, List<ZipFile> zipHolder) throws IOException {
        List<ZipFile> secondaryZips = new ArrayList<>();
        if (paths != null) {
            String[] tmp = Strings.splitIgnoreBlank(paths);
            for (String path : tmp) {
                File f = new File(path);
                if (f.isDirectory()) {
                    for (File f2 : f.listFiles()) {
                        if (f2.isFile() && f2.getName().endsWith(".zip")) {
                            ZipFile zip = new ZipFile(f2);
                            secondaryZips.add(zip);
                            zipHolder.add(zip);
                        }
                    }
                } else {
                    ZipFile zip = new ZipFile(path);
                    secondaryZips.add(zip);
                    zipHolder.add(zip);
                }
            }
        }
        return secondaryZips;
    }
    
    public static class WobjLine {
        public String id;
        public String path;
        public String obj_sha1;
        public String fdata_sha1;
        public WobjLine(String line) {
            String[] tmp = line.split("[\\:]");
            id = tmp[0];
            path = tmp[1];
            obj_sha1 = tmp[2];
            if (tmp.length > 3)
                fdata_sha1 = tmp[3];
        }
    }
}
