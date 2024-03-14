package com.site0.walnut.ext.media.ffmpeg;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
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
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class cmd_videoc extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "vkFocqn");
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
                seg = Segments.create("ffmpeg -y -v quiet -i ${source} -movflags faststart -preset ultrafast -pix_fmt yuv420p -vcodec libx264 -maxrate ${preview_bv}k -bufsize 2048k -b:a 64k -r ${fps} -s ${preview_size} ${previewPath}");
                cmd = seg.render(new SimpleContext(vc_params)).toString();
                log.debug("cmd: " + cmd);
                Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
                t = sys.io.createIfNoExists(tdir, "_preview.mp4", WnRace.FILE);
                sys.io.writeAndClose(t, new FileInputStream(preview));
                sys.io.appendMeta(obj, "video_preview:'" + t.id() + "'");
                sys.io.appendMeta(t, tMap);
            }
            // 生成主文件
            if (mode == null || mode.matcher("main_video").find()) {
                String _crop = params.get("crop", "");
                if (Strings.isBlank(_crop)) {
                    String tmp = obj.name();
                    if (pattern.matcher(tmp).find()) {
                        String cut = tmp.substring(0, tmp.indexOf('_'));
                        String size = tmp.substring(tmp.indexOf('_') + 1, tmp.indexOf('~'));
                        _crop = size.replace('x', ':') + ":" + cut.replace('-', ':');
                    }
                }
                if (Strings.isBlank(_crop)) {
                    seg = Segments.create("ffmpeg -y -v quiet -i ${source} -movflags faststart -pix_fmt yuv420p -preset ${preset} -vcodec ${vcodec} -acodec aac -maxrate ${bv}k -bufsize 2048k -b:a ${ba}k -r ${fps} -ar 48000 -ac 2 ${mainTarget}");
                    cmd = seg.render(new SimpleContext(vc_params)).toString();
                    log.debug("cmd: " + cmd);
                    Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
                    t = sys.io.createIfNoExists(tdir, "_1_1.mp4", WnRace.FILE);
                    String fmd5 = Lang.md5(mainTarget);
                    String smd5 = simpleMd5(mainTarget);
                    sys.io.writeAndClose(t, new FileInputStream(mainTarget));
                    sys.io.appendMeta(t, "fmd5:'" + fmd5 + "'");
                    sys.io.appendMeta(t, "smd5:'" + smd5 + "'");
                    sys.io.appendMeta(t, tMap);
                } else {
                    String[] tmp = _crop.split(":");
                    int crop_w = Integer.parseInt(tmp[0]);
                    int crop_h = Integer.parseInt(tmp[1]);
                    int crop_x = Integer.parseInt(tmp[2]);
                    int crop_y = Integer.parseInt(tmp[3]);
                    for (int i = 0; i < crop_x; i++) {
                        for (int j = 0; j < crop_y; j++) {
                            String crop = String.format("-vf crop=%s:%s:%s:%s",
                                                        crop_w,
                                                        crop_h,
                                                        i * crop_w,
                                                        j * crop_h);
                            String crop_target = tmpDir
                                                 + "/"
                                                 + String.format("_%s_%s.mp4", i + 1, j + 1);
                            vc_params.setv("crop", crop);
                            vc_params.put("crop_target", crop_target);
                            seg = Segments.create("ffmpeg -y -v quiet -i ${source} ${crop} -movflags faststart -pix_fmt yuv420p -preset ${preset} -vcodec ${vcodec} -acodec aac -maxrate ${bv}k -bufsize 2048k -b:a ${ba}k -r ${fps} -ar 48000 -ac 2 ${crop_target}");
                            cmd = seg.render(new SimpleContext(vc_params)).toString();
                            log.debug("cmd: " + cmd);
                            Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
                            t = sys.io.createIfNoExists(tdir,
                                                        String.format("_%s_%s.mp4", i + 1, j + 1),
                                                        WnRace.FILE);
                            String fmd5 = Lang.md5(crop_target);
                            String smd5 = simpleMd5(new File(crop_target));
                            sys.io.writeAndClose(t, new FileInputStream(crop_target));
                            sys.io.appendMeta(t, "fmd5:'" + fmd5 + "'");
                            sys.io.appendMeta(t, "smd5:'" + smd5 + "'");
                            sys.io.appendMeta(t, tMap);
                        }
                    }

                }
                sys.io.appendMeta(obj, "videoc_dir:'" + tdir.id() + "'");
            }
            sys.io.appendMeta(obj, "videoc_dir:'id:" + tdir.id() + "'");
            // 输出转换后元数据
            if (params.is("o")) {
                JsonFormat jfmt = Cmds.gen_json_format(params);
                sys.out.println(Json.toJson(obj, jfmt));
            }
            // 默认输出转换目录 ID
            else {
                sys.out.print(tdir.id());
            }
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

    static Pattern pattern = Pattern.compile("^([0-9]{1,2}-[0-9]{1,2})(_[0-9]{3,4}x[0-9]{3,4})~.+$");

    public static void main(String[] args) {
        String tmp = "3-1_640x1080~10空天猎.mp4";
        System.out.println(tmp.matches("^([0-9]{1,2}-[0-9]{1,2})(_[0-9]{3,4}x[0-9]{3,4})~.+$"));
        Matcher matcher = pattern.matcher(tmp);
        System.out.println(matcher.find());
        String cut = tmp.substring(0, tmp.indexOf('_'));
        String size = tmp.substring(tmp.indexOf('_') + 1, tmp.indexOf('~'));
        String crop = size.replace('x', ':') + ":" + cut.replace('-', ':');
        System.out.println(crop);
    }
}
