package org.nutz.walnut.ext.data.esi.hdl;

import org.nutz.ioc.Ioc;
import org.nutz.walnut.ext.data.esi.ElasticsearchService;
import org.nutz.walnut.ext.data.esi.EsiConf;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

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
