package org.nutz.walnut.ext.imagic.filter;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.nutz.img.Images;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.imagic.ImagicFilter;

public class ContainsImagicFilter implements ImagicFilter {

    @Override
    public BufferedImage doChain(BufferedImage sourceImage, String _args) throws IOException {
        String[] tmp = Strings.splitIgnoreBlank(_args);
        return Images.zoomScale(sourceImage, Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Color.WHITE);
    }

}
