package com.site0.walnut.core.bm.bmv;

import java.io.IOException;

import org.junit.Before;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.core.AbstractWnIoBMTest;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.ext.xo.builder.AbstractXoClientBuilder;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.ext.xo.util.XoClientWrapper;
import com.site0.walnut.util.Wn;

public abstract class AbstractVXDataSignBMTest<T> extends AbstractWnIoBMTest {

    private XoClientWrapper<T> _client;

    protected abstract AbstractXoClientBuilder<T> _builder(WnIo io);

    protected abstract XoService _make_api(XoClientWrapper<T> _client);

    @Before
    public void setup() throws Exception {
        WnIo io = setup.getIo();
        this.setup.cleanAllData();

        // 获取客户端
        if (null == _client) {
            // 创建一个目录，用来做映射的根
            AbstractXoClientBuilder<T> builder = _builder(io);
            // 创建客户端
            try {
                _client = builder.build();
            }
            catch (IOException e) {
                throw Er.wrap(e);
            }
        }

        XoService api = _make_api(_client);
        api.clear("*");

        indexer = setup.getGlobalIndexer();
        bm = setup.getVxDataSignBM(api);

        o = new WnIoObj();
        o.id(Wn.genId());
    }

    @Override
    protected String getObjSha1ForTest(String sha1) {
        return o.sha1();
    }

}