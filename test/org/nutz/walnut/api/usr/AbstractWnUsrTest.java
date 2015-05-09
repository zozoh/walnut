package org.nutz.walnut.api.usr;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.walnut.BaseUsrTest;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public abstract class AbstractWnUsrTest extends BaseUsrTest {

    @Test
    public void se_login_logout() {
        WnUsr xiaobai = usrs.create("xiaobai", "123456");
        WnSession se = ses.login("xiaobai", "123456");

        assertEquals(se.envs().getString("MY_ID"), xiaobai.id());
        assertEquals(se.envs().getString("HOME"), xiaobai.home());
        assertEquals(se.me(), xiaobai.name());

        // 检查对象
        WnObj oSe = io.check(null, "/session/" + se.id());
        assertEquals("application/json", oSe.mime());
        assertTrue(oSe.expireTime() > (System.currentTimeMillis() + 5000));

        // 获取
        se = ses.check(se.id());
        assertEquals(se.envs().getString("MY_ID"), xiaobai.id());
        assertEquals(se.envs().getString("HOME"), xiaobai.home());
        assertEquals(se.me(), xiaobai.name());

        // 设置环境变量
        ses.setEnv(se.id(), "x", "100");
        ses.setEnv(se.id(), "say", "hello");
        se = ses.check(se.id());
        assertEquals("100", se.envs().getString("x"));
        assertEquals("hello", se.envs().getString("say"));

        // 删除环境变量
        ses.removeEnvs(se.id(), "x", "say");
        se = ses.check(se.id());
        assertNull(se.envs().get("x"));
        assertNull(se.envs().get("say"));

        // 注销
        ses.logout(se.id());
        assertNull(ses.fetch(se.id()));
    }

    @Test
    public void usr_create_by_email() {
        WnUsr xiaobai = usrs.create("xiaobai@nutzam.com", "123456");

        // 获取一个
        WnUsr u = usrs.fetch("xiaobai@nutzam.com");
        assertEquals("123456", u.password());
        assertEquals(xiaobai.id(), u.id());
        assertEquals(xiaobai.id(), u.name());
        assertEquals("xiaobai@nutzam.com", u.email());
        assertNull(u.phone());

        WnObj oHome = io.check(null, u.home());
        assertEquals(u.id(), oHome.name());

        // 改个名
        usrs.setName("id:" + u.id(), "xiaobai");

        // 再次按照 Email 获取
        u = usrs.fetch("xiaobai@nutzam.com");
        assertEquals("123456", u.password());
        assertEquals(xiaobai.id(), u.id());
        assertEquals("xiaobai", u.name());
        assertEquals("xiaobai@nutzam.com", u.email());
        assertNull(u.phone());

        oHome = io.check(null, u.home());
        assertEquals(u.name(), oHome.name());

        // 按照 Name 获取
        u = usrs.fetch("xiaobai");
        assertEquals("123456", u.password());
        assertEquals(xiaobai.id(), u.id());
        assertEquals("xiaobai", u.name());
        assertEquals("xiaobai@nutzam.com", u.email());
        assertNull(u.phone());

        // 设置手机
        usrs.setPhone("xiaobai", "13910110054");

        // 按照 Name 获取
        u = usrs.fetch("xiaobai");
        assertEquals("123456", u.password());
        assertEquals(xiaobai.id(), u.id());
        assertEquals("xiaobai", u.name());
        assertEquals("13910110054", u.phone());
        assertEquals("xiaobai@nutzam.com", u.email());

        oHome = io.check(null, u.home());
        assertEquals(u.name(), oHome.name());

    }

    @Test
    public void usr_create_by_phone() {
        WnUsr xiaobai = usrs.create("13910110054", "123456");

        // 获取一个
        WnUsr u = usrs.fetch("13910110054");
        assertEquals("123456", u.password());
        assertEquals(xiaobai.id(), u.id());
        assertEquals(xiaobai.id(), u.name());
        assertEquals("13910110054", u.phone());
        assertNull(u.email());

        WnObj oHome = io.check(null, u.home());
        assertEquals(u.id(), oHome.name());

        // 改个名
        usrs.setName("id:" + u.id(), "xiaobai");

        // 再次按照 Phone 获取
        u = usrs.fetch("13910110054");
        assertEquals("123456", u.password());
        assertEquals(xiaobai.id(), u.id());
        assertEquals("xiaobai", u.name());
        assertEquals("13910110054", u.phone());
        assertNull(u.email());

        oHome = io.check(null, u.home());
        assertEquals(u.name(), oHome.name());

        // 按照 Name 获取
        u = usrs.fetch("xiaobai");
        assertEquals("123456", u.password());
        assertEquals(xiaobai.id(), u.id());
        assertEquals("13910110054", u.phone());
        assertEquals("xiaobai", u.name());
        assertNull(u.email());

        // 设置邮箱
        usrs.setEmail("xiaobai", "xiaobai@nutzam.com");

        // 按照 Name 获取
        u = usrs.fetch("xiaobai");
        assertEquals("123456", u.password());
        assertEquals(xiaobai.id(), u.id());
        assertEquals("xiaobai", u.name());
        assertEquals("13910110054", u.phone());
        assertEquals("xiaobai@nutzam.com", u.email());

        oHome = io.check(null, u.home());
        assertEquals(u.name(), oHome.name());

    }

    @Test
    public void usr_create_delete() {
        WnUsr xiaobai = usrs.create("xiaobai", "123456");

        // 获取一个
        WnUsr u = usrs.fetch("xiaobai");
        assertEquals("123456", u.password());
        assertEquals(xiaobai.id(), u.id());
        assertEquals("xiaobai", u.name());
        assertNull(u.phone());
        assertNull(u.email());

        // 检查 HOME
        WnObj oHome = io.check(null, u.home());

        // 检查权限设定
        WnObj oMe = io.check(oHome, ".people/" + u.id());
        assertEquals(Wn.ROLE.ADMIN, oMe.getInt("role"));

        // 删除就什么也没了
        usrs.delete(u);
        assertNull(usrs.fetch("xiaobai"));

        // 但是组目录还在
        io.check(null, u.home() + "/.people/" + u.id());

    }

}
