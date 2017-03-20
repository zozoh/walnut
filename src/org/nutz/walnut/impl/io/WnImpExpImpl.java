package org.nutz.walnut.impl.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnImpExp;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.util.ZParams;

@IocBean(name="wnImpExp")
public class WnImpExpImpl implements WnImpExp {
    
    private static final Log log = Logs.get();
    
    @Inject
    protected WnIo io;

    @SuppressWarnings("resource")
    @Override
    public void exp(String root, ZParams params) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir")  + "/" + R.UU32());
        log.info("exp tmp dir : " + tmpDir.getAbsolutePath());
        tmpDir.mkdirs();
        final FileWriter fw_objs;
        try {
            // 首先,准备一个临时文件,用于存放对象列表
            fw_objs = new FileWriter(new File(tmpDir, "objs.txt"));
            // 缓存sha1列表
            Set<String> sha1Set = new HashSet<>();
            io.walk(io.check(null, root), (wobj)->{
                try {
                    // 过滤一下肯定不需要的文件
                    if (wobj.mount() != null && wobj.data() != null) {
                        log.debug("skip >> " + wobj.path());
                        return;
                    }
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
    public void imp(String root, ZParams params) {}

}
