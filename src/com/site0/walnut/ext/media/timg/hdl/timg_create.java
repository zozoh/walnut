package com.site0.walnut.ext.media.timg.hdl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.media.timg.CartonBuilder;
import com.site0.walnut.ext.media.timg.CartonCtx;
import com.site0.walnut.ext.media.timg.TimgCarton;
import com.site0.walnut.ext.media.timg.builder.MoovCartonBuilder;
import com.site0.walnut.ext.media.timg.builder.NopCartonBuilder;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import org.nutz.web.Webs.Err;

@JvmHdlParamArgs(value="cqn", regex="^(keep|debug|fmd5|force|silencemp3)$")
public class timg_create implements JvmHdl {
    
    protected static final Log log = Wlog.getCMD();
    
    public static Map<String, CartonBuilder> builders = new LinkedHashMap<>();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        List<TimgCarton> cartons = null;
        // 读取动态配置文件
        NutMap conf = new NutMap();
        {
            WnObj wobj = sys.io.fetch(null, Wn.normalizeFullPath("~/.timg/config", sys));
            if (wobj != null && wobj.len() > 0) {
                conf = sys.io.readJson(wobj, NutMap.class);
            }
        }
        if (hc.params.has("simple")) {
            // 简单模式   图片A,图片B,转场效果,时长
            String[] simple = hc.params.get("simple").split(",");
            cartons = new ArrayList<>();
            String cartonName = simple[2];
            if ("效果测试".equals(cartonName)) {
                for (String cname : builders.keySet()) {
                    TimgCarton first = new TimgCarton();
                    first.cartonName = cname;
                    first.cartonTime = Integer.parseInt(simple[3]);
                    first.path = simple[cartons.size() % 2];
                    cartons.add(first);
                }
            }
            else {
                TimgCarton first = new TimgCarton();
                first.cartonName = cartonName;
                first.cartonTime = Integer.parseInt(simple[3]);
                first.path = simple[0];
                cartons.add(first);
                TimgCarton next = new TimgCarton();
                next.cartonName = simple[2];
                next.path = simple[1];
                cartons.add(next);
            }
        }
        else {
            // 详细模式
            cartons = Json.fromJsonAsList(TimgCarton.class, sys.in.getReader());
            if (cartons == null || cartons.isEmpty()) {
                sys.err.print("e.cmd.timg.create.need_data");
                return;
            }
        }
        if (hc.params.is("debug"))
            sys.out.writeJson(cartons);
        // 先检查所有素材是否完整
        for (TimgCarton carton : cartons) {
            carton.wobj = sys.io.check(null, Wn.normalizeFullPath(carton.path, sys));
            carton.builder = builders.get(carton.cartonName);
            if (carton.builder == null) {
                sys.err.print("尚不支持的转场效果: " + carton.cartonName);
                return;
            }
        }
        // 构建上下文
        String name = hc.params.get("name");
        if (Strings.isBlank(name)) {
            if (hc.params.is("simple")) {
                TimgCarton first = cartons.get(0);
                name = String.format("%s/%s/%s/%s毫秒", first.cartonName, first.wobj.sha1(), cartons.get(1).wobj.sha1(), first.cartonTime);
            }
            else {
                name = R.UU32();
            }
        }

        WnObj wobj = sys.io.createIfExists(null, Wn.normalizeFullPath("~/.timg/" + name + "/_timg.mp4", sys), WnRace.FILE);
        if (wobj.len() > 1024 && !hc.params.is("force")) {
            sys.out.writeJson(new NutMap("path", wobj.path()), Cmds.gen_json_format(hc.params));
            return;
        }
        
        CartonCtx ctx = new CartonCtx();
        ctx.cartons = cartons;
        ctx.tmpDir = "/tmp/" + R.UU32(); // 本地临时路径
        ctx.fps = hc.params.getInt("fps", conf.getInt("fps", 24)); // 图片生成的帧率总是25
        ctx.w = hc.params.getInt("w");
        ctx.h = hc.params.getInt("h");
        ctx.io = sys.io;
        ctx.conf = conf;
        ctx.videoFrame = hc.params.getInt("vframe", 24+12); // 假设2秒视频吧
        // 逐个效果操作
        for (int i = 0; i < cartons.size(); i++) {
            ctx.cur = cartons.get(i);
            ctx.next = cartons.get(0);
            if (i < cartons.size() - 1)
                ctx.next = cartons.get(i+1);
            ctx.index = i;
            ctx.cur.builder.invoke(ctx);
        }
        // 构建ffmpeg参数
        List<String> args = new ArrayList<>();
        if (Wlang.isWin()) {
            args.add("ffmpeg.exe");
        }
        else {
            args.add("ffmpeg");
        }
        // -r 25 -i uobm2jbg5ojj1qh0vpq8am5frn/images/T%06d.jpg -r 24 -y uobm2jbg5ojj1qh0vpq8am5frn.mp4
        args.add("-v");
        args.add("quiet");
        args.add("-r");
        args.add(""+ctx.fps);
        args.add("-i");
        args.add(ctx.tmpDir + "/images/T%06d.png");
        if (conf.getBoolean("silencemp3", false)) {
            args.add("-i");
            args.add(ctx.tmpDir + "/Silence01min.mp3");
            Files.write(ctx.tmpDir + "/Silence01min.mp3", getClass().getClassLoader().getResourceAsStream("Silence01min.mp3"));
            args.add("-t");
            int t = (new File(ctx.tmpDir + "/images/").list().length+23);
            args.add(String.format("00:%02d:%02d.00", t/24/24, t/ 24));
        }
        else if (conf.has("mp3")) {
            args.add("-i");
            args.add(ctx.tmpDir + "/Silence01min.mp3");
            Files.write(ctx.tmpDir + "/Silence01min.mp3", sys.io.getInputStream(sys.io.check(null, Wn.normalizeFullPath(conf.getString("mp3"), sys)), 0));
            args.add("-t");
            int t = (new File(ctx.tmpDir + "/images/").list().length+23);
            args.add(String.format("00:%02d:%02d.00", t/24/24, t/ 24));
        }
        args.add("-r");
        args.add(""+ctx.fps);
        args.add("-b:v");
        args.add(conf.getString("bv", "12000k"));
        args.add("-y");
        args.add("-pix_fmt");
        args.add("yuv420p");
        args.add("-movflags");
        args.add("faststart");
        args.addAll(Arrays.asList("-profile:v main -level 4.0".split(" ")));
        args.add(ctx.tmpDir + "/timg.mp4");
        // 开始转视频
        log.info("启动: " + Strings.join(" ", args));
        Wlang.execOutput(args.toArray(new String[args.size()]));
        log.info("完成: " + Strings.join(" ", args));
        // 写到walnut里面去
        File f = new File(ctx.tmpDir + "/timg.mp4");
        try (InputStream ins = new FileInputStream(f)) {
            sys.io.writeAndClose(wobj, ins);
        }
        // 算md5
        if (hc.params.is("fmd5")) {
            NutMap metas = new NutMap();
            metas.put("fmd5", Wlang.md5(f));
            metas.put("smd5", simpleMd5(f));
            sys.io.appendMeta(wobj, metas);
        }
        // 清理临时文件
        if (!hc.params.is("keep"))
            Files.deleteDir(new File(ctx.tmpDir));
        // 返回结果
        sys.out.writeJson(new NutMap("path", wobj.path()), Cmds.gen_json_format(hc.params));
    }

    static {
        builders.put("淡入淡出", MoovCartonBuilder.Builder.create(true).moov(0, 0, 0, 0).alpha(0, 1).build());
        builders.put("左入", MoovCartonBuilder.Builder.create(true).moov(0, 0, -1, 0).build());
        builders.put("右入", MoovCartonBuilder.Builder.create(true).moov(0, 0, 1, 0).build());
        builders.put("上入", MoovCartonBuilder.Builder.create(true).moov(0, 0, 0, -1).build());
        builders.put("下入", MoovCartonBuilder.Builder.create(true).moov(0, 0, 0, 1).build());
        
        builders.put("左入右出", MoovCartonBuilder.Builder.create(true).moov(1, 0, -1, 0).build());
        builders.put("右入左出", MoovCartonBuilder.Builder.create(true).moov(-1, 0, 1, 0).build());
        builders.put("上入下出", MoovCartonBuilder.Builder.create(true).moov(0, 1, 0, -1).build());
        builders.put("下入上出", MoovCartonBuilder.Builder.create(true).moov(0, -1, 0, 1).build());
        
        builders.put("左上入", MoovCartonBuilder.Builder.create(true).moov(0, 0, -1, 1).build());
        builders.put("右上入", MoovCartonBuilder.Builder.create(true).moov(0, 0, 1, 1).build());
        builders.put("左下入", MoovCartonBuilder.Builder.create(true).moov(0, 0, -1, -1).build());
        builders.put("右下入", MoovCartonBuilder.Builder.create(true).moov(0, 0, -1, 1).build());
        
        // 带淡出
        builders.put("左淡出", MoovCartonBuilder.Builder.create(false).moov(1, 0, 0, 0).alpha(-1, 0).build());
        builders.put("右淡出", MoovCartonBuilder.Builder.create(false).moov(-1, 0, 0, 0).alpha(-1, 0).build());
        builders.put("上淡出", MoovCartonBuilder.Builder.create(false).moov(0, -1, 0, 0).alpha(-1, 0).build());
        builders.put("下淡出", MoovCartonBuilder.Builder.create(false).moov(0, 1, 0, 0).alpha(-1, 0).build());

        builders.put("无效果", new NopCartonBuilder());
    }
    
    
    // 移植Strato的md5算法
    public static String simpleMd5(File f) {
        if (f.length() <= 1024 * 1024) {
            return Wlang.md5(f);
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
            return Wlang.fixedHexString(md.digest());
        }
        catch (Exception e) {
            throw Err.wrap(e);
        }
        finally {
            Streams.safeClose(raf);
        }
    }
}
