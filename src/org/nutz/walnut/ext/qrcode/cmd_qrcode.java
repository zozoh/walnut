package org.nutz.walnut.ext.qrcode;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.qrcode.QRCode;
import org.nutz.qrcode.QRCodeFormat;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_qrcode extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        // 分析参数
        ZParams params = ZParams.parse(args, "d");

        // 解码
        if (params.is("d")) {
            __do_decode(sys, params);
        }
        // 编码
        else {
            __do_encode(sys, params);
        }

    }

    private void __do_encode(WnSystem sys, ZParams params) throws IOException {

        // 当前仅支持输入字符串
        String content;

        // 从管道里读取
        if (sys.pipeId <= 0) {
            throw Er.create("e.cmd.qrcode.encode.noinput");
        }

        content = sys.in.readAll();

        // 生成的图片格式
        String fmt = params.get("fmt", "png");

        // icon
        String icon = params.get("icon");
        BufferedImage iconImg = null;
        if (!Strings.isBlank(icon)) {
            String iconPath = Wn.normalizeFullPath(icon, sys);
            iconImg = sys.io.readImage(sys.io.fetch(null, iconPath));
        }
        // 生成二维码
        QRCodeFormat qrcf = QRCodeFormat.NEW();
        qrcf.setErrorCorrectionLevel('M');
        qrcf.setSize(params.getInt("size", 256));
        qrcf.setImageFormat(fmt);
        qrcf.setIcon(iconImg);
        qrcf.setMargin(params.getInt("margin", 0));

        // 输出
        BufferedImage im = QRCode.toQRCode(content, qrcf);
        OutputStream ops = sys.out.getOutputStream();
        ImageIO.write(im, fmt, ops);

    }

    private void __do_decode(WnSystem sys, ZParams params) throws IOException {
        InputStream ins = null;

        try {
            // 从对象读取
            if (params.vals.length > 0) {
                String ph = params.vals[0];
                ph = Wn.normalizeFullPath(ph, sys);
                WnObj image = sys.io.check(null, ph);
                ins = sys.io.getInputStream(image, 0);
            }
            // 从管道读取
            else {
                ins = sys.in.getInputStream();
            }

            // 解析
            BufferedImage im = ImageIO.read(ins);
            String content = QRCode.from(im);

            // 输出
            if (params.is("n"))
                sys.out.println(content);
            else
                sys.out.print(content);
        }
        finally {
            Streams.safeClose(ins);
        }

    }
}
