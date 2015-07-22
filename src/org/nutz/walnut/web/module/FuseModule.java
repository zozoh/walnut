package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.web.filter.FuseActionFilter;

@IocBean
@At("/fuse")
@Ok("http:200")
@Fail("http:403")
@Filters({@By(type = FuseActionFilter.class, args = "ioc:fuseActionFilter")})
public class FuseModule extends AbstractWnModule {
	
	private static final Log log = Logs.get();
	
	@At
	public void create(@Param("path")String path) {
		io.writeText(io.create(null, path, WnRace.FILE), "");
	}
	
	@At
	public void link(@Param("source")String source, @Param("target")String target) {
        WnObj s = io.check(null, source);
		WnObj t = io.create(null, target, WnRace.FILE);
		io.writeAndClose(t, io.getInputStream(s, 0));
	}
	
	@At
	public void mkdir(@Param("path")String path) {
		io.create(null, path, WnRace.DIR);
	}
	
	@At
	@Ok("void")
	public void read(@Param("path")String path, @Param("size")int size, @Param("offset")int offset, HttpServletResponse resp) throws IOException {
		if (log.isDebugEnabled())
			log.debugf("path=%s, size=%s, offset=%s", path, size, offset);
		InputStream in = io.getInputStream(io.check(null, path), offset);
		OutputStream out = resp.getOutputStream();
		byte[] buf = new byte[16*1024];
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
				count+=len;
			}
		}
		in.close();
	}
	
	@At
	@Ok("json")
	public NutMap getattr(@Param("path")String path) {
		return _getattr(io.check(null, path));
	}
	
	protected NutMap _getattr(WnObj obj) {
		NutMap map = new NutMap();
		map.put("st_atime", 0);
		map.put("st_ctime", obj.createTime() / 1000);
		map.put("st_mtime", obj.lastModified() / 1000);
		map.put("st_nlink", 1);
		
		if (obj.isDIR()) {
			map.put("st_size", 0);
			map.put("st_mode", 0755 | 0x4000);
		} else if (obj.isFILE()) {
			map.put("st_size", obj.len() > 0 ? obj.len() : 0);
			map.put("st_mode", 0755 | 0x8000);
		} else {
			map.put("st_size", 0);
			map.put("st_mode", 0755 | 0xA000);
		}
		map.put("name", obj.name());
		
		//map.put("st_uid", 0);
		return map;
	}
	
	@At
	@Ok("json")
	public List<Object> readdir(@Param("path")String path) {
	    WnObj p = io.check(null, path);
	    List<WnObj> ls = io.getChildren(p, null);
		List<Object> re = new ArrayList<Object>();
		for (WnObj w : ls) {
			re.add(_getattr(w));
		}
		return re;
	}
	
	@At
	public void rmdir(@Param("path")String path) {
		io.delete(io.check(null, path));
	}
	
	@At
	public void symlink(@Param("target")String target) {
	 // TODO 等待 issue 35
	}
	
	@At
	public void truncate(@Param("path")String path, @Param("length")int length) throws IOException {
		// TODO 等待 issue 34
	}
	
	@At
	public void unlink(@Param("path")String path) {
		io.delete(io.check(null, path));
	}
	
	@At
	@Ok("json")
	public double[] utimens(@Param("path")String path) {
		return new double[]{System.currentTimeMillis()/100.0, io.check(null, path).lastModified()/100.0};
	}
	
	@At
	@Ok("raw")
	public int write(@Param("path")String path, InputStream data, @Param("offset")int offset, @Param("size")int size) throws IOException {
	    WnObj obj = io.check(null, path);
		if (log.isDebugEnabled())
			log.debugf("write file path=%s, offset=%s, old_len=%d, size=%d", path, offset, obj.len(), size);
		if (offset > 1024*1024)
			io.cleanHistory(obj, 0);
		
		OutputStream out = io.getOutputStream(obj, offset);
		try {
            byte[] buf = new byte[8192];
            int count = 0;
            while (size >= count) {
                int len = data.read(buf);
                if (len == -1)
                    break;
                if (len > 0) {
                    count += len;
                    out.write(buf);
                }
            }
            return count;
        }
        finally {
            out.flush();
            out.close();
        }
	}
	
    @At
    public void rename(@Param("source")String source, @Param("target") String _target) throws Exception {
        io.move(io.check(null, source), _target);
    }
}