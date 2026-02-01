package com.site0.walnut.ext.media.edi.bean;

import com.site0.walnut.ext.media.edi.loader.EdiMsgs;
import com.site0.walnut.ext.media.edi.loader.IMDResLoader;
import com.site0.walnut.ext.media.edi.msg.reply.imd.IcsReplyImdRes;
import com.site0.walnut.ext.media.edi.msg.reply.imd.ImdReplyHeadErr;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ImdReplyTest {

    private String _read_input(String name) {
        String txt = Files.read("com/site0/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }

    @Test
    public void test_Imd_SuccessRes_01() {
        String input = _read_input("imd_res_01");

        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        IMDResLoader loader = EdiMsgs.getIMDResLoader();
        IcsReplyImdRes re = loader.load(msg);
        System.out.println(Json.toJson(re, JsonFormat.full()));

        assertEquals(11, re.getFuncCode());
        assertEquals(null, re.getDutyDate());

        Map<String, String> statusMap = re.getImdStatus();
        assertEquals("CLEAR", statusMap.get("CLEAR"));

        // GIS 相关
        assertEquals(false, re.isPreLodge());
        assertEquals(true, re.isLlb());
        assertEquals(true, re.isTlb());

        // NAD 相关
        assertEquals("FJM396H", re.getMsgRecipient());
        assertEquals("AF46JR", re.getBranchId());
        assertEquals("5650", re.getBrokerLicNum());
        assertEquals("MASTER LOGISTICS PTY LTD", re.getBrokerName());
        assertEquals(null, re.getBorkerBoxNum());
        assertEquals("THETA WORKS PTY LTD", re.getImporterName());
        assertEquals(null, re.getImporterBoxNum());

        // RFF 相关
        assertEquals("N10", re.getHeaderNatureType());
        assertEquals("B00072552/1/PRD1", re.getRefId());
        assertEquals("B00072552/1/PRD1".toLowerCase(), re.getRefIdInLower());
        assertEquals(1, re.getRefVer());
        assertEquals("TAW23015", re.getImporterRef());
        assertEquals("AE6L36KNR", re.getImpDecNum());
        assertEquals("1", re.getImpDecVer());
        assertEquals("B00072552", re.getBrokerRef());

        // head errs
        List<ImdReplyHeadErr> headErrs = re.getHeadErrs();
        assertEquals(1, headErrs.size());

        assertEquals("1", headErrs.get(0).getErrLoc());
        assertEquals(true, headErrs.get(0).getErrId().indexOf("ID0302") != -1);
        assertEquals(true, headErrs.get(0).getErrDesc().indexOf("COMMUNITY PROTECTION PERMIT IS APPLICABLE AND REQUIRED") != -1);


        List<Map<String, String>> headMoas = re.getHeadMoas();
        assertEquals(headMoas.size(), 7);
        assertEquals(true, re.isSuccess());

    }

    @Test
    public void test_Imd_SuccessRes_02() {
        String input = _read_input("imd_res_02");

        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        IMDResLoader loader = EdiMsgs.getIMDResLoader();
        IcsReplyImdRes re = loader.load(msg);
        System.out.println(Json.toJson(re, JsonFormat.full()));
        assertEquals(true, re.isSuccess());
    }

    @Test
    public void test_Imd_SuccessRes_03() {
        String input = _read_input("imd_res_03");

        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        IMDResLoader loader = EdiMsgs.getIMDResLoader();
        IcsReplyImdRes re = loader.load(msg);
        System.out.println(Json.toJson(re, JsonFormat.full()));
        assertEquals(true, re.isSuccess());
    }
}
