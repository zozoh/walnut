package org.nutz.lang.encrypt;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;

public class MsgDigestOutputStreamTest {

	@Test
	public void test_dis() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		MsgDigestOutputStream out = new MsgDigestOutputStream(bout, "md5");
		out.write("abc".getBytes());
		Streams.safeClose(out);
		assertEquals(Wlang.md5("abc"), out.digest());
	}
}
