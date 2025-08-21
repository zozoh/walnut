package com.site0.walnut.core.bm.vofs;

import java.io.InputStream;

import com.site0.walnut.core.bm.WnIoReadHandle;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.util.Wobj;

public class VofsBMReadHandle extends WnIoReadHandle {

    private XoService api;

    public VofsBMReadHandle(XoService api) {
        this.api = api;
    }

    @Override
    protected InputStream getInputStream() {
        String myId = obj.OID().getMyId();
        String key = Wobj.decodePathFromBase64(myId);
        return api.read(key);
    }

}
