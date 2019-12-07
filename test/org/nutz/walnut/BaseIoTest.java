package org.nutz.walnut;

import java.util.HashMap;
import java.util.Map;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Mirror;
import org.nutz.mongo.ZMoCo;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.impl.auth.WnSysAuthServiceWrapper;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.impl.io.WnIoImpl;
import org.nutz.walnut.impl.io.WnMounter;
import org.nutz.walnut.impl.io.mnt.LocalFileMounter;
import org.nutz.walnut.impl.io.mnt.MemoryMounter;
import org.nutz.walnut.impl.io.mongo.MongoWnTree;
import org.nutz.walnut.util.Wn;

public abstract class BaseIoTest extends BaseStoreTest {

    protected WnIo io;

    protected WnAuthService auth;

    protected WnAccount root;

    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);

        WnTree tree = _create_tree();
        tree._clean_for_unit_test();

        // 设置 Io 接口
        io = new WnIoImpl();

        Mirror.me(io).setValue(io, "tree", tree);
        Mirror.me(io).setValue(io, "store", store);
        Mirror.me(io).setValue(io, "mimes", mimes);

        // 为接口设置映射器
        Map<String, WnMounter> mounters = new HashMap<>();
        mounters.put("memory", new MemoryMounter());
        mounters.put("file", new LocalFileMounter());
        Mirror.me(tree).setValue(tree, "mounters", mounters);

        // 准备会话校验接口
        auth = _create_auth_service();

        // 准备根用户
        root = auth.checkAccount("root");

        // 默认每个测试运行都是用 root
        Wn.WC().setMe(root);
    }

    protected void on_after(PropertiesProxy pp) {
        // 默认每个测试运行都是用 root
        Wn.WC().setMe(root);
    }

    private WnAuthService _create_auth_service() {
        WnSysAuthServiceWrapper auth = new WnSysAuthServiceWrapper();
        auth.setIo(io);
        auth.setRootDefaultPasswd("123456");
        auth.setSeDftDu(3600);
        auth.setSeTmpDu(60);
        auth.on_create();
        return auth;
    }

    private WnTree _create_tree() {
        ZMoCo co = db.getCollection("obj");

        String id = conf.get("root-id");

        WnObj root = new WnBean();
        root.id(id);
        root.path("/");
        root.race(WnRace.DIR);
        root.name("");
        root.lastModified(System.currentTimeMillis());
        root.createTime(System.currentTimeMillis());
        root.creator("root").mender("root").group("root");
        root.mode(0755);

        return new MongoWnTree(co, root, mimes);
    }

}
