package com.site0.walnut.ext.util.xxtea;

import org.nutz.lang.Streams;
import org.nutz.repo.Base64;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_xxtea extends JvmExecutor {

	@Override
	public void exec(WnSystem sys, String[] args) throws Exception {
		ZParams params = ZParams.parse(args, "d");
		byte[] key = null;
		if (params.has("k")) {
			key = params.getString("k").getBytes();
		}
		else if (params.has("k64")) {
			key = Base64.decode(params.get("k64"));
		}
		else {
			sys.err.print("e.cmd.xxtea.need_key");
			return;
		}
		boolean isDecode = params.is("d");
		byte[] scr = Streams.readBytes(sys.in.getInputStream());
		byte[] dst = null;
		if (isDecode) {
			dst = XXTEA.decrypt(scr, key);
		}
		else {
			dst = XXTEA.encrypt(scr, key);
		}
		if (dst != null)
			sys.out.write(dst);
	}

	// 实现过程
	// 1. 上位机会上报 xxx?key=YYYYYY
	// 其中YYYY的长度为32字节的hex字符串, 如果长度不符, 返回400
	// 如果YYYY不是合法的字符串, 返回400或者500
    // 2. 使用 结合 btea和hexbin命令, 解出 dev16的值, 命令如下
	// echo -n YYYYYY | btea -k "密钥" | hexbin
	// 3. 根据dev16的值, 在thingset中查询对应的设备, 更新激活时间,并设置新的随机数
	// 4. 根据新的随机数, 生成新的dev16
	// 5. 根据新的随机数, 生成激活码, 下发给上位机

	
	// 小端读取int32
	static int[] readInt32(byte[] buff, int len) {
		int[] ret = new int[len];
		for (int i = 0; i < len; i++) {
			ret[i] = (buff[i*4+3] & 0xFF) + ((buff[i*4+2] & 0xFF) << 8) 
				   + ((buff[i*4+1] & 0xFF) << 16) + ((buff[i*4+0] & 0xFF) << 24);
		}
		return ret;
	}

}
