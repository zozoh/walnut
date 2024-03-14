package com.site0.walnut.ext.media.timg;

import java.util.List;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;

public class CartonCtx {

    public List<TimgCarton> cartons;
    public int index;
    public TimgCarton cur;
    public TimgCarton next;
    public int lastFrameIndex;
    public int fps;
    public String tmpDir;
    public WnIo io;
    public int w;
    public int h;
    public NutMap conf;
    public int videoFrame;
}
