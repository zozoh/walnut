package org.nutz.walnut.ext.ml.hdl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import org.nutz.img.Images;
import org.nutz.plugins.ml.image.MlImages;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class ml_image999 implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        int block_w = hc.params.getInt("block_w", 32);
        int block_h = hc.params.getInt("block_h", 32);
        int x = hc.params.getInt("x", 0);
        int y = hc.params.getInt("y", 0);
        int gray_min = hc.params.getInt("gray_min", 248);
        String label = hc.params.getString("label", "999");
        BufferedImage image = null;
        if (hc.params.vals.length > 0) {
            WnObj wobj = sys.io.check(null, Wn.normalizeFullPath(hc.params.val(0), sys));
            try (InputStream ins = sys.io.getInputStream(wobj, 0)) {
                image = Images.read(ins);
            }
            catch (IOException e) {
            }
        } else {
            image = Images.read(sys.in.getInputStream());
        }
        if (image == null) {
            sys.err.println("bad image");
            return;
        }
        
        // 全图转灰度
        int[][] gray = MlImages.toGray(image, x, y, image.getWidth(), image.getHeight());
        // 按块大小计算块内的平均灰度
        int[][] gray_avg = MlImages.gray_avg(gray, block_w, block_h);
        // 根据阀值输出0/1矩阵
        boolean[][] gray_bol = MlImages.gray_bol(gray_avg, gray_min);
        gray_bol = MlImages.gray_bol_ci(gray_bol);
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < gray_bol[0].length; j++) {
            for (int i = 0; i < gray_bol.length; i++) {
                if (gray_bol[i][j])
                    sb.append(label);
                else
                    sb.append("0");
                sb.append(' ');
            }
            sb.append("\r\n");
        }
        sys.out.print(sb.toString());
    }

}
