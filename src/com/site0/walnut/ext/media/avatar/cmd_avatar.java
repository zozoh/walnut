package com.site0.walnut.ext.media.avatar;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.nutz.img.Images;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_avatar extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        String name = "";
        // 从参数里读取
        if (params.vals.length > 0) {
            name = params.vals[0];
        }
        // 从管道里读取
        else if (null != sys.in) {
            name = sys.in.readAll();
        }

        int size = params.getInt("s", 256);
        String fontColor = params.getString("fc", "#FFF");
        String bgColor = params.getString("bg", "#000");
        String fontName = params.getString("font", "");
        // 生成头像
        BufferedImage avImage = Images.createAvatar(name,
                                                    size,
                                                    fontColor,
                                                    bgColor,
                                                    fontName,
                                                    0,
                                                    Font.BOLD);
        OutputStream ops = sys.out.getOutputStream();
        ImageIO.write(avImage, "png", ops);
    }

}
