package org.nutz.walnut.web.module;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.FuseActionFilter;

@IocBean
@At("/fuse")
@Ok("http:200")
@Fail("http:403")
@Filters({@By(type = FuseActionFilter.class, args = "ioc:fuseActionFilter")})
public class FuseModule extends AbstractWnModule {

    private static final Log log = Logs.get();

    @At
    public void create(@Param("path") String path) {
        io.writeText(io.create(null, path, WnRace.FILE), "");
    }

    @At
    public void link(@Param("source") String source, @Param("target") String target) {
        WnObj s = _obj();
        WnObj t = io.create(null, target, WnRace.FILE);
        io.writeAndClose(t, io.getInputStream(s, 0));
    }

    @At
    public void mkdir(@Param("path") String path) {
        io.create(null, path, WnRace.DIR);
    }

    @At
    @Ok("void")
    public void read(@Param("path") String path,
                     @Param("size") int size,
                     @Param("offset") int offset,
                     HttpServletResponse resp) throws IOException {
        if (log.isDebugEnabled())
            log.debugf("path=%s, size=%s, offset=%s", path, size, offset);
        InputStream in = io.getInputStream(_obj(), offset);
        OutputStream out = resp.getOutputStream();
        byte[] buf = new byte[16 * 1024];
        int len = 0;
        int count = 0;
        while (size - count > 0) {
            if (size - count > buf.length)
                len = in.read(buf);
            else
                len = in.read(buf, 0, size - count);
            if (len < 0)
                break;
            if (len > 0) {
                out.write(buf, 0, len);
                count += len;
            }
        }
        in.close();
    }

    @At
    @Ok("json")
    public NutMap getattr(@Param("path") String path) {
        return _getattr(_obj());
    }

    protected NutMap _getattr(WnObj obj) {
        NutMap map = new NutMap();
        map.put("st_atime", 0);
        map.put("st_ctime", obj.createTime() / 1000);
        map.put("st_mtime", obj.lastModified() / 1000);
        map.put("st_nlink", 1);

        if (obj.isDIR()) {
            map.put("st_size", 0);
            map.put("st_mode", 0777 | 0x4000);
        } else if (obj.isFILE()) {
            map.put("st_size", obj.len() > 0 ? obj.len() : 0);
            map.put("st_mode", 0777 | 0x8000);
        } else {
            map.put("st_size", 0);
            map.put("st_mode", 0777 | 0xA000);
        }
        map.put("name", obj.name());

        // map.put("st_uid", 0);
        return map;
    }

    @At
    @Ok("json")
    public List<Object> readdir(@Param("path") String path) {
        WnObj p = _obj();
        List<WnObj> ls = io.getChildren(p, null);
        List<Object> re = new ArrayList<Object>();
        for (WnObj w : ls) {
            re.add(new Object[]{w.name(), _getattr(w) , 0});
        }
        return re;
    }

    @At
    public void rmdir(@Param("path") String path) {
        io.delete(_obj());
    }

    @At
    public void symlink(@Param("source") String source, @Param("target") String target) {
        _run_cmd("fuse_ln",
                 null,
                 "ln -s "
                       + Wn.normalizeFullPath(source, Wn.WC().SE())
                       + " "
                       + Wn.normalizeFullPath(target, Wn.WC().SE()));
    }

    @At
    public void truncate(@Param("path") String path, @Param("length") int length)
            throws IOException {
        // TODO 等待 issue 34
    }

    @At
    public void unlink(@Param("path") String path) {
        io.delete(_obj());
    }

    @At
    @Ok("json")
    public double[] utimens(@Param("path") String path) {
        return new double[]{System.currentTimeMillis() / 100.0, _obj().lastModified() / 100.0};
    }

    @At
    @Ok("raw")
    public long write(@Param("path") String path,
                      InputStream ins,
                      @Param("offset") int offset,
                      @Param("size") int size) throws IOException {
        WnObj obj = _obj();
        if (log.isDebugEnabled())
            log.debugf("write file path=%s, offset=%s, old_len=%d, size=%d",
                       path,
                       offset,
                       obj.len(),
                       size);
        if (offset > 1024 * 1024)
            io.cleanHistory(obj, 0);

        if (obj.len() == offset) {
            OutputStream out = io.getOutputStream(obj, offset);
            return Streams.writeAndClose(out, ins);
        }
        // --------------------------------------
        java.io.File tmp = java.io.File.createTempFile("ess_fuse", ".bin");
        OutputStream out = null;
        try {
            if (obj.len() > 0) {
                out = new FileOutputStream(tmp);
                Streams.writeAndClose(out, io.getInputStream(obj, 0));
                out = null;
            }
            RandomAccessFile raf = new RandomAccessFile(tmp, "rw");
            raf.seek(offset);
            byte[] buf = new byte[4096]; // 一般就是4kb
            int len = 0;
            int count = 0;
            while (true) {
                len = ins.read(buf);
                if (len < 0)
                    break;
                if (len > 0) {
                    raf.write(buf, 0, len);
                    count += len;
                }
            }
            raf.close();
            ins = new FileInputStream(tmp);
            io.writeAndClose(obj, ins);
            ins = null;
            return count;
        }
        finally {
            if (out != null)
                Streams.safeClose(out);
            if (ins != null)
                Streams.safeClose(ins);
            tmp.delete();
        }
    }

    @At
    public void rename(@Param("source") String source, @Param("target") String _target)
            throws Exception {
        io.move(_obj(), _target);
    }

    public WnObj _obj() {
        return (WnObj) Mvcs.getReq().getAttribute("fuse_obj");
    }
}