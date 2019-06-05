package org.nutz.walnut.ext.wnimg.filter;

import java.awt.image.*;

import net.coobird.thumbnailator.Thumbnails;

public interface WnImgFilter {

    void doChain(Thumbnails.Builder<BufferedImage> holder, String _args);
}
