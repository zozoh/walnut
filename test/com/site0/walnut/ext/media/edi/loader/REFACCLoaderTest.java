package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiInterchange;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.msg.reply.pay.RefundAdv;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;

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

    }

}
