package org.nutz.walnut.ext.imagic.filter;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.nutz.walnut.ext.imagic.ImagicFilter;

import net.coobird.thumbnailator.Thumbnails;

public class RotateImagicFilter implements ImagicFilter {

    @Override
    public BufferedImage doChain(BufferedImage image, String _args) throws NumberFormatException, IOException {
        return Thumbnails.of(image).rotate(Double.parseDouble(_args)).size(image.getWidth(), image.getHeight()).asBufferedImage();
    }

}
