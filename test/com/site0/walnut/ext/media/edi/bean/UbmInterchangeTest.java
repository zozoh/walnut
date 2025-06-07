package com.site0.walnut.ext.media.edi.bean;

import com.site0.walnut.ext.media.edi.loader.CLNTDUPLoader;
import com.site0.walnut.ext.media.edi.loader.EdiMsgs;
import com.site0.walnut.ext.media.edi.loader.UBMErrLoader;
import com.site0.walnut.ext.media.edi.loader.UBMResLoader;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.msg.reply.clreg.IcsReplyCLNTDUP;
import com.site0.walnut.ext.media.edi.msg.reply.ubm.IcsReplyUbmErr;
import com.site0.walnut.ext.media.edi.msg.reply.ubm.IcsReplyUbmRes;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;

import static org.junit.Assert.assertEquals;

public class UbmInterchangeTest {
    private String _read_input(String name) {
        String txt = Files.read("com/site0/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }


    @Test
    public void test_UBMErrLoader_01() {
        String input = _read_input("ubm_err_01");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        UBMErrLoader loader = EdiMsgs.getUBMErrLoader();
        IcsReplyUbmErr re = loader.load(msg);
        // System.out.println(Json.toJson(re, JsonFormat.full()));

        assertEquals("UBMREQE", re.getMsgType());
        assertEquals(true, re.isSuccess());
        assertEquals("U00000337/PRD1", re.getRefId());
        assertEquals("u00000337/prd1", re.getRefIdInLower());
        assertEquals(1, re.getRefVer());
        assertEquals(11, re.getFuncCode());


        assertEquals("9", re.getInMsgFuncCode());
        assertEquals(2, re.getErrs().length);
        assertEquals(0, re.getErrCount());
        assertEquals("20240612115040", re.getMsgRcvTime());
        System.out.println(Json.toJson(re.getErrs()));

        EdiReplyError[] errs = re.getErrs();
        for (EdiReplyError err : errs) {
            if ("MS5202".equals(err.getCode())) {
                assertEquals("THIS TRANSACTION WAS ACCEPTED WITH ERRORS AND/OR WARNINGS", err.getContent());
            } else if ("CG1071".equals(err.getCode())) {
                assertEquals("DESTINATION ESTABLISHMENT ID IS NOT AUTOMATED", err.getContent());
            }
        }
    }

    @Test
    public void test_UBMResLoader_01() {
        String input = _read_input("ubm_res_01");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        UBMResLoader loader = EdiMsgs.getUBMResLoader();
        IcsReplyUbmRes re = loader.load(msg);
        System.out.println(Json.toJson(re, JsonFormat.full()) );
    }

}
