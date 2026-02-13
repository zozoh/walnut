package com.site0.walnut.ext.media.edi.loader;


import com.site0.walnut.ext.media.edi.bean.EdiInterchange;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
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

        // MOA+9:0000000000000.00'
        Map<String, String> m2 = moaList.get(2);
        assertEquals("9", m2.get("amtCode"));
        assertEquals("0", m2.get("amtValue"));

        // MOA+58:0000000000000.00'
        Map<String, String> m3 = moaList.get(3);
        assertEquals("58", m3.get("amtCode"));
        assertEquals("0", m3.get("amtValue"));

        // MOA+149:0000000000000.00'
        Map<String, String> m4 = moaList.get(4);
        assertEquals("149", m4.get("amtCode"));
        assertEquals("0", m4.get("amtValue"));

        // MOA+369:0000000000405.58'
        Map<String, String> m5 = moaList.get(5);
        assertEquals("369", m5.get("amtCode"));
        assertEquals("405.58", m5.get("amtValue"));

        // MOA+371:0000000000000.00'
        Map<String, String> m6 = moaList.get(6);
        assertEquals("371", m6.get("amtCode"));
        assertEquals("0", m6.get("amtValue"));

        // MOA+26:0000000000049.00'
        Map<String, String> m7 = moaList.get(7);
        assertEquals("26", m7.get("amtCode"));
        assertEquals("49", m7.get("amtValue"));

        // MOA+206:0000000000000.00'
        Map<String, String> m8 = moaList.get(8);
        assertEquals("206", m8.get("amtCode"));
        assertEquals("0", m8.get("amtValue"));

        // MOA+304:0000000000000.00'
        Map<String, String> m9 = moaList.get(9);
        assertEquals("304", m9.get("amtCode"));
        assertEquals("0", m9.get("amtValue"));

        // MOA+128:0000000000504.58'
        Map<String, String> mLast = moaList.get(10);
        assertEquals("128", mLast.get("amtCode"));
        assertEquals("504.58", mLast.get("amtValue"));


    }

    // 对应报文 payrec_res_02.edi.txt
    @Test
    public void test_PAYREC_SuccessRes_02() {
        String input = _read_input("payrec_res_02");

        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        PAYRECLoader loader = EdiMsgs.getPAYRECLoader();
        PayRecRes re = loader.load(msg);

        System.out.println(Json.toJson(re, JsonFormat.full()));

        assertEquals("PAYREC", re.getMsgType());
        assertEquals("20230904", re.getPaymentDate());
        assertEquals("229037151", re.getBankAccNum());
        assertEquals("MASTER LOGISTICS PTY LTD", re.getBankAccName());
        assertEquals("33380054835", re.getAbn());
        assertEquals("082356", re.getBsbNum());
        assertEquals("CHL9469934R", re.getImporterId());
        assertEquals("FJM396H", re.getMsgRecipient());
        assertEquals("AF46JR", re.getBranchId());

        // RFF
        assertEquals("B00058603/1/PRD2", re.getRefId());
        assertEquals("b00058603/1/prd2", re.getRefIdInLower());
        assertEquals(2, re.getRefVer());

        // RFF+ABQ:SHL23038-1'
        assertEquals("SHL23038-1", re.getImporterRef());
        // RFF+ADU:B00058603'
        assertEquals("B00058603", re.getBrokerRef());
        // RFF+ABT:AE3AHWF4H'
        assertEquals("AE3AHWF4H", re.getImpDecNum());
        // RFF+RA:AE3AHWGEK'
        assertEquals("AE3AHWGEK", re.getBankReceiptNum());

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

        // MOA+9:0000000000000.00'
        Map<String, String> m2 = moaList.get(2);
        assertEquals("9", m2.get("amtCode"));
        assertEquals("0", m2.get("amtValue"));

        // MOA+58:0000000000000.00'
        Map<String, String> m3 = moaList.get(3);
        assertEquals("58", m3.get("amtCode"));
        assertEquals("0", m3.get("amtValue"));

        // MOA+149:0000000000000.00'
        Map<String, String> m4 = moaList.get(4);
        assertEquals("149", m4.get("amtCode"));
        assertEquals("0", m4.get("amtValue"));

        // MOA+369:0000000000506.15'
        Map<String, String> m5 = moaList.get(5);
        assertEquals("369", m5.get("amtCode"));
        assertEquals("506.15", m5.get("amtValue"));

        // MOA+371:0000000000000.00'
        Map<String, String> m6 = moaList.get(6);
        assertEquals("371", m6.get("amtCode"));
        assertEquals("0", m6.get("amtValue"));

        // MOA+26:0000000000063.00'
        Map<String, String> m7 = moaList.get(7);
        assertEquals("26", m7.get("amtCode"));
        assertEquals("63", m7.get("amtValue"));

        // MOA+206:0000000000000.00'
        Map<String, String> m8 = moaList.get(8);
        assertEquals("206", m8.get("amtCode"));
        assertEquals("0", m8.get("amtValue"));

        // MOA+304:0000000000000.00'
        Map<String, String> m9 = moaList.get(9);
        assertEquals("304", m9.get("amtCode"));
        assertEquals("0", m9.get("amtValue"));

        // MOA+128:0000000000619.15'
        Map<String, String> mLast = moaList.get(10);
        assertEquals("128", mLast.get("amtCode"));
        assertEquals("619.15", mLast.get("amtValue"));
    }
}
