package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiInterchange;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.msg.reply.sam.IcsReplySamRes;
import com.site0.walnut.ext.media.edi.msg.reply.sam.SamAdv;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class SAMResTest {

    private String _read_input(String name) {
        String txt = Files.read("com/site0/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }


    @Test
    public void test_sam_msg_01() {
        String input = _read_input("sam_res_01");

        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        SAMResLoader loader = EdiMsgs.getSAMResLoader();
        IcsReplySamRes re = loader.load(msg);

        System.out.println(Json.toJson(re, JsonFormat.full()));
        assertEquals(true, re.isSuccess());
        assertEquals("B00058602/1/PRD1", re.getRefId());
        assertEquals("b00058602/1/prd1", re.getRefIdInLower());
        assertEquals(1, re.getRefVer());
        assertEquals("SAM", re.getDocName());

        assertEquals("CLEAR", re.getImdStatus().get("CLEAR"));

        assertEquals("FJM396H", re.getMsgRecipient());
        assertEquals("AF46JR", re.getBranchId());

        // brokerLicNum
        assertEquals("5650", re.getBrokerLicNum());
        assertEquals("MASTER LOGISTICS PTY LTD", re.getBrokerName());
        assertEquals("MR GUOHUI XU", re.getImporterName());

        assertEquals("AE3AMJPL7", re.getImpDecNum());
        assertEquals("1", re.getImpDecVer());
        assertEquals("SHL23038-6", re.getImporterRef());
        assertEquals("B00058602", re.getBrokerRef());

        ArrayList<SamAdv> advs = re.getAdvs();
        assertEquals(7, advs.size());

        assertEquals("1", advs.get(0).getAdvId());
        assertEquals("1,7,10", String.join(",", advs.get(0).getLocList()));

        assertEquals("177", advs.get(2).getAdvId());
        assertEquals("1,10", String.join(",", advs.get(2).getLocList()));

        assertEquals("402", advs.get(5).getAdvId());
        assertEquals("1,2,3,4,5,6,7,8,9,10,11,12,13,14,16,17,18,19,20", String.join(",", advs.get(5).getLocList()));

        assertEquals("515", advs.get(6).getAdvId());
        assertEquals("2", String.join(",", advs.get(6).getLocList()));

    }
}
