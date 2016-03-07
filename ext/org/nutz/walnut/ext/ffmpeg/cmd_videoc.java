package org.nutz.walnut.ext.ffmpeg;

import java.io.File;
import java.io.FileInputStream;

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

public class cmd_videoc extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "vk");
        WnObj obj = Wn.checkObj(sys, params.vals[0]);
        WnObj pdir = sys.io.checkById(obj.parentId());
        WnObj tdir = sys.io.createIfNoExists(pdir, "." + obj.name() + ".videoc", WnRace.DIR);

        File tmpDir = new File("/tmp/walnut_video/" + R.UU32());
        File source = new File(tmpDir, "source.mp4");
        File mainTarget = new File(tmpDir, "1_1.mp4");
        File thumb = new File(tmpDir, "preview.jpg");
        File preview = new File(tmpDir, "preview.mp4");
        Files.createDirIfNoExists(tmpDir);
        NutMap vc_params = params.map();
        vc_params.setnx("vcodec", "libx264")
                 .setnx("bv", "3000")
                 .setnx("ba", "64")
                 .setnx("preset", "ultrafast");
        vc_params.setnx("fps", "24");

        vc_params.setv("source", source.getAbsolutePath());
        vc_params.setv("mainTarget", mainTarget.getAbsolutePath());
        vc_params.setv("thumbPath", thumb.getAbsolutePath());
        vc_params.setv("previewPath", preview.getAbsolutePath());

        String cmd = "";
        Segment seg = null;
        WnObj t;

        // Log log = new WalnutLog(sys, params.is("v") ? 10 : 40);
        Log log = sys.getLog(params);

        try {
            Files.write(source, sys.io.getInputStream(obj, 0));
            VideoInfo vi = cmd_videoi.readVideoInfo(source.getPath());

            log.debug("video info=\n" + Json.toJson(vi));

            vc_params.setnx("previewSize",
                            String.format("%dx%d", vi.getWidth() / 4 * 2, vi.getHeight() / 4 * 2));

            log.debug("ffmpeg params :\n" + Json.toJson(vc_params));

            // 先生成预览图
            seg = Segments.create("ffmpeg -y -v quiet -i ${source} -f image2 -ss 1 -vframes 1  ${thumbPath}");
            cmd = seg.render(new SimpleContext(vc_params)).toString();
            log.debug("cmd: " + cmd);
            Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
            t = sys.io.createIfNoExists(tdir, "_preview.jpg", WnRace.FILE);
            sys.io.writeAndClose(t, new FileInputStream(thumb));

            // 再生成预览视频
            vc_params.setv("preview_bv", vc_params.getInt("bv") / 3);
            seg = Segments.create("ffmpeg -y -v quiet -i ${source} -movflags faststart -preset ultrafast -vcodec libx264 -b:v ${preview_bv}k -b:a 64k -r ${fps} -s ${previewSize} ${previewPath}");
            cmd = seg.render(new SimpleContext(vc_params)).toString();
            log.debug("cmd: " + cmd);
            Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
            t = sys.io.createIfNoExists(tdir, "_preview.mp4", WnRace.FILE);
            sys.io.writeAndClose(t, new FileInputStream(preview));

            // 生成主文件
            seg = Segments.create("ffmpeg -y -v quiet -i ${source} -movflags faststart -preset ${preset} -vcodec ${vcodec} -b:v ${bv}k -b:a ${ba}k -r ${fps} -ar 48000 -ac 2 ${mainTarget}");
            cmd = seg.render(new SimpleContext(vc_params)).toString();
            log.debug("cmd: " + cmd);
            Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
            t = sys.io.createIfNoExists(tdir, "1_1.mp4", WnRace.FILE);
            sys.io.writeAndClose(t, new FileInputStream(mainTarget));

            sys.io.appendMeta(obj, "videoc_dir:'" + tdir.id() + "'");
            sys.out.print(tdir.id());
        }
        finally {
            if (!params.is("k"))
                Files.deleteDir(tmpDir);
        }
    }
}
