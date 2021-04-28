package org.nutz.walnut.ext.media.imagic;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface ImagicFilter {

    BufferedImage doChain(BufferedImage sourceImage, String _args) throws IOException;
}
