package com.site0.walnut.impl.box;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.stream.VoidInputStream;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.box.WnBoxStatus;
import com.site0.walnut.api.box.WnServiceFactory;
import com.site0.walnut.util.WnRun;

public class Jvms {

    public static WnSystem createWnSystem(WnRun runner,
                                          JvmExecutorFactory jef,
                                          WnBoxContext bc,
                                          StringBuilder sbOut,
                                          StringBuilder sbErr,
                                          CharSequence strIn) {
        OutputStream out = Lang.ops(sbOut);
        OutputStream err = Lang.ops(sbErr);
        InputStream in = Strings.isBlank(strIn) ? null : Lang.ins(strIn);

        return createWnSystem(runner, jef, bc, out, err, in);
    }

    public static WnSystem createWnSystem(WnRun runner,
                                          JvmExecutorFactory jef,
                                          WnBoxContext bc,
                                          OutputStream out,
                                          OutputStream err,
                                          InputStream in) {
        WnServiceFactory service = runner.getServiceFactory();
        WnSystem sys = new WnSystem(service);

        OutputStream s_out = EscapeCloseOutputStream.WRAP(out);
        OutputStream s_err = EscapeCloseOutputStream.WRAP(err);
        InputStream s_in = EscapeCloseInputStream.WRAP(in);

        sys.boxId = null;
        sys.pipeId = 0;
        sys.nextId = -1;
        sys.cmdOriginal = null;
        sys.session = bc.session;
        sys.io = bc.io;
        sys.auth = bc.auth;
        sys.jef = jef;
        sys.in = new JvmBoxInput(null == s_in ? new VoidInputStream() : s_in);
        sys.out = new JvmBoxOutput(s_out);
        sys.err = new JvmBoxOutput(s_err);

        sys._runner = new JvmAtomRunner(runner.boxes());
        sys._runner.boxId = null;
        sys._runner.status = WnBoxStatus.FREE;
        sys._runner.jef = jef;
        sys._runner.bc = bc;
        sys._runner.out = s_out;
        sys._runner.err = s_err;
        sys._runner.in = s_in;

        return sys;
    }

}
