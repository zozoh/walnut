package com.site0.walnut.core.mapping.bm;

import java.io.File;

import org.nutz.lang.Files;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.bm.localfile.LocalFileWBM;
import com.site0.walnut.core.mapping.WnBMFactory;

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
