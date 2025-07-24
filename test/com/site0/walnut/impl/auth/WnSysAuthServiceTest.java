package com.site0.walnut.impl.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.log.Log;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;

import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import com.site0.walnut.BaseUsrTest;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.login.role.WnRoleLoader;
import com.site0.walnut.login.role.WnRoleType;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;

public class WnSysAuthServiceTest extends BaseUsrTest {

    static Log log = Wlog.getTEST();

    @Test
    public void usr_create_by_email() {
        log.info("@Test WnSysAuthServiceTest.usr_create_by_email Begin");
        WnUser xiaobai = user_passwd("xiaobai@nutzam.com", "123456");
        //prepareSession(xiaobai);
        

        // 获取一个
        WnUser u = auth.getUser("xiaobai@nutzam.com");
        // assertEquals("123456", u.password());
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName(xiaobai.getName()));
        assertTrue(u.isSameMainGroup(xiaobai.getMainGroup()));
        assertEquals("xiaobai@nutzam.com", u.getEmail());
        assertEquals("xiaobai", u.getName());
        assertNull(u.getPhone());
        assertEquals("/home/xiaobai", u.getMetaString("HOME"));

        // 检查主目录
        assertEquals("/home/xiaobai", xiaobai.getHomePath());
        WnObj oHome = io.check(null, u.getHomePath());
        assertEquals(u.getMainGroup(), oHome.name());
        assertEquals("xiaobai", oHome.creator());
        assertEquals("xiaobai", oHome.mender());
        assertEquals("xiaobai", oHome.group());
        assertEquals(488, oHome.mode());

        // 改个名
        u.setName("xiaobai");
        auth.updateUserName(u);

        // 再次按照 Email 获取
        u = auth.getUser("xiaobai@nutzam.com");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("xiaobai@nutzam.com", u.getEmail());
        assertNull(u.getPhone());

        // 检查主目录
        oHome = io.check(null, u.getHomePath());
        assertEquals(u.getName(), oHome.name());
        assertEquals(u.getName(), oHome.name());
        assertEquals("xiaobai", oHome.mender());
        assertEquals("xiaobai", oHome.creator());
        assertEquals("xiaobai", oHome.group());
        assertEquals(488, oHome.mode());

        // 按照 Name 获取
        u = auth.getUser("xiaobai");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("xiaobai@nutzam.com", u.getEmail());
        assertNull(u.getPhone());

        // 设置手机
        u.setPhone("13910110054");
        auth.updateUserPhone(u);

        // 按照 手机 获取
        u = auth.getUser("13910110054");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("xiaobai@nutzam.com", u.getEmail());
        assertEquals("13910110054", u.getPhone());

        oHome = io.check(null, u.getHomePath());
        assertEquals("xiaobai", oHome.name());
        assertEquals("xiaobai", oHome.name());
        assertEquals("xiaobai", oHome.mender());
        assertEquals("xiaobai", oHome.creator());
        assertEquals("xiaobai", oHome.group());
        assertEquals(488, oHome.mode());

        //clearSession();
        log.info("@Test WnSysAuthServiceTest.usr_create_by_email End");
    }

    @Test
    public void usr_create_by_phone() {
        log.info("@Test WnSysAuthServiceTest.usr_create_by_phone Begin");
        WnUser xiaobai = user_passwd("13910110054", "123456");
        assertEquals("/home/13910110054", xiaobai.getHomePath());

        // 获取一个
        WnUser u = auth.getUser("13910110054");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName(xiaobai.getName()));
        assertEquals("13910110054", u.getPhone());
        assertNull(u.getEmail());
        assertEquals("/home/13910110054", u.getMetaString("HOME"));

        WnObj oHome = io.check(null, u.getHomePath());
        assertEquals(u.getName(), oHome.name());

        // 改个名
        u.setName("xiaobai");
        auth.updateUserName(u);
        auth.updateUserHomeName(u, "xiaobai");

        // 再次按照 Phone 获取
        u = auth.getUser("13910110054");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("13910110054", u.getPhone());
        assertNull(u.getEmail());

        oHome = io.check(null, u.getHomePath());
        assertEquals(u.getName(), oHome.name());

        // 按照 Name 获取
        u = auth.getUser("xiaobai");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("13910110054", u.getPhone());
        assertNull(u.getEmail());

        // 设置邮箱
        u.setEmail("xiaobai@nutzam.com");
        auth.updateUserEmail(u);

        // 按照 Name 获取
        u = auth.getUser("xiaobai");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("13910110054", u.getPhone());
        assertEquals("xiaobai@nutzam.com", u.getEmail());

        oHome = io.check(null, u.getHomePath());
        assertEquals(u.getName(), oHome.name());
        log.info("@Test WnSysAuthServiceTest.usr_create_by_phone End");
    }

    @Test
    public void test_forbidden_read() {
        // 创建两个用户
        final WnUser ua = user_passwd("userA", "123456");
        final WnUser ub = user_passwd("userB", "123456");

        // 设置权限监控
        WnContext wc = Wn.WC();

        try {
            // A 用户建立一个文件
            final String path = Wn.appendPath(ua.getHomePath(), "/aaa.txt");
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
                // 因为没加到组里，所以不能进入这个目录
                assertEquals("e.io.forbidden : /home/userA", e.toString());
            }

            // 把 B 用户加入到组里就能读
            auth.addRole(ub, ua.getMainGroup(), WnRoleType.MEMBER);

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
                str = wc.su(ub, new Proton<String>() {
                    protected String exec() {
                        io.check(null, path);
                        throw Wlang.impossible();
                    }
                });
                fail();
            }
            catch (Exception e) {
                // 因为加到组里，但是没有读取目标文件的权限，所以 check 的时候就为不存在
                assertEquals("e.io.obj.noexists : /home/userA/aaa.txt", e.toString());
            }
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
                // 因为加到组里，但是没有读取目标文件的权限，所以 check 的时候就为不存在
                assertEquals("e.io.forbidden : /home/userA/aaa.txt", e.toString());
            }

            // 只有变成管理员
            auth.addRole(ub, ua.getMainGroup(), WnRoleType.ADMIN);

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
        WnUser xiaobai = user_passwd("xiaobai", "123456");

        WnSession se = auth.loginByPassword("xiaobai", "123456");

        assertEquals(se.getMyId(), xiaobai.getId());
        assertEquals(se.getMyName(), xiaobai.getName());
        assertEquals(se.getMyGroup(), xiaobai.getMainGroup());
        assertEquals(se.getEnv().getString("HOME"), xiaobai.getHomePath());

        // 检查对象
        WnObj oSe = io.check(null, "/var/session/" + se.getTicket());
        assertTrue(oSe.expireTime() > (Wn.now() + 5000));

        // 获取
        WnSession se2 = auth.checkSession(se.getTicket());
        assertTrue(se.isSame(se2));
        assertEquals(se2.getMyId(), xiaobai.getId());
        assertEquals(se2.getMyName(), xiaobai.getName());
        assertEquals(se2.getMyGroup(), xiaobai.getMainGroup());
        assertEquals(se2.getEnv().getString("HOME"), xiaobai.getHomePath());

        // 当前会话是重读出来的那个
        se = se2;

        // 设置环境变量
        // TODO zozoh 自从改了自制的 nanoTime 以后，就老过不去
        // 看起来是因为在很短的时间内，重复写文件，导致历史记录产生了问题？
        try {
            se.getEnv().put("say", "hello");
            se.getEnv().put("x", "100");
            auth.saveSessionEnv(se);

            se = auth.checkSession(se.getTicket());
            assertEquals("100", se.getEnv().getString("x"));
            assertEquals("hello", se.getEnv().getString("say"));

            // 删除环境变量
            se.getEnv().put("say", null);
            se.getEnv().put("x", null);
            auth.saveSessionEnv(se);

            se = auth.checkSession(se.getTicket());
            assertNull(se.getEnv().get("x"));
            assertNull(se.getEnv().get("say"));

            // 注销
            auth.logout(se.getTicket());
            se = auth.getSession(se.getTicket());
            assertNull(se);

        }
        catch (Throwable e) {
            System.out.println(Json.toJson(se.toBean()));
            System.out.println("--------------------check again:");
            se = auth.checkSession(se.getTicket());
            System.out.println(Json.toJson(se.toBean()));
            System.out.println("--------------------Obj:");
            WnObj oSe2 = io.check(null, "/sys/session/" + se.getTicket());
            System.out.println(Json.toJson(oSe2));
            throw e;
        }
    }

    @Test
    public void test_forbidden_write() {
        // 创建两个用户
        final WnUser ua = user_passwd("userA", "123456");
        final WnUser ub = user_passwd("userB", "123456");

        // 设置权限监控
        WnContext wc = Wn.WC();

        wc.setSecurity(security);
        try {
            // A 用户建立一个文件，改变权限
            final String path = Wn.appendPath(ua.getHomePath(), "/aaa.txt");
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
                // 因为没加到组里，所以不能进入这个目录
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
            auth.addRole(ub, ua.getMainGroup(), WnRoleType.MEMBER);

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
            auth.addRole(ub, ua.getMainGroup(), WnRoleType.ADMIN);

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

    // @Test
    public void usr_create_delete() {
        WnUser xiaobai = user_passwd("xiaobai", "123456");
        WnUser xiaohei = user_passwd("xiaohei", "123456");

        assertEquals("/home/xiaobai/", xiaobai.getHomePath());

        // 获取一个
        WnUser u = auth.getUser("xiaobai");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("xiaobai", u.getName());
        assertEquals("xiaobai", u.getMainGroup());
        assertNull(u.getPhone());
        assertNull(u.getEmail());

        // 重新获取一遍
        xiaobai = auth.checkUser("xiaobai");
        xiaohei = auth.checkUser("xiaohei");

        // 检查 HOME
        WnObj oHome = io.check(null, u.getHomePath());

        // 检查权限设定
        WnRoleLoader rl = auth.roleLoader(null);
        assertEquals(WnRoleType.ADMIN, rl.getRoleTypeOfGroup(xiaobai, xiaobai.getMainGroup()));
        assertEquals(WnRoleType.GUEST, rl.getRoleTypeOfGroup(xiaohei, xiaobai.getMainGroup()));

        assertEquals(WnRoleType.ADMIN, rl.getRoleTypeOfGroup(xiaohei, xiaohei.getMainGroup()));
        assertEquals(WnRoleType.GUEST, rl.getRoleTypeOfGroup(xiaobai, xiaohei.getMainGroup()));

        // 权限也就是访客了
        assertEquals(WnRoleType.GUEST, rl.getRoleTypeOfGroup(xiaohei, "nogroup"));

        // 但是主目录还在
        WnObj oHome2 = io.check(null, u.getHomePath());
        assertEquals(oHome.id(), oHome2.id());
    }

}
