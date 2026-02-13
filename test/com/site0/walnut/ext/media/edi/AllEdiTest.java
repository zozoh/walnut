package com.site0.walnut.ext.media.edi;

import com.site0.walnut.ext.media.edi.bean.EdiInterchangeTest;
import com.site0.walnut.ext.media.edi.loader.ImdReplyTest;
import com.site0.walnut.ext.media.edi.loader.PAYRECMsgTest;
import com.site0.walnut.ext.media.edi.loader.UbmInterchangeTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({EdiInterchangeTest.class, UbmInterchangeTest.class, ImdReplyTest.class, PAYRECMsgTest.class})
public class AllEdiTest {
}
