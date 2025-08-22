package com.site0.walnut.core.bm;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.site0.walnut.core.bm.bml.LocalSha1BMTest;
import com.site0.walnut.core.bm.localfile.LocalFileBMTest;
import com.site0.walnut.core.bm.redis.RedisBMTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({Sha1PartsTest.class,
                     LocalSha1BMTest.class,
                     LocalFileBMTest.class,
                     RedisBMTest.class})
public class AllBMTest {}
