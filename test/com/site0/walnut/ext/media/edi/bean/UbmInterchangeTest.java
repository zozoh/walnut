package com.site0.walnut.ext.media.edi.bean;

import com.site0.walnut.ext.media.edi.loader.CLNTDUPLoader;
import com.site0.walnut.ext.media.edi.loader.EdiMsgs;
import org.nutz.lang.Files;

public class UbmInterchangeTest {
    private String _read_input(String name) {
        String txt = Files.read("com/site0/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }


    public void test_UBMErrLoader_01() {
        String input = _read_input("ubm_err_01");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        CLNTDUPLoader loader = EdiMsgs.getCLNTDUPLoader();
    }

}
