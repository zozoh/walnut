package com.site0.walnut.core.bm;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.site0.walnut.core.bm.bml.LocalSha1BMTest;
import com.site0.walnut.core.bm.bmv.CosVXDataSignBMTest;
import com.site0.walnut.core.bm.bmv.S3VXDataSignBMTest;
import com.site0.walnut.core.bm.localfile.LocalFileBMTest;
import com.site0.walnut.core.bm.redis.RedisBMTest;
import com.site0.walnut.core.bm.vofs.CosVofsBMTest;
import com.site0.walnut.core.bm.vofs.S3VofsBMTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({Sha1PartsTest.class,
                     LocalSha1BMTest.class,
                     LocalFileBMTest.class,
                     CosVofsBMTest.class,
                     S3VofsBMTest.class,
                     CosVXDataSignBMTest.class,
                     S3VXDataSignBMTest.class,
                     RedisBMTest.class})
public class AllBMTest {}
