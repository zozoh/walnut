package org.nutz.walnut.impl.usr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.BaseUsrTest;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class IoWnUsrTest extends BaseUsrTest {

    @Test
    public void test_forbidden_write() {
        // 创建两个用户
        final WnUsr ua = usrs.create("userA", "123456");
        final WnUsr ub = usrs.create("userB", "123456");

        // 设置权限监控
        WnContext wc = Wn.WC();

        wc.setSecurity(security);
        try {
            // A 用户建立一个文件，改变权限
            final String path = ua.home() + "/aaa.txt";
            wc.su(ua, new Atom() {
                public void run() {
                    WnObj o = io.create(null, path, WnRace.FILE);
                    io.writeText(o, "hello");
                }
            });

            // B 用户不能写
            try {
                final WnObj o = io.check(null, path);
                wc.su(ub, new Atom() {
                    public void run() {
                        io.writeText(o, "I am B");
                    }
                });
                fail();
            }
            catch (Exception e) {
                assertEquals("e.io.forbidden : /home/userA/aaa.txt", e.toString());
            }

            // 将文件改成变成同组能写
            wc.su(ua, new Atom() {
                public void run() {
                    WnObj o = io.check(null, path);
                    io.appendMeta(o, "md:0770");
                }
            });

            // 把 B 用户加入到组里就能写
            usrs.setRoleInGroup(ub, ua.group(), Wn.ROLE.MEMBER);
            String str = wc.su(ub, new Proton<String>() {
                protected String exec() {
                    WnObj o = io.check(null, path);
                    io.writeText(o, "I am B");
                    return io.readText(o);
                }
            });
            assertEquals("I am B", str);

            // 将文件改成变成管理员只写
            wc.su(ua, new Atom() {
                public void run() {
                    WnObj o = io.check(null, path);
                    io.appendMeta(o, "md:0700");
                }
            });

            // B 又不能写了
            try {
                final WnObj o = io.check(null, path);
                str = wc.su(ub, new Proton<String>() {
                    protected String exec() {
                        io.writeText(o, "I am B2");
                        return io.readText(o);
                    }
                });
                fail();
            }
            catch (Exception e) {
                assertEquals("e.io.forbidden : /home/userA/aaa.txt", e.toString());
            }

            // 只有变成管理员
            wc.su(root, new Atom() {
                public void run() {
                    usrs.setRoleInGroup(ub, ua.group(), Wn.ROLE.ADMIN);
                }
            });

            // 才能写
            str = wc.su(ub, new Proton<String>() {
                protected String exec() {
                    WnObj o = io.check(null, path);
                    io.writeText(o, "I am B2");
                    return io.readText(o);
                }
            });
            assertEquals("I am B2", str);
        }
        finally {
            wc.setSecurity(null);
        }
    }

    @Test
    public void test_forbidden_read() {
        // 创建两个用户
        final WnUsr ua = usrs.create("userA", "123456");
        final WnUsr ub = usrs.create("userB", "123456");

        // 设置权限监控
        WnContext wc = Wn.WC();

        wc.setSecurity(security);
        try {
            // A 用户建立一个文件，改变权限
            final String path = ua.home() + "/aaa.txt";
            wc.su(ua, new Atom() {
                public void run() {
                    WnObj o = io.create(null, path, WnRace.FILE);
                    io.writeText(o, "hello");
                }
            });

            // A 用户能读
            String str = wc.su(ua, new Proton<String>() {
                protected String exec() {
                    WnObj o = io.check(null, path);
                    return io.readText(o);
                }
            });
            assertEquals("hello", str);

            // B 用户不能读
            try {
                str = wc.su(ub, new Proton<String>() {
                    protected String exec() {
                        WnObj o = io.check(null, path);
                        return io.readText(o);
                    }
                });
                fail();
            }
            catch (Exception e) {
                assertEquals("e.io.forbidden : /home/userA", e.toString());
            }

            // 把 B 用户加入到组里就能读
            usrs.setRoleInGroup(ub, ua.group(), Wn.ROLE.MEMBER);
            str = wc.su(ub, new Proton<String>() {
                protected String exec() {
                    WnObj o = io.check(null, path);
                    return io.readText(o);
                }
            });
            assertEquals("hello", str);

            // 将文件改成变成管理员只读
            wc.su(ua, new Atom() {
                public void run() {
                    WnObj o = io.check(null, path);
                    io.appendMeta(o, "md:0700");
                }
            });

            // B 又不能读了
            try {
                final WnObj o = io.check(null, path);
                str = wc.su(ub, new Proton<String>() {
                    protected String exec() {
                        return io.readText(o);
                    }
                });
                fail();
            }
            catch (Exception e) {
                assertEquals("e.io.forbidden : /home/userA/aaa.txt", e.toString());
            }

            // 只有变成管理员
            wc.su(root, new Atom() {
                public void run() {
                    usrs.setRoleInGroup(ub, ua.group(), Wn.ROLE.ADMIN);
                }
            });

            // 才能读
            str = wc.su(ub, new Proton<String>() {
                protected String exec() {
                    WnObj o = io.check(null, path);
                    return io.readText(o);
                }
            });
            assertEquals("hello", str);
        }
        finally {
            wc.setSecurity(null);
        }
    }

    @Test
    public void se_login_logout() throws Throwable {
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
        // TODO zozoh 自从改了自制的 nanoTime 以后，就老过不去
        // 看起来是因为在很短的时间内，重复写文件，导致历史记录产生了问题？
        try {
            ses.setEnv(se.id(), "say", "hello");
            ses.setEnv(se.id(), "x", "100");

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
        catch (Throwable e) {
            System.out.println(Json.toJson(se));
            System.out.println("--------------------check again:");
            se = ses.check(se.id());
            System.out.println(Json.toJson(se));
            System.out.println("--------------------Obj:");
            WnObj oSe2 = io.check(null, "/session/" + se.id());
            System.out.println(Json.toJson(oSe2));
            throw e;
        }
    }

    @Test
    public void usr_create_by_email() {
        WnUsr xiaobai = usrs.create("xiaobai@nutzam.com", "123456");

        // 获取一个
        WnUsr u = usrs.fetch("xiaobai@nutzam.com");
        //assertEquals("123456", u.password());
        assertTrue(usrs.checkPassword("xiaobai", "123456"));
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
        //assertEquals("123456", u.password());
        assertTrue(usrs.checkPassword("xiaobai", "123456"));
        assertEquals(xiaobai.id(), u.id());
        assertEquals("xiaobai", u.name());
        assertEquals("xiaobai@nutzam.com", u.email());
        assertNull(u.phone());

        oHome = io.check(null, u.home());
        assertEquals(u.name(), oHome.name());

        // 按照 Name 获取
        u = usrs.fetch("xiaobai");
        //assertEquals("123456", u.password());
        assertTrue(usrs.checkPassword("xiaobai", "123456"));
        assertEquals(xiaobai.id(), u.id());
        assertEquals("xiaobai", u.name());
        assertEquals("xiaobai@nutzam.com", u.email());
        assertNull(u.phone());

        // 设置手机
        usrs.setPhone("xiaobai", "13910110054");

        // 按照 Name 获取
        u = usrs.fetch("xiaobai");
        //assertEquals("123456", u.password());
        assertTrue(usrs.checkPassword("xiaobai", "123456"));
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
        //assertEquals("123456", u.password());
        assertTrue(usrs.checkPassword("13910110054", "123456"));
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
        //assertEquals("123456", u.password());
        assertTrue(usrs.checkPassword("13910110054", "123456"));
        assertEquals(xiaobai.id(), u.id());
        assertEquals("xiaobai", u.name());
        assertEquals("13910110054", u.phone());
        assertNull(u.email());

        oHome = io.check(null, u.home());
        assertEquals(u.name(), oHome.name());

        // 按照 Name 获取
        u = usrs.fetch("xiaobai");
        //assertEquals("123456", u.password());
        assertTrue(usrs.checkPassword("xiaobai", "123456"));
        assertEquals(xiaobai.id(), u.id());
        assertEquals("13910110054", u.phone());
        assertEquals("xiaobai", u.name());
        assertNull(u.email());

        // 设置邮箱
        usrs.setEmail("xiaobai", "xiaobai@nutzam.com");

        // 按照 Name 获取
        u = usrs.fetch("xiaobai");
        //assertEquals("123456", u.password());
        assertTrue(usrs.checkPassword("xiaobai", "123456"));
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
        //assertEquals("123456", u.password());
        assertTrue(usrs.checkPassword("xiaobai", "123456"));
        assertEquals(xiaobai.id(), u.id());
        assertEquals("xiaobai", u.name());
        assertEquals("xiaobai", u.group());
        assertNull(u.phone());
        assertNull(u.email());

        // 检查 HOME
        WnObj oHome = io.check(null, u.home());

        // 检查权限设定
        WnObj oMe = io.check(oHome, "/grp/" + u.group() + "/people/" + u.id());
        assertEquals(Wn.ROLE.ADMIN, oMe.getInt("role"));
        assertEquals(Wn.ROLE.ADMIN, usrs.getRoleInGroup(u, u.group()));

        // 删除就什么也没了
        usrs.delete(u);
        assertNull(usrs.fetch("xiaobai"));

        // 但是组目录还在
        io.check(oHome, "/grp/" + u.group() + "/people/" + u.id());

    }

}
