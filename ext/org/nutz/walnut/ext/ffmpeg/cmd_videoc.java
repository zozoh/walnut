package org.nutz.walnut.ext.ffmpeg;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.random.R;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.SimpleContext;
import org.nutz.log.Log;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class cmd_videoc extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "vkF");
        WnObj obj = Wn.checkObj(sys, params.vals[0]);
        if (obj.name().startsWith("_") && !params.is("F"))
            return;
        WnObj pdir = sys.io.checkById(obj.parentId());
        WnObj tdir = sys.io.createIfNoExists(pdir, "." + obj.name() + ".videoc", WnRace.DIR);

        File tmpDir = new File("/tmp/walnut_video/" + R.UU32());
        File source = new File(tmpDir, "source.mp4");
        File mainTarget = new File(tmpDir, "main.mp4");
        File thumb = new File(tmpDir, "preview.jpg");
        File preview = new File(tmpDir, "preview.mp4");
        Files.createDirIfNoExists(tmpDir);
        NutMap vc_params = params.map();
        vc_params.setnx("vcodec", "libx264")
                 .setnx("bv", "3000")
                 .setnx("ba", "64")
                 .setnx("preset", "veryfast");
        vc_params.setnx("fps", "24");

        vc_params.setv("source", source.getAbsolutePath());
        vc_params.setv("mainTarget", mainTarget.getAbsolutePath());
        vc_params.setv("thumbPath", thumb.getAbsolutePath());
        vc_params.setv("previewPath", preview.getAbsolutePath());

        String cmd = "";
        Segment seg = null;
        WnObj t;
        NutMap tMap = new NutMap().setv("videoc_source", obj.id());

        Log log = sys.getLog(params);
        Pattern mode = params.has("mode") ? Pattern.compile(params.get("mode")) : null;
        try {
            Files.write(source, sys.io.getInputStream(obj, 0));
            VideoInfo vi = cmd_videoi.readVideoInfo(source.getPath());
            if (vi == null)
                throw Err.create("e.cmds.videoc.video_info_null");

            log.debug("video info=\n" + Json.toJson(vi));

            vc_params.setnx("preview_size",
                            String.format("%dx%d", vi.getWidth() / 4 * 2, vi.getHeight() / 4 * 2));

            log.debug("ffmpeg params :\n" + Json.toJson(vc_params));

            // 先生成预览图
            if (mode == null || mode.matcher("preview_image").find()) {
                seg = Segments.create("ffmpeg -y -v quiet -ss 00:00:01.00 -i ${source} -f image2 -vframes 1 ${thumbPath}");
                cmd = seg.render(new SimpleContext(vc_params)).toString();
                log.debug("cmd: " + cmd);
                Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
                t = sys.io.createIfNoExists(tdir, "_preview.jpg", WnRace.FILE);
                sys.io.writeAndClose(t, new FileInputStream(thumb));
                t = sys.io.checkById(t.id());
                sys.io.appendMeta(obj, "thumb:'" + t.thumbnail() + "'");
                sys.io.appendMeta(t, tMap);
            }
            // 再生成预览视频
            if (mode == null || mode.matcher("preview_video").find()) {
                vc_params.setv("preview_bv", vc_params.getInt("bv") / 3);
                seg = Segments.create("ffmpeg -y -v quiet -i ${source} -movflags faststart -preset ultrafast -vcodec libx264 -maxrate ${preview_bv}k -bufsize 2048k -b:a 64k -r ${fps} -s ${preview_size} ${previewPath}");
                cmd = seg.render(new SimpleContext(vc_params)).toString();
                log.debug("cmd: " + cmd);
                Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
                t = sys.io.createIfNoExists(tdir, "_preview.mp4", WnRace.FILE);
                sys.io.writeAndClose(t, new FileInputStream(preview));
                sys.io.appendMeta(obj, "video_preview:'" + t.id() + "'");
                sys.io.appendMeta(t, tMap);
            }
            // 生成主文件
            if (mode == null || mode.matcher("preview_video").find()) {
                seg = Segments.create("ffmpeg -y -v quiet -i ${source} -movflags faststart -preset ${preset} -vcodec ${vcodec} -acodec aac -maxrate ${bv}k -bufsize 2048k -b:a ${ba}k -r ${fps} -ar 48000 -ac 2 ${mainTarget}");
                cmd = seg.render(new SimpleContext(vc_params)).toString();
                log.debug("cmd: " + cmd);
                Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
                t = sys.io.createIfNoExists(tdir, "_1_1.mp4", WnRace.FILE);
                sys.io.writeAndClose(t, new FileInputStream(mainTarget));
                sys.io.appendMeta(obj, "videoc_dir:'" + tdir.id() + "'");
                sys.io.appendMeta(t, tMap);
            }
            sys.io.appendMeta(obj, "videoc_dir:'id:"+tdir.id()+"'");
            sys.out.print(tdir.id());
        }
        finally {
            if (!params.is("k"))
                Files.deleteDir(tmpDir);
        }
    }
}
