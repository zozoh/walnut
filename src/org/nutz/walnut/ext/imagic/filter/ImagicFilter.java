package org.nutz.walnut.ext.imagic.filter;

import java.awt.image.*;

import net.coobird.thumbnailator.Thumbnails;

public interface ImagicFilter {

    void doChain(Thumbnails.Builder<BufferedImage> holder, String _args);
}
