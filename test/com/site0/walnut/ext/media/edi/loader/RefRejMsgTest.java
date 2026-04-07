package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiInterchange;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.msg.reply.pay.RefundRej;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RefRejMsgTest {

    private String _read_input(String name) {
        String txt = Files.read("com/site0/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }

    @Test
    public void test_refundRej_01() {
        //refund_acc_01.edi.txt
        String input = _read_input("refund_rej_01");

        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        REFREJLoader loader = EdiMsgs.getREFREJLoader();
        RefundRej re = loader.load(msg);
        System.out.println(Json.toJson(re, JsonFormat.full()));

        assertNotNull(re);
        assertEquals(0, re.getRstVer());
        assertEquals(true, re.isSuccess());
        assertEquals("ISMEL4106", re.getClientRef());
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
}
