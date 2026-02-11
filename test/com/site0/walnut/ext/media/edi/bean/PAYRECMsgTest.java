package com.site0.walnut.ext.media.edi.bean;


import com.site0.walnut.ext.media.edi.loader.EdiMsgs;
import com.site0.walnut.ext.media.edi.loader.IMDResLoader;
import com.site0.walnut.ext.media.edi.loader.PAYRECLoader;
import com.site0.walnut.ext.media.edi.msg.reply.imd.IcsReplyImdRes;
import com.site0.walnut.ext.media.edi.msg.reply.pay.PayRecRes;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;

import static org.junit.Assert.assertEquals;

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
        


    }
}
