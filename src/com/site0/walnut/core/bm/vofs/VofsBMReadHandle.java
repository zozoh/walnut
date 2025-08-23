package com.site0.walnut.core.bm.vofs;

import java.io.InputStream;

import com.site0.walnut.core.bm.WnIoReadHandle;
import com.site0.walnut.util.Wobj;

public class VofsBMReadHandle extends WnIoReadHandle {

    private VofsBM bm;

    VofsBMReadHandle(VofsBM bm) {
        this.bm = bm;
    }

    @Override
    protected InputStream getInputStream() {
        String myId = obj.OID().getMyId();
        String key = Wobj.decodePathFromBase64(myId);
        return bm.api.read(key);
    }

}
