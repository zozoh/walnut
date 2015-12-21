package org.nutz.walnut.web.module;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

/**
 * 为obj对象提供预览功能
 *
 * 为图片提供缩略图
 * 
 * 为视频提供预览视频(分辨率缩小)
 * 
 * 为纯文本文件提供前100行
 * 
 * 为word文档提供前2页
 * 
 * @author pw
 *
 */
@IocBean
@At("/p")
public class PreviewModule extends AbstractWnModule {

    /**
     * 
     * 
     * 
     * @param obj
     *            可以是文件路徑或ID
     * @param size
     *            大小, 默認提供 16, 24, 64, 256
     * @param resp
     * @return
     */
    @At("/thumbnail")
    @Ok("raw")
    public Object getPreviewThumbnail(@Param("obj") String obj,
                                      @Param("size") int size,
                                      HttpServletResponse resp) {
        WnObj tobj = null;
        WnObj re = null;
        WnObj pdir = null;
        if (size <= 0) {
            size = 256;
        }
        // 找到obj对象
        if (obj.indexOf("/") != -1) {
            tobj = io.fetch(null, obj);
        } else {
            tobj = io.get(obj);
        }
        String dtp = (tobj.race().equals(WnRace.DIR)
                      && Strings.isBlank(tobj.type())) ? "folder" : tobj.type().toLowerCase();
        // 查找对应的缩略图对象
        pdir = io.fetch(null, thumbnailPath(tobj, true)); // id
        if (pdir == null) {
            pdir = io.fetch(null, thumbnailPath(tobj, false)); // sha1
            if (pdir == null) {
                // 没有的话返回默认缩略图
                return getDefaultPreviewThumbnail(dtp, size, resp);
            }
        }
        // 找到了目錄
        re = getThumbnailObj(pdir, size);
        if (re == null) {
            return getDefaultPreviewThumbnail(dtp, size, resp);
        }
        try {
            String fnm = tobj.name() + "_" + size + "." + re.type();
            String encode = new String(fnm.getBytes("UTF-8"), "ISO8859-1");
            resp.setHeader("Content-Disposition", "attachment; filename=" + encode);
            resp.setHeader("Content-Type", re.mime());
        }
        catch (Exception e) {}
        return Wn.getCopyFile(io, re);
    }

    private String thumbnailPath(WnObj obj, boolean useId) {
        String d0 = obj.d0();
        String d1 = obj.d1();
        String dir = useId ? "/id/" + obj.id() : "/sha1/" + obj.sha1();
        if ("root".equals(d0)) {
            return "/root/.preview_thumbnail" + dir;
        } else {
            return "/" + d0 + "/" + d1 + "/.preview_thumbnail" + dir;
        }
    }

    @At("/default/thumbnail")
    @Ok("raw")
    public Object getDefaultPreviewThumbnail(@Param("tp") String tp,
                                             @Param("size") int size,
                                             HttpServletResponse resp) {
        WnObj re = null;
        WnObj etpdir = io.fetch(null, "/etc/thumbnail");
        WnObj pdir = io.fetch(etpdir, tp);
        if (pdir != null) {
            re = getThumbnailObj(pdir, size);
        }
        // 返回unknown類型
        if (re == null) {
            re = getThumbnailObj(io.fetch(etpdir, "unknown"), size);
        }
        if (re == null) {
            throw Lang.impossible();
        }
        try {
            String fnm = tp + "_" + size + "." + re.type();
            String encode = new String(fnm.getBytes("UTF-8"), "ISO8859-1");
            resp.setHeader("Content-Disposition", "attachment; filename=" + encode);
            resp.setHeader("Content-Type", re.mime());
            OutputStream ops = resp.getOutputStream();
            InputStream ins = io.getInputStream(re, 0);
            Streams.writeAndClose(ops, ins);
            ops.flush();
        }
        catch (Exception e) {}
        return null;
    }

    // 缩略图后缀搜索的顺序是 png > jpg > gif > jpeg
    private String[] tps = new String[]{"png", "jpg", "gif", "jpeg"};

    private WnObj getThumbnailObj(WnObj pdir, int size) {
        for (String itp : tps) {
            WnObj tobj = io.fetch(pdir, size + "x" + size + "." + itp);
            if (tobj != null) {
                return tobj;
            }
        }
        // size大小可能不支持, 返回最大的一個
        for (String itp : tps) {
            WnObj tobj = io.fetch(pdir, Wn.thumbnail.size_256 + "." + itp);
            if (tobj != null) {
                return tobj;
            }
        }
        return null;
    }

    @At("/video")
    @Ok("raw")
    public Object getPreviewVideo(@Param("obj") String obj, HttpServletResponse resp) {
        WnObj tobj = null;
        // 找到obj对象
        if (obj.indexOf("/") != -1) {
            tobj = io.fetch(null, obj);
        } else {
            tobj = io.get(obj);
        }
        if (tobj != null && "done".equals(tobj.getString("vcp_state"))) {
            String pvpath = previewVideoPath(tobj);
            WnObj pvobj = io.fetch(null, pvpath);
            if (pvobj != null) {
                try {
                    String fnm = tobj.name() + "_preview." + pvobj.type();
                    String encode = new String(fnm.getBytes("UTF-8"), "ISO8859-1");
                    resp.setHeader("Content-Disposition", "attachment; filename=" + encode);
                    resp.setHeader("Content-Type", pvobj.mime());
                }
                catch (Exception e) {}
                return Wn.getCopyFile(io, pvobj);
            }
        }
        return null;
    }

    private String previewVideoPath(WnObj obj) {
        String d0 = obj.d0();
        String d1 = obj.d1();
        String dir = "/sha1/" + obj.sha1() + "/preview.mp4";
        if ("root".equals(d0)) {
            return "/root/.preview_video" + dir;
        } else {
            return "/" + d0 + "/" + d1 + "/.preview_video" + dir;
        }
    }

}