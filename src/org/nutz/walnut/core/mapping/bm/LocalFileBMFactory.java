package org.nutz.walnut.core.mapping.bm;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoHandleManager;
import org.nutz.walnut.core.bm.localfile.WriteableLocalFileBM;
import org.nutz.walnut.core.mapping.WnBMFactory;

public class LocalFileBMFactory implements WnBMFactory {

    private WnIoHandleManager handles;

    public LocalFileBMFactory(WnIoHandleManager handles) {
        this.handles = handles;
    }

    @Override
    public WnIoBM load(String homeId, String str) {
        File f = Files.findFile(str);
        return new WriteableLocalFileBM(handles, f);
    }

}
