package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiInterchange;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.msg.reply.clreg.IcsReplyCLNTDUP;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;

import static org.junit.Assert.*;

public class CLNTDUPMsgTest {

    private String _read_input(String name) {
        String txt = Files.read("com/site0/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }

    @Test
    public void test_dup() {
        String input = _read_input("re_dup");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        CLNTDUPLoader loader = EdiMsgs.getCLNTDUPLoader();
        IcsReplyCLNTDUP re = loader.load(msg);
        System.out.println(Json.toJson(re, JsonFormat.full()));

        assertTrue(re.isSuccess());
        assertEquals("CCI", re.getType());
        assertEquals("AAA3436797Y", re.getCode());
        assertEquals("28558C74B757460991741B177754D009", re.getRefId());
        assertEquals("28558c74b757460991741b177754d009", re.getRefIdInLower());
        assertEquals("CLNTDUP", re.getMsgType());

        // LOC+ZZZ+:::CALLIVER TRADING LIMITED'
        assertEquals("CALLIVER TRADING LIMITED", re.getName());

        // FTX+BA+++ROOM1002,HUAYI BUILDING, NO.9 PINGJI
        // ROA:NANWAN STREET,LONGGANG DISTRICT:SHENZHEN:518000:GD+CN'
        assertEquals("BA", re.getAddrType());
        assertNotNull(re.getAddr());
        assertTrue(re.getAddr().startsWith("ROOM1002"));

        // TAX+RN+:::IMPORTER' / TAX+RN+:::SUPPLIER'
        assertEquals("IMPORTER,SUPPLIER", re.getRoleNames());
    }

    @Test
    public void test_dup_ind() {
        String input = _read_input("re_dup_02");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        CLNTDUPLoader loader = EdiMsgs.getCLNTDUPLoader();
        IcsReplyCLNTDUP re = loader.load(msg);
        System.out.println(Json.toJson(re, JsonFormat.full()));

        assertTrue(re.isSuccess());
        assertEquals("CCI", re.getType());
        assertEquals("AAA3436797Y", re.getCode());

        // FTX+AFM+++MR:JOHN:MICHAEL:SMITH:JR'
        // FIRST NAME + SECOND NAME + FAMILY NAME = JOHN MICHAEL SMITH
        assertEquals("JOHN MICHAEL SMITH", re.getName());
    }
}
