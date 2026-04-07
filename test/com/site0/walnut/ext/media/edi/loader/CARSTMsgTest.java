package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiInterchange;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.msg.reply.IcsReplyCARST;
import org.junit.Test;
import org.nutz.lang.Files;

import static org.junit.Assert.assertEquals;

public class CARSTMsgTest {

    private String _read_input(String name) {
        String txt = Files.read("com/site0/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }

    @Test
    public void test_carst_msg_01() {
        String input = _read_input("carst_msg_01");

        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        CARSTLoader loader = EdiMsgs.getCARSTLoader();
        IcsReplyCARST re = loader.load(msg);
        // System.out.println(Json.toJson(re, JsonFormat.full()));

        assertEquals(true, re.isSuccess());
        assertEquals("mmhojo3dttvyur1otl", re.getRefIdInLower());
        assertEquals("MMHOJO3DTTVYUR1OTL", re.getRefId());
        assertEquals(4, re.getRefVer());
    }
}
