package com.site0.walnut.core.indexer.vofs;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.nutz.lang.util.NutMap;

import com.qcloud.cos.COSClient;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.IoCoreTest;
import com.site0.walnut.ext.xo.builder.CosClientBuilder;
import com.site0.walnut.ext.xo.impl.CosXoService;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.ext.xo.util.XoClientWrapper;

public class WnVofsIndexerTest extends IoCoreTest {

    private XoClientWrapper<COSClient> _client;

    // private WnIo io;
    // private MimeMap mimes;
    // private WnObj oDir;
    // private XoService api;
    //
    // private WnVofsIndexer indexer;
    
    @Before
    public void setUp() throws Exception {
        this.setup.cleanAllData();
        this.setup.getGlobalIndexer();
    }

    private WnVofsIndexer getIndexer() {
//        WnIo io = setup.getRawIo();
//        MimeMap mimes = setup.getMimes();
//        if (null == _client) {
//            // 创建一个目录，用来做映射的根
//            CosClientBuilder builder = new CosClientBuilder(io, "/test");
//            NutMap conf = new NutMap();
//            conf.put("secretId", setup.getConifg("cos-secret-id"));
//            conf.put("secretKey", setup.getConifg("cos-secret-key"));
//            conf.put("bucket", setup.getConifg("cos-bucket"));
//            conf.put("region", setup.getConifg("cos-region"));
//            conf.put("prefix", "mnt_home/");
//            builder.loadConfig(conf);
//            // 创建客户端
//            try {
//                _client = builder.build();
//            }
//            catch (IOException e) {
//                throw Er.wrap(e);
//            }
//        }
//        WnObj oDir = io.createIfNoExists(null, "/test/mntdir", WnRace.DIR);
//        XoService api = new CosXoService(_client);
//        api.clear("*");
//        WnVofsIndexer indexer = new WnVofsIndexer(oDir, mimes, api);
//
//        return indexer;
        return null;
    }

    @Test
    public void test() {
        WnVofsIndexer indexer = getIndexer();
        // 创建
        WnObj o = indexer.create(null, "foo/bar.txt", WnRace.FILE);

        // 读取
        WnObj o2 = indexer.fetch(null, "foo/bar.txt");

        assertTrue(o2.isFILE());
        assertEquals(o.id(), o2.id());
    }

}
