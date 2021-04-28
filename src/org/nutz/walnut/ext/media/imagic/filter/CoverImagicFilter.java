package org.nutz.walnut.ext.media.imagic.filter;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.nutz.img.Images;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.media.imagic.ImagicFilter;

public class CoverImagicFilter implements ImagicFilter {

    @Override
    public BufferedImage doChain(BufferedImage image, String _args) throws NumberFormatException, IOException {
        String[] tmp = Strings.splitIgnoreBlank(_args);
        return Images.clipScale(image, Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]));
    }

}
