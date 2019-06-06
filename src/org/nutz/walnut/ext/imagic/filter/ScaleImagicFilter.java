package org.nutz.walnut.ext.imagic.filter;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.nutz.lang.Strings;
import org.nutz.walnut.ext.imagic.ImagicFilter;

import net.coobird.thumbnailator.Thumbnails;

public class ScaleImagicFilter implements ImagicFilter {

    @Override
    public BufferedImage doChain(BufferedImage image, String _args) throws IOException {
        Thumbnails.Builder<BufferedImage> holder = Thumbnails.of(image);
        String[] tmp = Strings.splitIgnoreBlank(_args);
        if (tmp[0].contains(".")) {
            if (tmp.length == 1) {
                holder.scale(Double.parseDouble(tmp[0]));
            } else if (tmp.length == 2) {
                holder.scale(Double.parseDouble(tmp[0]), Double.parseDouble(tmp[1]));
            }
        } else {
            holder.size(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]));
        }
        return holder.asBufferedImage();
    }

}
