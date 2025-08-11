package com.site0.walnut.val;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.site0.walnut.val.date.UTCDateMakerTest;
import com.site0.walnut.val.date.UTCTimestampMakerTest;
import com.site0.walnut.val.id.WnSnowQMakerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnSnowQMakerTest.class, UTCDateMakerTest.class, UTCTimestampMakerTest.class})
public class AllValMakerTest {}
