package com.site0.walnut.impl.box.cmd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.site0.walnut.api.box.WnServiceGetter;
import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.impl.box.JvmBoxInput;
import com.site0.walnut.impl.box.JvmBoxOutput;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.WnConfig;

/**
 * RM-NUTZ-11 命令侧冒烟：改造后的命令通过 {@link WnServiceGetter} 取服务可正常执行。
 */
public class WnServiceCommandSmokeTest {

    static class TestSys extends cmd_sys {

        void set(WnServiceGetter getter) {
            this.services = getter;
        }
    }

    static class TestHttpout extends cmd_httpout {

        void set(WnServiceGetter getter) {
            this.services = getter;
        }
    }

    private WnSystem sys(WnServiceHolder holder, ByteArrayOutputStream out) {
        WnSystem sys = new WnSystem(holder);
        sys.out = new JvmBoxOutput(out);
        sys.in = new JvmBoxInput(new ByteArrayInputStream(new byte[0]));
        return sys;
    }

    @Test
    public void sys_setup() throws Exception {
        WnServiceHolder holder = new WnServiceHolder();
        holder.setConfig(new WnConfig("test.properties"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TestSys cmd = new TestSys();
        cmd.set(new WnServiceGetter(holder));
        cmd.exec(sys(holder, out), new String[]{"-setup"});

        assertFalse(out.toString(StandardCharsets.UTF_8).isEmpty());
    }

    @Test
    public void sys_sysenv() throws Exception {
        WnServiceHolder holder = new WnServiceHolder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TestSys cmd = new TestSys();
        cmd.set(new WnServiceGetter(holder));
        cmd.exec(sys(holder, out), new String[]{"-sysenv"});

        assertFalse(out.toString(StandardCharsets.UTF_8).isEmpty());
    }

    @Test
    public void httpout_body() throws Exception {
        WnServiceHolder holder = new WnServiceHolder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TestHttpout cmd = new TestHttpout();
        cmd.set(new WnServiceGetter(holder));
        cmd.exec(sys(holder, out), new String[]{"hello"});

        assertTrue(out.toString(StandardCharsets.UTF_8).contains("hello"));
    }

}
