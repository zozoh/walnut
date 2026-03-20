package com.site0.walnut.ext.media.edi;

import com.site0.walnut.ext.media.edi.bean.EdiInterchangeTest;
import com.site0.walnut.ext.media.edi.loader.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({EdiInterchangeTest.class, UbmInterchangeTest.class, ImdReplyTest.class,
        PAYRECMsgTest.class, ATDMsgTest.class, REFACCMsgTest.class})
public class AllEdiTest {
}
