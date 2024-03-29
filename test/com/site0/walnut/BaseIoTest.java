package com.site0.walnut;

import org.junit.After;
import org.junit.Before;
import org.nutz.trans.Proton;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.core.IoCoreTest;
import com.site0.walnut.impl.auth.WnSysAuthServiceWrapper;
import com.site0.walnut.util.Wn;

public abstract class BaseIoTest extends IoCoreTest {

    protected WnIo io;

    protected WnAuthService auth;

    protected WnAccount root;

    @Before
    public void setUp() throws Exception {
        // 清空测试数据
        setup.cleanAllData();

        // 设置 Io 接口
        io = setup.getIo();

        // 准备会话校验接口
        auth = _create_auth_service();
        
        // 为一些索引管理器或者桶管理器，设置其需要的 auth接口
        setup.getDaoIndexerFactory();

        // 准备根用户
        root = auth.checkAccount("root");

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

    private WnAuthService _create_auth_service() {
        return Wn.WC().nosecurity(io, new Proton<WnAuthService>() {
            protected WnAuthService exec() {
                WnSysAuthServiceWrapper auth = new WnSysAuthServiceWrapper();
                auth.setIo(io);
                auth.setRootDefaultPasswd("123456");
                auth.setSeDftDu(3600);
                auth.setSeTmpDu(60);
                auth.on_create(); // 此时，会检查 root 用户，并确保自动创建
                return auth;
            }
        });
    }

}
