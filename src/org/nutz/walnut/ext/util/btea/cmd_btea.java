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
        } else if (params.has("k64")) {
            key = Base64.decode(params.get("k64"));
        } else if (params.has("khex")) {
            key = HexBin.decode(params.get("khex"));
        } else {
            sys.err.print("e.cmd.xxtea.need_key");
            return;
        }

        byte[] scr = Streams.readBytes(sys.in.getInputStream());
        int[] v = BTea.toInt32(scr);
        int[] k = BTea.toInt32(key);
        if (params.is("d"))
            BTea.btea(v, -v.length, k);
        else
            BTea.btea(v, v.length, k);
        byte[] buff = BTea.fromInt32(v);
        sys.out.write(buff);
    }

}
