package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiInterchange;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.msg.reply.pay.RefundAdv;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;

import static org.junit.Assert.*;

public class REFACCLoaderTest {

    private String _read_input(String name) {
        String txt = Files.read("com/site0/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }


    @Test
    public void test_refundRes_01() {
        //refund_res_01.edi.txt
        String input = _read_input("refund_res_01");

        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        REFACCLoader loader = EdiMsgs.getREFACCLoader();
        RefundAdv re = loader.load(msg);
        System.out.println(Json.toJson(re, JsonFormat.full()));

        assertNotNull(re);
        assertEquals(0, re.getRstVer());
        assertEquals("ISMEL4106", re.getDrawbackId());
        assertEquals("FJM396H", re.getMsgRecipient());
        assertEquals("AF46JR", re.getBranchId());
        assertEquals("MASTER LOGISTICS PTY LTD", re.getBrokerName());
        assertEquals("B00058894/1/PRD1", re.getRefId());
        assertEquals("b00058894/1/prd1", re.getRefIdInLower());
        assertEquals(2, re.getRefVer());
        assertEquals("AE3AFP7X4", re.getImpDecNum());
        assertEquals("2", re.getImpDecNumVer());
        assertEquals("B00058894", re.getBrokerRef());
    }

    //refund_res_02.edi.txt
    @Test
    public void test_refundRes_02() {
        String input = _read_input("refund_res_02");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        REFACCLoader loader = EdiMsgs.getREFACCLoader();
        RefundAdv re = loader.load(msg);

        assertNotNull(re);
        assertEquals(0, re.getRstVer());
        assertEquals("KM23016-1", re.getDrawbackId());
        assertEquals("FJM396H", re.getMsgRecipient());
        assertEquals("AF46JR", re.getBranchId());
        assertEquals("MASTER LOGISTICS PTY LTD", re.getBrokerName());
        assertEquals("B00058425/1/PRD1", re.getRefId());
        assertEquals("b00058425/1/prd1", re.getRefIdInLower());
        assertEquals(4, re.getRefVer());
        assertEquals("AE3ATFJAA", re.getImpDecNum());
        assertEquals("3", re.getImpDecNumVer());
        assertEquals("B00058425", re.getBrokerRef());
    }

    //refund_res_03.edi.txt
    @Test
    public void test_refundRes_03() {
        String input = _read_input("refund_res_03");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        REFACCLoader loader = EdiMsgs.getREFACCLoader();
        RefundAdv re = loader.load(msg);

        assertNotNull(re);
        assertEquals(0, re.getRstVer());
        assertEquals("ISMEL4159E", re.getDrawbackId());
        assertEquals("FJM396H", re.getMsgRecipient());
        assertEquals("AF46JR", re.getBranchId());
        assertEquals("MASTER LOGISTICS PTY LTD", re.getBrokerName());
        assertEquals("B00060706/1/PRD1", re.getRefId());
        assertEquals("b00060706/1/prd1", re.getRefIdInLower());
        assertEquals(2, re.getRefVer());
        assertEquals("AE3J3CNXG", re.getImpDecNum());
        assertEquals("2", re.getImpDecNumVer());
        assertEquals("B00060706", re.getBrokerRef());
    }


}
