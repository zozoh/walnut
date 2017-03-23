package org.nutz.walnut.impl.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
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
import org.nutz.walnut.api.io.WnImpExp;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.util.ZParams;

@IocBean(name="wnImpExp")
public class WnImpExpImpl implements WnImpExp {
    
    @Inject
    protected WnIo io;

    @SuppressWarnings("resource")
    @Override
    public void exp(String root, ZParams params, Log log) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir")  + "/walnut/dump/" + R.UU32());
        log.info("exp tmp dir : " + tmpDir.getAbsolutePath());
        log.info(">> " + Json.toJson(params));
        tmpDir.mkdirs();
        final FileWriter fw_objs;
        int[] count = new int[1];
        long[] fdata_sum = new long[1];
        try {
            // 首先,准备一个临时文件,用于存放对象列表
            fw_objs = new FileWriter(new File(tmpDir, "objs.txt"));
            // 缓存sha1列表
            Set<String> sha1Set = new HashSet<>();
            io.walk(io.check(null, root), wobj -> {
                try {
                    // 过滤一下肯定不需要的文件
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
            String zippath = params.get("zippath");
            if (Strings.isBlank(zippath) || !(zippath.startsWith("/opt") || zippath.startsWith("/data"))) {
                zippath = tmpDir.getParent() + "/" + tmpDir.getName() + ".zip";
            } else if(!zippath.endsWith(".zip")){
                zippath += ".zip";
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
        }
        catch (IOException e) {
            log.warn("write something fail", e);
        }
        finally {
            if (!params.is("keep"))
                Files.deleteDir(tmpDir);
        }
        
    }

    @Override
    public void imp(String root, ZParams params, Log log) {}

}
