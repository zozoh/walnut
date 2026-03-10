package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiInterchange;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.msg.reply.atd.IcsReplyAtdRes;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;

import static org.junit.Assert.assertEquals;

public class ATDMsgTest {

    private String _read_input(String name) {
        String txt = Files.read("com/site0/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }

    @Test
    public void test_Atd_Res_01() {
        String input = _read_input("atd_01");

        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        ATDResLoader loader = EdiMsgs.getATDResLoader();
        IcsReplyAtdRes re = loader.load(msg);
        //System.out.println(Json.toJson(re, JsonFormat.full()));

        assertEquals(true, re.isSuccess());

        assertEquals("20260308", re.getClearDate());
        assertEquals("20260308", re.getPaymentDate());
        assertEquals("FINALISED", re.getAtdStatus().get("FINALISED"));
        assertEquals("A", re.getTransportMode());
        assertEquals("QF", re.getAirlineCode());
        assertEquals("AUSYD", re.getDischargePort());
        assertEquals("FKG373X", re.getMsgRecipient());
        assertEquals("AF46JR", re.getBranchId());
        assertEquals("5340", re.getBrokerLicNum());
        assertEquals("MASTER LOGISTICS PTY LTD", re.getBrokerName());
        assertEquals("HANGZHOU AORUI TRADING CO., LT", re.getImporterName());
        assertEquals("MMGHH5X6CHZTKSSK3M", re.getRefId());
        assertEquals("mmghh5x6chztkssk3m", re.getRefIdInLower());
        assertEquals("AFHCT3PJG", re.getCusDecNum());
        assertEquals("1", re.getCusDecVer());
        assertEquals("SHS1073038299", re.getImporterRef());
        assertEquals("X25192139", re.getBrokerRef());
        assertEquals("AFHCT3PKA", re.getDealSecCode());
        assertEquals("N10", re.getNatureType());

        assertEquals(Integer.valueOf(5), re.getCntCusDetailLine());
        assertEquals(Integer.valueOf(1), re.getCntLine());
        assertEquals(Integer.valueOf(0), re.getCntLineAndSubItem());

    }

    @Test
    public void test_Atd_Res_02() {
        String input = _read_input("atd_02");

        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        ATDResLoader loader = EdiMsgs.getATDResLoader();
        IcsReplyAtdRes re = loader.load(msg);
        System.out.println(Json.toJson(re, JsonFormat.full()));
        assertEquals(true, re.isSuccess());

        assertEquals("20240612", re.getClearDate());
        assertEquals("20240612", re.getPaymentDate());
        assertEquals("FINALISED", re.getAtdStatus().get("FINALISED"));

        assertEquals("S", re.getTransportMode());
        assertEquals("111S", re.getVoyageNum());
        assertEquals("9477878", re.getVesselId());
        assertEquals("AUMEL", re.getDischargePort());
        assertEquals(null, re.getWarehouseEstId());
        assertEquals("FJM396H", re.getMsgRecipient());
        assertEquals("AF46JR", re.getBranchId());

        assertEquals("5650", re.getBrokerLicNum());
        assertEquals("MASTER LOGISTICS PTY LTD", re.getBrokerName());
        assertEquals("360 INTERNATIONAL TRADING PTY", re.getImporterName());

        assertEquals("B00076191/1/PRD1", re.getRefId());
        assertEquals("b00076191/1/prd1", re.getRefIdInLower());
        assertEquals("AE7AK4L6W", re.getCusDecNum());
        assertEquals("1", re.getCusDecVer());
        assertEquals("CXF677", re.getImporterRef());

        assertEquals("B00076191", re.getBrokerRef());
        assertEquals("AE7AK4L74", re.getDealSecCode());
        assertEquals("N10", re.getNatureType());

        assertEquals(Integer.valueOf(3), re.getCntCusDetailLine());
        assertEquals(Integer.valueOf(3), re.getCntLine());
        assertEquals(Integer.valueOf(0), re.getCntLineAndSubItem());


    }
}
