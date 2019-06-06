package org.nutz.walnut.ext.imagic.filter;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.nutz.lang.Strings;
import org.nutz.walnut.ext.imagic.ImagicFilter;
import org.nutz.web.Webs.Err;

public class ClipImagicFilter implements ImagicFilter {

    @Override
    public BufferedImage doChain(BufferedImage image, String _args) throws IOException {
        String[] tmp = Strings.splitIgnoreBlank(_args);
        if (tmp.length != 4) {
            throw Err.create("e.cmd.imagic.clip.need_4_args");
        }
        int sizeX = Integer.parseInt(tmp[2]);
        int sizeY = Integer.parseInt(tmp[3]);
        int posX = 0;
        int posY = 0;
        if (tmp[0].contains(".")) {
            posX = (int) (image.getWidth() * Float.parseFloat(tmp[0]));
            posY = (int) (image.getHeight() * Float.parseFloat(tmp[1]));
        } else {
            posX = Integer.parseInt(tmp[0]);
            posY = Integer.parseInt(tmp[1]);
        }
        return image.getSubimage(posX, posY, sizeX, sizeY);
    }

}
