package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiInterchange;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.msg.reply.erm.IcsReplyERM;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ERMLoaderTest {

	private String _read_input(String name) {
		String txt = Files.read("com/site0/walnut/ext/media/edi/bean/pack/" + name + ".edi.txt");
		return txt.replaceAll("\r\n", "\n").trim();
	}

	@Test
	public void test_erm_msg_01() {
		String input = _read_input("erm_01");

		EdiInterchange ic = EdiInterchange.parse(input);
		EdiMessage msg = ic.getFirstMessage();

		assertEquals("ERM", EdiMsgs.getLoaderType(msg));

		ERMLoader loader = EdiMsgs.getERMLoader();
		IcsReplyERM re = loader.load(msg);

		System.out.println(Json.toJson(re, JsonFormat.full()));

		assertNotNull(re);
		assertFalse(re.isSuccess());
		assertEquals("ERM", re.getMsgType());
		assertEquals("ERM", re.getDocName());
		assertEquals(11, re.getFuncCode());
		assertEquals("mpqwwny2930i7e7jlv-0", re.getRefId());
		assertEquals("mpqwwny2930i7e7jlv-0", re.getRefIdInLower());
		assertEquals(1, re.getRefVer());
		assertEquals(IcsReplyERM.ERM_TYPE_ACR, re.getErmType());
		assertEquals(true, re.isErmError());
		assertEquals(true, re.isAcrType());
		assertEquals(false, re.isScrType());
		assertEquals("FKK363E", re.getIcsSiteId());
		assertEquals("20260530015542", re.getMsgRcvTime());
		assertEquals(1, re.getErrCount());
		assertEquals(1, re.getErrs().length);

		EdiReplyError err = re.getErrs()[0];
		assertEquals("CCFERROR", err.getType());
		assertEquals("22", err.getCode());
		assertEquals("The length of ORIGINALPORTOFLOADING in BODY is 4 characters which is less than the minimum field length of 5 characters",
					 err.getContent());
	}

	@Test
	public void test_erm_msg_02_scr() {
		String input = _read_input("erm_02");

		EdiInterchange ic = EdiInterchange.parse(input);
		EdiMessage msg = ic.getFirstMessage();
		ERMLoader loader = EdiMsgs.getERMLoader();
		IcsReplyERM re = loader.load(msg);

		System.out.println(Json.toJson(re, JsonFormat.full()));

		assertNotNull(re);
		assertFalse(re.isSuccess());
		assertEquals("ERM", re.getMsgType());
		assertEquals("sea-shipment-ref-001", re.getRefId());
		assertEquals("sea-shipment-ref-001", re.getRefIdInLower());
		assertEquals(2, re.getRefVer());
		assertEquals(IcsReplyERM.ERM_TYPE_SCR, re.getErmType());
		assertEquals(true, re.isErmError());
		assertEquals(false, re.isAcrType());
		assertEquals(true, re.isScrType());
		assertEquals(1, re.getErrCount());
		assertEquals("Sea cargo report validation failed", re.getErrs()[0].getContent());
	}
}




