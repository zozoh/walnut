package com.site0.walnut;

import org.junit.After;
import org.junit.Before;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.core.IoCoreTest;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.usr.WnSimpleUser;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wn;

public abstract class BaseIoTest extends IoCoreTest {

    protected WnIo io;

    protected WnLoginApi auth;

    protected WnUser root;

    protected WnUser user_passwd(String name, String passwd) {
        WnUser u = auth.getUser(name);
        if (null == u) {
            u = new WnSimpleUser(name);
            u.genSaltAndRawPasswd(passwd);
            return auth.addUser(u);
        }
        return u;
    }

    protected WnUser user_grp(String name, String group) {
        return user_grp(name, group, "123456");
    }

    protected WnUser user_grp(String name, String group, String passwd) {
        WnUser u = auth.getUser(name);
        if (null == u) {
            u = new WnSimpleUser(group);
            u.setMainGroup(name);
            u.genSaltAndRawPasswd(passwd);
            return auth.addUser(u);
        }
        return u;
    }

    @Before
    public void setup() throws Exception {
        // 清空测试数据
        setup.cleanAllData();

        // 设置 Io 接口
        io = setup.getIo();

        // 准备会话校验接口
        auth = setup.getLoginApi();

        // 为一些索引管理器或者桶管理器，设置其需要的 auth接口
        setup.getDaoIndexerFactory();

        // 准备根用户
        root = auth.addRootUserIfNoExists("123456");

        // 默认每个测试运行都是用 root
        Wn.WC().setMe(root);

        // 调用自定义的初始化
        this.on_before();
    }

    @After
    public void tearDown() throws Exception {
        this.on_after();
    }

    protected abstract void on_before();

    protected abstract void on_after();

}
