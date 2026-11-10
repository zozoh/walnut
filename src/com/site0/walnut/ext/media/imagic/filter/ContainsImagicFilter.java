package com.site0.walnut.ext.media.imagic.filter;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.nutz.img.Images;
import com.site0.walnut.util.Ws;
import com.site0.walnut.ext.media.imagic.ImagicFilter;

public class ContainsImagicFilter implements ImagicFilter {

    @Override
    public BufferedImage doChain(BufferedImage sourceImage, String _args) throws IOException {
        String[] tmp = Ws.splitIgnoreBlank(_args);
        return Images.zoomScale(sourceImage, Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Color.WHITE);
    }

}
