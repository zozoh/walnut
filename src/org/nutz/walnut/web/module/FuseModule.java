package org.nutz.walnut.web.module;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        InputStream ins = io.getInputStream(_obj(), offset);
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            int len = 0;
            byte[] buf = new byte[4096];
            int count = 0;
            while (true) {
                if (size <= count)
                    break;
                len = ins.read(buf, 0, size - count);
                if (len == -1)
                    break;
                if (len > 0) {
                    bao.write(buf, 0, len);
                    count += len;
                } else {
                    break; // TODO 为啥总是返回0了呢,没数据的时候
                }
            }
            buf = bao.toByteArray();
            resp.setContentLength(buf.length);
            resp.getOutputStream().write(buf);
        }
        finally {
            Streams.safeClose(ins);
        }
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

        if (obj.isLink()) {
            map.put("st_nlink", 2);
        	map.put("st_size", 4);
            map.put("st_mode", obj.mode() | 0xA000);
        }else if (obj.isDIR()) {
            map.put("st_size", 4);
            map.put("st_mode", obj.mode() | 0x4000);
        } else if (obj.isFILE()) {
            map.put("st_size", obj.len() > 0 ? obj.len() : 0);
            map.put("st_mode", obj.mode() | 0x8000);
        } else {
            map.put("st_size", 0);
            map.put("st_mode", obj.mode() | 0xA000);
        }
        map.put("name", obj.name());

        // map.put("st_uid", 0);
        return map;
    }

    @At
    @Ok("json")
    public List<Object> readdir(@Param("path") String path) {
        WnObj p = _obj();
        List<WnObj> ls = io.query(Wn.Q.pid(p));
        List<Object> re = new ArrayList<Object>();
        for (WnObj w : ls) {
            re.add(new Object[]{w.name(), _getattr(w), 0});
        }
        return re;
    }

    @At
    public void rmdir(@Param("path") String path) {
        io.delete(_obj());
    }

    @At
    public void symlink(@Param("source") String source, @Param("target") String target) {
    	String dir = target.substring(0, target.lastIndexOf('/')+1);
        exec("fuse_ln",
                 Wn.WC().checkMe(),
                 "ln -s "
                       + Wn.normalizeFullPath(dir + source, Wn.WC().SE())
                       + " "
                       + Wn.normalizeFullPath(target, Wn.WC().SE()));
    }

    @At
    public void truncate(@Param("path") String path, @Param("length") int length)
            throws IOException {
        io.trancate(io.check(null, path), length);
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
        String hid = io.open(obj, Wn.S.WM);
        io.seek(hid, offset);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len = 0;
        while (-1 != (len = ins.read(buf))) {
            bao.write(buf, 0, len);
        }
        buf = bao.toByteArray();
        io.write(hid, buf);
        io.close(hid);
        return buf.length;
    }

    @At
    public void rename(@Param("source") String source, @Param("target") String _target)
            throws Exception {
    	WnObj t = io.fetch(null, _target);
    	if (t != null) {
    		io.delete(t);
    	}
        io.move(_obj(), _target);
    }
    
    @At
    @Ok("raw")
    public String readlink(@Param("path") String path) {
    	WnObj obj = _obj();
    	if (obj == null)
    		return null;
    	return Wn.real(obj, io).path();
    }

    @At
    @Ok("raw")
    public int open(@Param("path")String path, @Param("flags")int flags) {
        /*
        #define O_RDONLY    0x0000
        #define O_WRONLY    0x0001
        #define O_RDWR      0x0002
        #define O_ACCMODE   0x0003

        #define O_BINARY    0x0004  
        #define O_TEXT      0x0008  
        #define O_NOINHERIT 0x0080  

        #define O_CREAT     0x0100  
        #define O_EXCL      0x0200
        #define O_NOCTTY    0x0400
        #define O_TRUNC     0x0800
        #define O_APPEND    0x1000
        #define O_NONBLOCK  0x2000
        */
        return 0; 
    }
    
    @At
    @Ok("raw")
    public int release(@Param("path")String path, @Param("fh")int fh) {
        
        return 0;
    }
    
    @At
    @Ok("void")
    public void chmod(@Param("path")String path, @Param("mode")int mode) {
       _obj().mode(mode);
       io.set(_obj(), "^md$");
    }

    public WnObj _obj() {
        return (WnObj) Mvcs.getReq().getAttribute("fuse_obj");
    }
}