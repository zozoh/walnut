package com.site0.walnut.core.indexer.vofs;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Test;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.indexer.WnVirtualFsIndexerTest;
import com.site0.walnut.ext.xo.builder.AbstractXoClientBuilder;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.ext.xo.util.XoClientWrapper;

public abstract class AbstractVofsIndexerTest<T> extends WnVirtualFsIndexerTest {

    private XoClientWrapper<T> _client;

    protected abstract AbstractXoClientBuilder<T> _builder(WnIo io);

    protected abstract XoService _make_api(XoClientWrapper<T> _client);

    protected WnIoIndexer _get_indexer() {
        WnIo io = setup.getRawIo();
        MimeMap mimes = setup.getMimes();
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
        WnObj oDir = io.createIfNoExists(null, "/test/mntdir", WnRace.DIR);
        XoService api = _make_api(_client);
        api.clear("*");
        return new VofsIndexer(oDir, mimes, api);
    }

    @Test
    public void test_get_query_prefix() {
        indexer.create(null, "/a/a.js", WnRace.FILE);
        WnObj p = indexer.check(null, "a/");
        assertEquals(VID("a/"), p.id());

        String prefix;

        prefix = ((VofsIndexer) indexer).get_query_prefix(p, null);
        assertEquals("a/", prefix);

        prefix = ((VofsIndexer) indexer).get_query_prefix(p, "a.js");
        assertEquals("a/a.js", prefix);

        prefix = ((VofsIndexer) indexer).get_query_prefix(p, "b/");
        assertEquals("a/b/", prefix);
    }

}
