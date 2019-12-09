package org.nutz.walnut.impl.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.BaseUsrTest;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.auth.WnAuths;
import org.nutz.walnut.api.auth.WnGroupRole;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class TestWnSysAuthService extends BaseUsrTest {

    @Test
    public void test_forbidden_write() {
        // 创建两个用户
        final WnAccount ua = auth.createAccount(new WnAccount("userA", "123456"));
        final WnAccount ub = auth.createAccount(new WnAccount("userB", "123456"));

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
            auth.setGroupRole(ub, ua.getGroupName(), WnGroupRole.MEMBER);

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
            auth.setGroupRole(ub, ua.getGroupName(), WnGroupRole.ADMIN);

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
        final WnAccount ua = auth.createAccount(new WnAccount("userA", "123456"));
        final WnAccount ub = auth.createAccount(new WnAccount("userB", "123456"));

        // 设置权限监控
        WnContext wc = Wn.WC();

        wc.setSecurity(security);
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
            auth.setGroupRole(ub, ua.getGroupName(), WnGroupRole.MEMBER);

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
                        throw Lang.impossible();
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
            auth.setGroupRole(ub, ua.getGroupName(), WnGroupRole.ADMIN);

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
        WnAccount xiaobai = auth.createAccount(new WnAccount("xiaobai", "123456"));

        WnAuthSession se = auth.loginByPasswd("xiaobai", "123456");

        assertEquals(se.getMyId(), xiaobai.getId());
        assertEquals(se.getMyName(), xiaobai.getName());
        assertEquals(se.getMyGroup(), xiaobai.getGroupName());
        assertEquals(se.getVars().getString("HOME"), xiaobai.getHomePath());

        // 检查对象
        WnObj oSe = io.check(null, "/var/session/" + se.getTicket());
        assertTrue(oSe.expireTime() > (System.currentTimeMillis() + 5000));

        // 获取
        WnAuthSession se2 = auth.checkSession(se.getTicket());
        assertEquals(se2.getId(), se2.getId());
        assertTrue(se.isSame(se2));
        assertEquals(se2.getMyId(), xiaobai.getId());
        assertEquals(se2.getMyName(), xiaobai.getName());
        assertEquals(se2.getMyGroup(), xiaobai.getGroupName());
        assertEquals(se2.getVars().getString("HOME"), xiaobai.getHomePath());

        // 当前会话是重读出来的那个
        se = se2;

        // 设置环境变量
        // TODO zozoh 自从改了自制的 nanoTime 以后，就老过不去
        // 看起来是因为在很短的时间内，重复写文件，导致历史记录产生了问题？
        try {
            se.getVars().put("say", "hello");
            se.getVars().put("x", "100");
            auth.saveSessionVars(se);

            se = auth.checkSession(se.getTicket());
            assertEquals("100", se.getVars().getString("x"));
            assertEquals("hello", se.getVars().getString("say"));

            // 删除环境变量
            se.getVars().put("say", null);
            se.getVars().put("x", null);
            auth.saveSessionVars(se);

            se = auth.checkSession(se.getTicket());
            assertNull(se.getVars().get("x"));
            assertNull(se.getVars().get("say"));

            // 注销
            auth.logout(se.getTicket());
            se = auth.getSession(se.getTicket());
            assertNull(se);

        }
        catch (Throwable e) {
            System.out.println(Json.toJson(se.toMapForClient()));
            System.out.println("--------------------check again:");
            se = auth.checkSession(se.getTicket());
            System.out.println(Json.toJson(se.toMapForClient()));
            System.out.println("--------------------Obj:");
            WnObj oSe2 = io.check(null, "/sys/session/" + se.getTicket());
            System.out.println(Json.toJson(oSe2));
            throw e;
        }
    }

    @Test
    public void usr_create_by_email() {
        WnAccount xiaobai = auth.createAccount(new WnAccount("xiaobai@nutzam.com", "123456"));

        // 获取一个
        WnAccount u = auth.getAccount("xiaobai@nutzam.com");
        // assertEquals("123456", u.password());
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName(xiaobai.getName()));
        assertTrue(u.isSameId(xiaobai.getGroupName()));
        assertTrue(u.isSameGroup(xiaobai.getGroupName()));
        assertTrue(u.isSameGroup(u.getId()));
        assertEquals("xiaobai@nutzam.com", u.getEmail());
        assertNull(u.getPhone());

        // 检查主目录
        assertEquals("/home/" + xiaobai.getId() + "/", xiaobai.getHomePath());
        WnObj oHome = io.check(null, u.getHomePath());
        assertEquals(u.getId(), oHome.name());
        assertEquals("root", oHome.mender());
        assertEquals(u.getName(), oHome.creator());
        assertEquals(u.getId(), oHome.group());
        assertEquals(488, oHome.mode());

        // 改个名
        auth.renameAccount(u, "xiaobai");

        // 再次按照 Email 获取
        u = auth.getAccount("xiaobai@nutzam.com");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("xiaobai@nutzam.com", u.getEmail());
        assertNull(u.getPhone());

        // 检查主目录
        oHome = io.check(null, u.getHomePath());
        assertEquals(u.getName(), oHome.name());
        assertEquals(u.getName(), oHome.name());
        assertEquals("root", oHome.mender());
        assertEquals(u.getName(), oHome.creator());
        assertEquals(u.getName(), oHome.group());
        assertEquals(488, oHome.mode());

        // 按照 Name 获取
        u = auth.getAccount("xiaobai");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("xiaobai@nutzam.com", u.getEmail());
        assertNull(u.getPhone());

        // 设置手机
        u.setPhone("13910110054");
        auth.saveAccount(u, WnAuths.ABMM.LOGIN);

        // 按照 手机 获取
        u = auth.getAccount("13910110054");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("xiaobai@nutzam.com", u.getEmail());
        assertEquals("13910110054", u.getPhone());

        oHome = io.check(null, u.getHomePath());
        assertEquals(u.getName(), oHome.name());
        assertEquals(u.getName(), oHome.name());
        assertEquals("root", oHome.mender());
        assertEquals(u.getName(), oHome.creator());
        assertEquals(u.getName(), oHome.group());
        assertEquals(488, oHome.mode());

    }

    @Test
    public void usr_create_by_phone() {
        WnAccount xiaobai = auth.createAccount(new WnAccount("13910110054", "123456"));

        assertEquals("/home/" + xiaobai.getId() + "/", xiaobai.getHomePath());

        // 获取一个
        WnAccount u = auth.getAccount("13910110054");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName(xiaobai.getName()));
        assertEquals("13910110054", u.getPhone());
        assertNull(u.getEmail());

        WnObj oHome = io.check(null, u.getHomePath());
        assertEquals(u.getId(), oHome.name());

        // 改个名
        auth.renameAccount(u, "xiaobai");

        // 再次按照 Phone 获取
        u = auth.getAccount("13910110054");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("13910110054", u.getPhone());
        assertNull(u.getEmail());

        oHome = io.check(null, u.getHomePath());
        assertEquals(u.getName(), oHome.name());

        // 按照 Name 获取
        u = auth.getAccount("xiaobai");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("13910110054", u.getPhone());
        assertNull(u.getEmail());

        // 设置邮箱
        u.setEmail("xiaobai@nutzam.com");
        auth.saveAccount(u, WnAuths.ABMM.LOGIN);

        // 按照 Name 获取
        u = auth.getAccount("xiaobai");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("13910110054", u.getPhone());
        assertEquals("xiaobai@nutzam.com", u.getEmail());

        oHome = io.check(null, u.getHomePath());
        assertEquals(u.getName(), oHome.name());

    }

    @Test
    public void usr_create_delete() {
        WnAccount xiaobai = auth.createAccount(new WnAccount("xiaobai", "123456"));
        WnAccount xiaohei = auth.createAccount(new WnAccount("xiaohei", "123456"));

        assertEquals("/home/xiaobai/", xiaobai.getHomePath());

        // 获取一个
        WnAccount u = auth.getAccount("xiaobai");
        assertTrue(u.isMatchedRawPasswd("123456"));
        assertTrue(u.isSameId(xiaobai.getId()));
        assertTrue(u.isSameName("xiaobai"));
        assertEquals("xiaobai", u.getName());
        assertEquals("xiaobai", u.getGroupName());
        assertNull(u.getPhone());
        assertNull(u.getEmail());

        // 重新获取一遍
        xiaobai = auth.checkAccount("xiaobai");
        xiaohei = auth.checkAccount("xiaohei");

        // 检查 HOME
        WnObj oHome = io.check(null, u.getHomePath());

        // 检查权限设定
        assertEquals(WnGroupRole.ADMIN, auth.getGroupRole(xiaobai, xiaobai.getGroupName()));
        assertEquals(WnGroupRole.GUEST, auth.getGroupRole(xiaohei, xiaobai.getGroupName()));

        assertEquals(WnGroupRole.ADMIN, auth.getGroupRole(xiaohei, xiaohei.getGroupName()));
        assertEquals(WnGroupRole.GUEST, auth.getGroupRole(xiaobai, xiaohei.getGroupName()));

        // 删除就什么也没了
        auth.deleteAccount(u);
        assertNull(auth.getAccount("xiaobai"));

        // 权限也就是访客了
        assertEquals(WnGroupRole.GUEST, auth.getGroupRole(u, u.getGroupName()));

        // 但是主目录还在
        WnObj oHome2 = io.check(null, u.getHomePath());
        assertEquals(oHome.id(), oHome2.id());
    }

}
