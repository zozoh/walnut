package org.nutz.walnut.ext.media.edi.bean;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.media.edi.bean.segment.ICS_UNB;
import org.nutz.walnut.ext.media.edi.bean.segment.ICS_UNH;
import org.nutz.walnut.ext.media.edi.bean.segment.ICS_UNT;
import org.nutz.walnut.ext.media.edi.bean.segment.ICS_UNZ;
import org.nutz.walnut.ext.media.edi.bean.segment.ICS_FTX;
import org.nutz.walnut.ext.media.edi.bean.segment.ICS_UCI;
import org.nutz.walnut.ext.media.edi.loader.CLNTDUPLoader;
import org.nutz.walnut.ext.media.edi.loader.CLREGRLoader;
import org.nutz.walnut.ext.media.edi.loader.EdiMsgs;
import org.nutz.walnut.ext.media.edi.loader.CONTRLLoader;
import org.nutz.walnut.ext.media.edi.reply.EdiReplyCLNTDUP;
import org.nutz.walnut.ext.media.edi.reply.EdiReplyCLREGR;
import org.nutz.walnut.ext.media.edi.reply.EdiReplyError;
import org.nutz.walnut.ext.media.edi.reply.EdiReplyCONTRL;
import org.nutz.walnut.ext.media.edi.util.EdiSegmentFinder;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.tmpl.WnTmplX;

public class EdiInterchangeTest {

    private String _read_input(String name) {
        String txt = Files.read("org/nutz/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
        return txt.replaceAll("\r\n", "\n").trim();
    }

    @Test
    public void test_parse_dup() {
        String input = _read_input("re_dup");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        CLNTDUPLoader loader = EdiMsgs.getCLNTDUPLoader();
        EdiReplyCLNTDUP re = loader.load(msg);

        assertEquals("CCI", re.getType());
        assertEquals("AAA3436797Y", re.getCode());
        assertEquals("28558C74B757460991741B177754D009", re.getReferId());
        assertEquals("28558c74b757460991741b177754d009", re.getReferIdInLower());
    }

    @Test
    public void test_parse_ok() {
        String input = _read_input("re_ok");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        CLREGRLoader loader = EdiMsgs.getCLREGRLoader();
        EdiReplyCLREGR re = loader.load(msg);
        assertFalse(re.isFailed());
        assertTrue(re.isSuccess());
        EdiReplyError[] errs = re.getErrors();
        assertNull(errs);

        assertEquals("ABN", re.getType());
        assertEquals("14165610382", re.getCode());
        assertEquals("7N170STPSUIBEP6PH1D4E6ESHF", re.getReferId());
        assertEquals("7n170stpsuibep6ph1d4e6eshf", re.getReferIdInLower());
    }

    @Test
    public void test_parse_error() {
        String input = _read_input("re_error");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        CLREGRLoader loader = EdiMsgs.getCLREGRLoader();
        EdiReplyCLREGR re = loader.load(msg);
        assertTrue(re.isFailed());
        assertFalse(re.isSuccess());
        EdiReplyError[] errs = re.getErrors();
        assertEquals(12, errs.length);

        List<EdiReplyError> errList = Wlang.list(errs);
        Iterator<EdiReplyError> it = errList.iterator();
        EdiReplyError err;

        err = it.next();
        assertEquals("ADVICE:MS5201:THIS TRANSACTION WAS REJECTED", err.toString());
        err = it.next();
        assertEquals("ERROR:CL0404:ADDRESS TYPE SUPPLIED ALONG WITH ABN BUT NO CAC SUPPLIED",
                     err.toString());
        err = it.next();
        assertEquals("ERROR:CL0405:BUSINESS ADDRESS LINE 1 NOT ALLOWED WITH ABN", err.toString());
        err = it.next();
        assertEquals("ERROR:CL0420:BUSINESS ADDRESS LOCALITY SUPPLIED WITHOUT A VALID CLIENT TYPE",
                     err.toString());
        err = it.next();
        assertEquals("ERROR:CL0422:ADDRESS LOCALITY NOT ALLOWED WITH ABN", err.toString());
        err = it.next();
        assertEquals("ERROR:CL0425:BUSINESS ADDRESS POST CODE SUPPLIED WITHOUT A VALID CLIENT TYPE",
                     err.toString());
        err = it.next();
        assertEquals("ERROR:CL0427:ADDRESS POST CODE NOT ALLOWED WITH ABN", err.toString());
        err = it.next();
        assertEquals("ERROR:CL0436:BUSINESS ADDRESS STATE CODE SUPPLIED WITHOUT A VALID CLIENT TYPE",
                     err.toString());
        err = it.next();
        assertEquals("ERROR:CL0438:BUSINESS ADDRESS COUNTRY CODE SUPPLIED WITHOUT A VALID CLIENT TYPE",
                     err.toString());
        err = it.next();
        assertEquals("ERROR:CL0440:ADDRESS STATE CODE NOT ALLOWED WITH ABN", err.toString());
        err = it.next();
        assertEquals("ERROR:CL0441:ADDRESS COUNTRY CODE NOT ALLOWED WITH ABN", err.toString());
        err = it.next();
        assertEquals("ERROR:CL0467:PREFIX CODE ONLY ALLOWED WITH AP, FA OR BP CONTACT ADDRESS",
                     err.toString());

        assertFalse(it.hasNext());
    }

    @Test
    public void test_UNH() {
        String input = _read_input("c03");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        ICS_UNH mh = msg.getHeader();
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
        ICS_UNB h = ic.getHeader();
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
        CONTRLLoader loader = EdiMsgs.getInterchangeLoader();

        String input = _read_input("c_error");
        EdiInterchange ic = EdiInterchange.parse(input);

        EdiMessage msg = ic.getFirstMessage();

        EdiReplyCONTRL ric = loader.load(msg);
        ICS_UCI uci = ric.getUci();
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
        ric = loader.load(msg);
        uci = ric.getUci();
        assertEquals("23062700000009", uci.getRefNumber());
        assertEquals("AAR399A", uci.getCreator());
        assertEquals("AAR399A", uci.getOwner());
        assertEquals("AAA336C", uci.getRecipient());
        assertEquals("7", uci.getActionCode());
        assertFalse(uci.isRejected());
        assertTrue(uci.isNotExplicitlyRejected());
    }

    @Test
    public void test_FTX() {
        String input = _read_input("re_ok");
        EdiInterchange ic = EdiInterchange.parse(input);
        EdiMessage msg = ic.getFirstMessage();
        EdiSegment seg = msg.findSegment("FTX", "AAO");
        ICS_FTX t = new ICS_FTX(seg);
        assertEquals("AAO", t.getSubjectCode());
        assertTrue(t.isSubject("AAO"));
        assertNull(t.getFuncCoded());
        assertNull(t.getReference());
        assertEquals("THIS TRANSACTION WAS ACCEPTED WITH ERRORS AND/OR WARNINGS", t.getLiteral());
        assertNull(t.getLanguage());
        assertNull(t.getFormatting());

        // 测试 finder
        EdiSegmentFinder finder = msg.getFinder();
        List<EdiSegment> segs = finder.nextAll("FTX", "AAO");
        assertEquals(2, segs.size());
        // ------------------
        seg = segs.get(0);
        t = new ICS_FTX(seg);
        assertEquals("AAO", t.getSubjectCode());
        assertTrue(t.isSubject("AAO"));
        assertNull(t.getFuncCoded());
        assertNull(t.getReference());
        assertEquals("THIS TRANSACTION WAS ACCEPTED WITH ERRORS AND/OR WARNINGS", t.getLiteral());
        assertNull(t.getLanguage());
        assertNull(t.getFormatting());
        // ------------------
        seg = segs.get(1);
        t = new ICS_FTX(seg);
        assertEquals("AAO", t.getSubjectCode());
        assertTrue(t.isSubject("AAO"));
        assertNull(t.getFuncCoded());
        assertNull(t.getReference());
        assertEquals("ABN =14165610382 REGISTERED SUCCESSFULLY", t.getLiteral());
        assertNull(t.getLanguage());
        assertNull(t.getFormatting());
    }

    @Test
    public void test_00_fillBean() {
        String input = _read_input("c00");
        EdiInterchange ic = EdiInterchange.parse(input);
        ic.packMessages();

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
        ic.packMessages();
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
        ic.packMessages();
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
        ic.packMessages();
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
        ic.packMessages();
        String s3 = ic.toString().trim();
        assertEquals(in2, s3);
    }

    @Test
    public void test_04() {
        String input = _read_input("c04");
        EdiInterchange ic = EdiInterchange.parse(input);
        //
        // 测试头部
        //
        ICS_UNB h = ic.getHeader();
        assertEquals("UNOC", h.getSyntaxId());
        assertEquals("3", h.getSyntaxVersion());
        assertEquals("AAA336C", h.getCreator());
        assertEquals(null, h.getCreatorIdCode());
        assertEquals("AAA336C", h.getOwner());
        assertEquals("AAR399A", h.getRecipient());
        assertEquals(null, h.getRecipientIdCode());
        assertEquals(null, h.getRecipientRoutingAddress());
        assertEquals("230627", h.getTransDate());
        assertEquals("1357", h.getTransTime());
        assertEquals("00000000000052", h.getControlRefNumber());
        assertEquals(null, h.getRecipientRefPassword());
        assertEquals(null, h.getRecipientRefPasswordQualifier());
        assertEquals(null, h.getApplicationReference());
        assertEquals(null, h.getProcessingPriorityCode());
        assertEquals(null, h.getRequested());
        assertEquals("1", h.getTest());
        //
        // 测试尾部
        //
        ICS_UNZ t = ic.getTail();
        assertEquals(1, t.getMessageCount());
        assertEquals("00000000000052", t.getControlRefNumber());

        //
        // 测试消息
        //
        EdiMessage msg = ic.getFirstMessage();
        ICS_UNH mh = msg.getHeader();
        assertEquals("000001", mh.getRefNumber());
        assertEquals("CONTRL", mh.getTypeId());
        assertEquals("D", mh.getTypeVersion());
        assertEquals("3", mh.getTypeReleaseNumber());
        assertEquals("UN", mh.getControlingAgency());

        ICS_UNT mt = msg.getTail();
        assertEquals(7, mt.getSegmentCount());
        assertEquals("000001", mt.getRefNumber());

        EdiSegment uci_sg = msg.findSegment("UCI");
        ICS_UCI uci = new ICS_UCI(uci_sg);
        assertEquals("23062700000008", uci.getRefNumber());
        assertEquals("AAR399A", uci.getCreator());
        assertEquals("AAR399A", uci.getOwner());
        assertEquals("AAA336C", uci.getRecipient());
        assertEquals("4", uci.getActionCode());
        assertTrue(uci.isRejected());
        assertFalse(uci.isNotExplicitlyRejected());
    }

}
