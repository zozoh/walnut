package com.site0.walnut.ext.data.esi.hdl;

import org.nutz.ioc.Ioc;
import com.site0.walnut.ext.data.esi.ElasticsearchService;
import com.site0.walnut.ext.data.esi.EsiConf;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public abstract class esi_xxx implements JvmHdl {

    protected ElasticsearchService _service;

    public ElasticsearchService esi(Ioc ioc) {
        if (_service == null)
            _service = ioc.get(ElasticsearchService.class);
        return _service;
    }

    public EsiConf conf(WnSystem sys, JvmHdlContext hc) {
        return (EsiConf) hc.get("conf");
    }
}
