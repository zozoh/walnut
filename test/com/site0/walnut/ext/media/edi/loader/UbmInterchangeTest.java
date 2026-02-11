package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiInterchange;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.msg.reply.ubm.IcsReplyUbmErr;
import com.site0.walnut.ext.media.edi.msg.reply.ubm.IcsReplyUbmRes;
import com.site0.walnut.ext.media.edi.msg.reply.ubm.UbmLineRst;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;

import java.util.List;

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

        assertEquals("UBMREQR", re.getMsgType());
        assertEquals(true, re.isSuccess());
        assertEquals("U00000337/PRD1", re.getRefId());
        assertEquals("u00000337/prd1", re.getRefIdInLower());
        assertEquals(1, re.getRefVer());
        // 对应 BGM 报文行,  11: Response, 8: Status, 32: Approval
        assertEquals(32, re.getFuncCode());

        assertEquals("20240612215114156647", re.getTxnTime());
        assertEquals("FJM396HU00000337/PRD1", re.getCargoMsgId());
        assertEquals("ROA", re.getInTrans().get("mvMode"));
        assertEquals("2403S", re.getMainTrans().get("voyNum"));
        assertEquals("9953846", re.getMainTrans().get("vesselId"));
        assertEquals("11", re.getMainTrans().get("transType"));

        assertEquals("GE65A", re.getLocInfo().get("discEstId"));
        assertEquals("FM27N", re.getLocInfo().get("oriEstId"));

        assertEquals("UNDERBOND APPROVAL", re.getUbmNotice());
        assertEquals("DCL", re.getReqReason());

        List<UbmLineRst> lineRsts = re.getLineRsts();
        assertEquals(1, lineRsts.size());

        UbmLineRst ubmLineRst = lineRsts.get(0);
        assertEquals("FCL", ubmLineRst.getCargoTp());
        assertEquals("YC", ubmLineRst.getPkgTp());
        assertEquals("0000001", ubmLineRst.getPkgNum());
        assertEquals("FBLU0238434", ubmLineRst.getCntrNum());

    }

    @Test
    public void test_UBMResLoader_02() {
        String input = _read_input("ubm_res_02");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        UBMResLoader loader = EdiMsgs.getUBMResLoader();
        IcsReplyUbmRes re = loader.load(msg);
        System.out.println(Json.toJson(re, JsonFormat.full()) );
    }
}
