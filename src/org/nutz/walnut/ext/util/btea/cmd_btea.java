package org.nutz.walnut.ext.util.btea;

import org.nutz.lang.Streams;
import org.nutz.repo.Base64;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

import com.alibaba.druid.util.HexBin;

public class cmd_btea extends JvmExecutor {

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
		else if (params.has("khex")) {
			key = HexBin.decode(params.get("khex"));
		}
		else {
			sys.err.print("e.cmd.xxtea.need_key");
			return;
		}
		
		byte[] scr = Streams.readBytes(sys.in.getInputStream());
		int[] v = toInt32(scr);
		int[] k = toInt32(key);
		if (params.is("d"))
			btea(v, v.length, k);
		else
			btea(v, -v.length, k);
		byte[] buff = fromInt32(v);
		sys.out.write(buff);
	}

	public static int[] toInt32(byte[] buff) {
		int[] ret = new int[buff.length / 4];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (buff[i*4+3] & 0xFF) + ((buff[i*4+2] & 0xFF) << 8) 
				   + ((buff[i*4+1] & 0xFF) << 16) + ((buff[i*4+0] & 0xFF) << 24);
		}
		return ret;
	}
	
	public static byte[] fromInt32(int[] ret) {
		byte[] buff = new byte[ret.length * 4];
		for (int i = 0; i < ret.length; i++) {
			buff[i*4+0] = (byte) (ret[i] >>> 24 & 0xFF);
			buff[i*4+1] = (byte) (ret[i] >>> 16 & 0xFF);
			buff[i*4+2] = (byte) (ret[i] >>> 8 & 0xFF);
			buff[i*4+3] = (byte) (ret[i] >>> 0 & 0xFF);
		}
		return buff;
	}
	
	//---------------------------------------------------------------------
	public static int DELTA = 0x9e3779b9;
	
	public static void btea(int[] v, int n, int key[])
	{
	    int y, z, sum;
	    short p, rounds, e;
	    if (n > 1)            /* Coding Part */
	    {
	        rounds = (byte) (6 + 52/n);
	        sum = 0;
	        z = v[n-1];
	        do
	        {
	            sum += DELTA;
	            sum = sum & 0xFFFFFFFF;
	            e = (byte) ((sum >>> 2) & 3);
	            for (p=0; p<n-1; p++)
	            {
	                y = v[p+1];
//	                System.out.printf("Z=%08X Y=%08X\n", z & 0xFFFFFFFF ,y & 0xFFFFFFFF );
//	                System.out.printf("(%08X + %08X) ^ (%08X + %08X)\n", (z>>>5)^(y<<2), ((y>>>3)^(z<<4)), ((sum^y)), (key[(p&3)^e] ^ z));
	                z = v[p] += ((((z>>>5)^(y<<2)) + ((y>>>3)^(z<<4))) ^ ((sum^y) + (key[(p&3)^e] ^ z)));
//	                System.out.printf("z -- %08X\n", z & 0xFFFFFFFF);
	            }
	            y = v[0];
	            z = v[n-1] += ((((z>>>5)^(y<<2)) + ((y>>>3)^(z<<4))) ^ ((sum^y) + (key[(p&3)^e] ^ z)));
//	            System.out.printf("%08X %08X %08X %02X %02X %02X\n", y & 0xFFFFFFFF, z& 0xFFFFFFFF, sum& 0xFFFFFFFF, 
//	            		p & 0xFF, rounds & 0xFF, e & 0xFF);
	        }
	        while (--rounds != 0);
	    }
	    else if (n < -1)      /* Decoding Part */
	    {
	        n = -n;
	        rounds = (byte) (6 + 52/n);
	        sum = rounds*DELTA;
	        y = v[0];
	        do
	        {
	            e = (byte) ((sum >>> 2) & 3);
	            for (p=(byte) (n-1); p>0; p--)
	            {
	                z = v[p-1];
	                y = v[p] -= (((z>>>5^y<<2) + (y>>>3^z<<4)) ^ ((sum^y) + (key[(p&3)^e] ^ z)));
	            }
	            z = v[n-1];
	            y = v[0] -= (((z>>>5^y<<2) + (y>>>3^z<<4)) ^ ((sum^y) + (key[(p&3)^e] ^ z)));
	            sum -= DELTA;
	        }
	        while (--rounds != 0);
	    }
	}
}
