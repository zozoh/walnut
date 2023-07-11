package org.nutz.walnut.ext.media;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.ext.media.edi.bean.EdiInterchangeTest;
import org.nutz.walnut.ext.media.ooml.AllOomlTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllOomlTest.class, EdiInterchangeTest.class})
public class AllMediasTest {}
