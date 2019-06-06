package org.nutz.walnut.ext.imagic;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface ImagicFilter {

    BufferedImage doChain(BufferedImage sourceImage, String _args) throws IOException;
}
