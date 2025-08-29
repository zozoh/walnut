package com.site0.walnut.val.id;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.BaseIoTest;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.val.ValueMaker;
import com.site0.walnut.val.ValueMakers;

public class WnSeqDMakerTest extends BaseIoTest {

    protected WnObj oSeq;
    protected String seqPath;
    protected Date now;
    protected String yyyy_MM;
    protected String yyMMdd;
    protected NutMap context;

    @Override
    protected void on_before() {
        oSeq = this.io.createIfExists(null, "/test/", WnRace.DIR);
        seqPath = oSeq.getRegularPath();
        now = new Date();
        yyyy_MM = Wtime.formatUTC(now, "yyyy-MM");
        yyMMdd = Wtime.formatUTC(now, "yyMMdd");
        context = new NutMap();
    }

    @Override
    protected void on_after() {}

    protected ValueMaker maker(String fmt) {
        NutMap vars = new NutMap();
        String input = String.format(fmt, seqPath);
        return ValueMakers.build(io, vars, input);
    }

    @Test
    public void test_00() {
        ValueMaker vm = maker("seqD::4:%s@yyyy-MM#skey");
        Object v0 = vm.make(now, context);
        // 250829000001
        assertEquals(yyMMdd + "0001", v0);

        WnObj o = io.check(oSeq, yyyy_MM);
        assertEquals(yyyy_MM, o.name());
        
        // 修改序列数量 1+123 = 124
        io.inc(o.id(), "skey", 123, false);
        
        Object v1 = vm.make(now, context);
        // 250829000001
        assertEquals(yyMMdd + "0125", v1);
    }

}
