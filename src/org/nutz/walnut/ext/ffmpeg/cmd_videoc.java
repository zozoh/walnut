package org.nutz.walnut.ext.ffmpeg;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
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

            // 记录一下视频的信息
            obj.setv("width", vi.getWidth());
            obj.setv("height", vi.getHeight());
            obj.setv("duration", vi.getLength());
            obj.setv("video_frame_count", vi.getFrameCount());
            obj.setv("video_frame_rate", vi.getFrameRate());
            sys.io.set(obj, "^(video_frame_.+|width|height|duration)");

            // 先生成预览图
            if (mode == null || mode.matcher("preview_image").find()) {
                // 预览图不能是视频的第一秒，应该是开头的 38.2% 左右的帧
                int preview_in_sec = (int) (vi.getLength() * 0.382);
                String preview_in_secS = Times.sT(preview_in_sec);

                seg = Segments.create("ffmpeg -y -v quiet -ss "
                                      + preview_in_secS
                                      + ".00 -i ${source} -f image2 -vframes 1 ${thumbPath}");
                cmd = seg.render(new SimpleContext(vc_params)).toString();
                log.debug("cmd: " + cmd);
                Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
                t = sys.io.createIfNoExists(tdir, "_preview.jpg", WnRace.FILE);
                sys.io.writeAndClose(t, new FileInputStream(thumb));
                t = sys.io.checkById(t.id());

                // 如果预览图没有缩略图，生成它
                if (!t.hasThumbnail()) {
                    String thumbSize = params.get("thumb");
                    if (Strings.isBlank(thumbSize) || "true".equals(thumbSize))
                        thumbSize = "64x64";
                    sys.execf("iimg id:%s -thumb %s -Q", t.id(), thumbSize);
                    t = sys.io.checkById(t.id());
                }

                // 这里要将预览图的 thumb 设置给 video，同时将预览图作为 video_cover
                NutMap meta = new NutMap();
                meta.put("thumb", t.thumbnail());
                meta.put("video_cover", t.id());

                sys.io.appendMeta(obj, meta);
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
                String fmd5 = Lang.md5(mainTarget);
                String smd5 = simpleMd5(mainTarget);
                sys.io.writeAndClose(t, new FileInputStream(mainTarget));
                sys.io.appendMeta(obj, "videoc_dir:'" + tdir.id() + "'");
                sys.io.appendMeta(t, "fmd5:'" + fmd5 + "'");
                sys.io.appendMeta(t, "smd5:'" + smd5 + "'");
                sys.io.appendMeta(t, tMap);
            }
            sys.io.appendMeta(obj, "videoc_dir:'id:" + tdir.id() + "'");
            sys.out.print(tdir.id());
        }
        finally {
            if (!params.is("k"))
                Files.deleteDir(tmpDir);
        }
    }

    /**
     * 抽样式md5
     * 
     * @return
     */
    public static String simpleMd5(File f) {
        if (f.length() <= 1024 * 1024) {
            return Lang.md5(f);
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(f, "r");
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] data = new byte[1024 * 8];
            long size = f.length();
            for (int i = 0; i < 128; i++) {
                long pos = ((size * i / 128) / 8192) * 8192;
                raf.seek(pos);
                raf.read(data);
                md.update(data);
            }
            return Lang.fixedHexString(md.digest());
        }
        catch (Exception e) {
            throw Err.wrap(e);
        }
        finally {
            Streams.safeClose(raf);
        }
    }
}
