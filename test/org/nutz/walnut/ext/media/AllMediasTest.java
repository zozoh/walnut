package org.nutz.walnut.ext.media;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.ext.media.edi.bean.EdiMsgPackTest;
import org.nutz.walnut.ext.media.ooml.AllOomlTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllOomlTest.class, EdiMsgPackTest.class})
public class AllMediasTest {}
