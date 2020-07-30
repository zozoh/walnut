package org.nutz.walnut.core.mapping.bm;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoHandleManager;
import org.nutz.walnut.core.bm.localfile.LocalFileWBM;
import org.nutz.walnut.core.mapping.WnBMFactory;

public class LocalFileWBMFactory implements WnBMFactory {

    private WnIoHandleManager handles;

    public LocalFileWBMFactory() {}

    public LocalFileWBMFactory(WnIoHandleManager handles) {
        this.handles = handles;
    }

    public WnIoHandleManager getHandles() {
        return handles;
    }

    public void setHandles(WnIoHandleManager handles) {
        this.handles = handles;
    }

    @Override
    public WnIoBM load(WnObj oHome, String str) {
        File f = Files.findFile(str);
        return new LocalFileWBM(handles, f);
    }

}
