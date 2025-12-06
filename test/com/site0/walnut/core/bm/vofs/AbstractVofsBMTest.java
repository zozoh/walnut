package com.site0.walnut.core.bm.vofs;

import java.io.IOException;

import org.junit.Before;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.AbstractWnIoBMTest;
import com.site0.walnut.core.indexer.vofs.VofsIndexer;
import com.site0.walnut.ext.xo.builder.AbstractXoClientBuilder;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.ext.xo.util.XoClientWrapper;

public abstract class AbstractVofsBMTest<T> extends AbstractWnIoBMTest {

    private XoClientWrapper<T> _client;

    protected abstract AbstractXoClientBuilder<T> _builder(WnIo io);

    protected abstract XoService _make_api(XoClientWrapper<T> _client);

    @Before
    public void setup() throws Exception {
        WnIo io = setup.getIo();
        MimeMap mimes = setup.getMimes();
        this.setup.cleanAllData();
        WnObj oDir = io.createIfNoExists(null, "/test/mntdir", WnRace.DIR);
        

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
        
        indexer = new VofsIndexer(oDir, mimes, api);
        bm = this.setup.getVofsBM(api);

        o = indexer.create(null, "/a/b.txt", WnRace.FILE);
    }

    @Override
    protected String getObjSha1ForTest(String sha1) {
        return o.sha1();
    }

}
