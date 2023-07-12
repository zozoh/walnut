package org.nutz.walnut.ext.media.edi.bean;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.media.edi.bean.segment.SG_UNB;
import org.nutz.walnut.ext.media.edi.bean.segment.SG_UNH;
import org.nutz.walnut.ext.media.edi.bean.segment.SG_UCI;
import org.nutz.walnut.ext.media.edi.loader.EdiLoaders;
import org.nutz.walnut.ext.media.edi.loader.ICLoader;
import org.nutz.walnut.ext.media.edi.reply.EdiReplyIC;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.tmpl.WnTmplX;

public class EdiInterchangeTest {

    private String _read_input(String name) {
        String txt = Files.read("org/nutz/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }

    @Test
    public void test_UNH() {
        String input = _read_input("c03");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        SG_UNH mh = msg.getHeader();
        assertEquals("000001", mh.getRefNumber());
        assertEquals("CUSRES", mh.getTypeId());
        assertEquals("D", mh.getTypeVersion());
        assertEquals("99B", mh.getTypeReleaseNumber());
        assertEquals("UN", mh.getControlingAgency());
    }

    @Test
    public void test_UNB() {
        String input = _read_input("c03");
        EdiInterchange ic = EdiInterchange.parse(input);
        SG_UNB h = ic.getHeader();
        assertEquals("UNOC", h.getSyntaxId());
        assertEquals("3", h.getSyntaxVersion());
        assertEquals("AAA336C", h.getCreator());
        assertEquals(null, h.getCreatorIdCode());
        assertEquals("AAA336C", h.getOwner());
        assertEquals("AAR399A", h.getRecipient());
        assertEquals(null, h.getRecipientIdCode());
        assertEquals(null, h.getRecipientRoutingAddress());
        assertEquals("230627", h.getTransDate());
        assertEquals("0139", h.getTransTime());
        assertEquals("00000000000047", h.getControlRefNumber());
        assertEquals(null, h.getRecipientRefPassword());
        assertEquals(null, h.getRecipientRefPasswordQualifier());
        assertEquals(null, h.getApplicationReference());
        assertEquals("1", h.getProcessingPriorityCode());
        assertEquals(null, h.getRequested());
        assertEquals("1", h.getTest());
    }

    @Test
    public void test_UCI() {
        ICLoader loader = EdiLoaders.getInterchangeLoader();
        
        String input = _read_input("c_error");
        EdiInterchange ic = EdiInterchange.parse(input);

        EdiMessage msg = ic.getFirstMessage();
        
        EdiReplyIC ric = loader.trans(msg);
        SG_UCI uci = ric.getUCI();
        assertEquals("23062600000024", uci.getRefNumber());
        assertEquals("AAR399A", uci.getCreator());
        assertEquals("AAR399A", uci.getOwner());
        assertEquals("AAA336C", uci.getRecipient());
        assertEquals("4", uci.getActionCode());
        assertTrue(uci.isRejected());
        assertFalse(uci.isNotExplicitlyRejected());

        //
        // 测试成功
        //
        input = _read_input("c_ok");
        ic = EdiInterchange.parse(input);

        msg = ic.getFirstMessage();
        ric = loader.trans(msg);
        uci = ric.getUCI();
        assertEquals("23062700000009", uci.getRefNumber());
        assertEquals("AAR399A", uci.getCreator());
        assertEquals("AAR399A", uci.getOwner());
        assertEquals("AAA336C", uci.getRecipient());
        assertEquals("7", uci.getActionCode());
        assertFalse(uci.isRejected());
        assertTrue(uci.isNotExplicitlyRejected());
    }

    @Test
    public void test_00_fillBean() {
        String input = _read_input("c00");
        EdiInterchange ic = EdiInterchange.parse(input);
        ic.packEntry();

        NutMap bean = new NutMap();
        EdiSegment head = ic.getHeadSegment();
        head.fillBean(bean, "type", "part: p1,p2", null, null, null, "n1", null, "n2");
        assertEquals("UNB", bean.getString("type"));
        NutMap part = bean.getAs("part", NutMap.class);
        assertEquals("A", part.getString("p1"));
        assertEquals("B", part.getString("p2"));
        assertEquals(1, bean.getInt("n1"));
        assertEquals(1, bean.getInt("n2"));

        EdiMessage en = ic.getFirstMessage();
        bean = new NutMap();
        head = en.getHeadSegment();
        head.fillBean(bean, "type", "nb");
        assertEquals("UNH", bean.getString("type"));
        assertEquals(1, bean.getInt("nb"));

        EdiSegment rff = en.findSegment("RFF");
        bean = new NutMap();
        rff.fillBean(bean, "type", "info: type,abn");
        assertEquals("RFF", bean.getString("type"));
        NutMap info = bean.getAs("info", NutMap.class);
        assertEquals("ABN", info.getString("type"));
        assertEquals("15654834214", info.getString("abn"));
    }

    @Test
    public void test_00() {
        String input = _read_input("c00");
        EdiInterchange ic = EdiInterchange.parse(input);
        String s2 = ic.toString().trim();
        assertEquals(input, s2);

        String in2 = WnTmplX.exec(input, Wlang.map("N", 3));
        ic.packEntry();
        String s3 = ic.toString().trim();
        assertEquals(in2, s3);
    }

    @Test
    public void test_01() {
        String input = _read_input("c01");
        EdiInterchange ic = EdiInterchange.parse(input);
        String s2 = ic.toString().trim();
        assertEquals(input, s2);

        String in2 = WnTmplX.exec(input, Wlang.map("N", 10));
        ic.packEntry();
        String s3 = ic.toString().trim();
        assertEquals(in2, s3);
    }

    @Test
    public void test_02() {
        String input = _read_input("c02");
        EdiInterchange ic = EdiInterchange.parse(input);
        String s2 = ic.toString().trim();
        assertEquals(input, s2);

        String in2 = WnTmplX.exec(input, Wlang.map("N", 4));
        ic.packEntry();
        String s3 = ic.toString().trim();
        assertEquals(in2, s3);
    }

    @Test
    public void test_03() {
        String input = _read_input("c03");
        EdiInterchange ic = EdiInterchange.parse(input);
        String s2 = ic.toString().trim();
        assertEquals(input, s2);

        String in2 = WnTmplX.exec(input, Wlang.map("N", 57));
        ic.packEntry();
        String s3 = ic.toString().trim();
        assertEquals(in2, s3);
    }

}
