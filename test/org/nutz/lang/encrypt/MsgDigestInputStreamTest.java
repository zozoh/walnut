package org.nutz.lang.encrypt;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;

public class MsgDigestInputStreamTest {

	@Test
	public void testGetDigest() throws IOException{
		String str = "wendal";
		ByteArrayInputStream bin = new ByteArrayInputStream(str.getBytes());
		MsgDigestInputStream in = new MsgDigestInputStream(bin, "md5");
		in.read(new byte[1024]);
		Streams.safeClose(in);
		assertEquals(Wlang.md5(str), in.digest());
	}

}
