package com.site0.walnut.ext.media.edi.bean;


import com.site0.walnut.ext.media.edi.loader.EdiMsgs;
import com.site0.walnut.ext.media.edi.loader.PAYRECLoader;
import com.site0.walnut.ext.media.edi.msg.reply.pay.PayRecRes;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PAYRECMsgTest {
    private String _read_input(String name) {
        String txt = Files.read("com/site0/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }

    @Test
    public void test_PAYREC_SuccessRes_01() {
        String input = _read_input("payrec_res_01");

        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        PAYRECLoader loader = EdiMsgs.getPAYRECLoader();
        PayRecRes re = loader.load(msg);

        System.out.println(Json.toJson(re, JsonFormat.full()));

        assertEquals("PAYREC", re.getMsgType());
        assertEquals("20221219", re.getPaymentDate());
        assertEquals("530579", re.getBankAccNum());
        assertEquals("MASTER LOGISTICS", re.getBankAccName());
        assertEquals("33380054835", re.getAbn());
        assertEquals("032060", re.getBsbNum());
        assertEquals("38650842621", re.getImporterId());
        assertEquals("FJM396H", re.getMsgRecipient());
        assertEquals("AF46JR", re.getBranchId());

        // RFF
        assertEquals("B00044782/1/PRD1", re.getRefId());
        assertEquals("b00044782/1/prd1", re.getRefIdInLower());
        assertEquals(1, re.getRefVer());
        // RFF+ABQ:AXSYD22003279'
        assertEquals("AXSYD22003279", re.getImporterRef());
        // RFF+ADU:B00044782'
        assertEquals("B00044782", re.getBrokerRef());
        // RFF+ABT:AEWMK9NHW'
        assertEquals("AEWMK9NHW", re.getImpDecNum());
        // RFF+RA:AEWMK9PA9'
        assertEquals("AEWMK9PA9", re.getBankReceiptNum());

        // TAX/MOA
        List<Map<String, String>> moaList = re.getMoaList();
        assertNotNull(moaList);
        assertEquals(11, moaList.size());

        // MOA+7:0000000000000.00'
        Map<String, String> m0 = moaList.get(0);
        assertEquals("7", m0.get("amtCode"));
        assertEquals("0", m0.get("amtValue"));

        // MOA+23:0000000000050.00'
        Map<String, String> m1 = moaList.get(1);
        assertEquals("23", m1.get("amtCode"));
        assertEquals("50", m1.get("amtValue"));

        // MOA+369:0000000000405.58'
        Map<String, String> m5 = moaList.get(5);
        assertEquals("369", m5.get("amtCode"));
        assertEquals("405.58", m5.get("amtValue"));

        // MOA+128:0000000000504.58'
        Map<String, String> mLast = moaList.get(10);
        assertEquals("128", mLast.get("amtCode"));
        assertEquals("504.58", mLast.get("amtValue"));

    }
}
