package com.site0.walnut.ext.media;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.ext.media.edi.bean.EdiInterchangeTest;
import com.site0.walnut.ext.media.ooml.AllOomlTest;
import com.site0.walnut.ext.media.sheet.AllSheetTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllOomlTest.class,
                     EdiInterchangeTest.class,
                     AllSheetTest.class})
public class AllMediasTest {}
