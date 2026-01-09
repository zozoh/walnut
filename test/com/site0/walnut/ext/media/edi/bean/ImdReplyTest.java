package com.site0.walnut.ext.media.edi.bean;

import com.site0.walnut.ext.media.edi.loader.EdiMsgs;
import com.site0.walnut.ext.media.edi.loader.IMDResLoader;
import com.site0.walnut.ext.media.edi.msg.reply.imd.IcsReplyImdRes;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;

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

//        assertEquals(11, re.getFuncCode());
//        assertEquals(null, re.getDutyDate());
//
//        Map<String, String> statusMap = re.getImdStatus();
//        assertEquals("CLEAR", statusMap.get("CLEAR"));
//
//        // GIS 相关
//        assertEquals(false, re.isPreLodge());
//        assertEquals(true, re.isLlb());
//        assertEquals(true, re.isTlb());
//
//        // NAD 相关
//        assertEquals("FJM396H", re.getMsgRecipient());
//        assertEquals("AF46JR", re.getBranchId());
//        assertEquals("5650", re.getBrokerLicNum());
//        assertEquals("MASTER LOGISTICS PTY LTD", re.getBrokerName());
//        assertEquals(null, re.getBorkerBoxNum());
//        assertEquals("THETA WORKS PTY LTD", re.getImporterName());
//        assertEquals(null, re.getImporterBoxNum());
//
//        // RFF 相关
//        assertEquals("N10", re.getHeaderNatureType());


    }
}
