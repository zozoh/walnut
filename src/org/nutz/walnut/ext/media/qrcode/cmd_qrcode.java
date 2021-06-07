package org.nutz.walnut.ext.media.qrcode;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class cmd_qrcode extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "dn");
        int size = params.getInt("size", 256);
        String fmt = params.getString("fmt", "png");
        int margin = params.getInt("margin", 1);

        // 内容,如果是编码就是文本,如果是解码,就是源文件
        String str;

        // 解码
        if (params.is("d")) {
            str = Cmds.getParamOrPipe(sys, params, 0);
            BufferedImage im;
            // 从文件读取
            if (!Ws.isBlank(str)) {
                WnObj oImg = Wn.checkObj(sys, str);
                im = sys.io.readImage(oImg);
            }
            // 从标准输入读取
            else {
                InputStream ins = sys.in.getInputStream();
                im = ImageIO.read(ins);
            }
            // 解析
            WnQrCode qr = new WnQrCode(im);

            // 输出
            if (params.is("n")) {
                sys.out.println(qr.getText());
            } else {
                sys.out.print(qr.getText());
            }
        }
        //
        // 编码
        //
        str = Cmds.checkParamOrPipe(sys, params, 0);
        WnQrCode qr = new WnQrCode(str, margin, size, size);
        // 容错级别
        if (params.has("c")) {
            qr.setCorrection(params.getString("c"));
        }
        // 图标
        String iconPath = params.getString("icon");
        if (!Ws.isBlank(iconPath)) {
            WnObj oIcon = Wn.checkObj(sys, iconPath);
            BufferedImage icon = sys.io.readImage(oIcon);
            int iconSize = params.getInt("icsz", -1);
            int iconPad = params.getInt("icpad", 4);
            qr.setIcon(icon);
            if (iconSize > 0) {
                qr.setIconWidth(iconSize);
                qr.setIconHeight(iconSize);
            }
            qr.setIconPadding(iconPad);
        }
        // 写入
        OutputStream ops = sys.out.getOutputStream();
        qr.writeTo(ops, fmt);
    }

}
