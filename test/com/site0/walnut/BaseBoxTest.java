package com.site0.walnut;

import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mongo.ZMoCo;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.box.WnBox;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.box.WnBoxService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmBoxService;
import com.site0.walnut.impl.box.JvmExecutorFactory;
import com.site0.walnut.util.Wn;

public abstract class BaseBoxTest extends BaseUsrTest {

    protected WnBoxService boxes;

    protected WnBox box;

    protected StringBuilder out;

    protected StringBuilder err;

    protected WnAccount me;

    protected WnAuthSession se;

    protected WnBoxContext bc;

    protected String outs() {
        return out.toString();
    }

    protected String touts() {
        String re = Strings.trim(out.toString());
        // 这玩意有问题，看看 mongo 那边还有多少数据
        if (Strings.isBlank(re)) {
            ZMoCo co = setup.getMongoCoObj();
            long n = co.countDocuments();
            System.out.printf("靠!!!数据库里还有记录 【%d】条\n", n);
        }
        return re;
    }

    protected String errs() {
        return err.toString();
    }

    protected String terrs() {
        return Strings.trim(err.toString());
    }

    protected WnObj check(String ph) {
        String path = Wn.normalizeFullPath(ph, se);
        return io.check(null, path);
    }

    protected void cleanOutputAndErr() {
        out.delete(0, out.length());
        err.delete(0, err.length());
    }

    @Override
    protected void on_before() {
        super.on_before();

        // 子类可能需要包裹 Io 实现类
        this.io = this.prepareIo();

        boxes = _create_box_service();

        WnAccount info = new WnAccount("xiaobai", "123456");
        me = auth.createAccount(info);
        se = auth.createSession(me, false);

        // 将测试线程切换到当前测试账号
        Wn.WC().setSession(se);

        out = new StringBuilder();
        err = new StringBuilder();

        // 准备上下文
        bc = new WnBoxContext(null, new NutMap());
        bc.io = io;
        bc.session = se;
        bc.auth = auth;

        box = _alloc_box();
    }

    protected WnIo prepareIo() {
        return this.io;
    }

    protected WnBox _alloc_box() {
        WnBox box = boxes.alloc(0);
        box.setStdin(null);
        box.setStdout(Lang.ops(out));
        box.setStderr(Lang.ops(err));
        box.setup(bc);
        return box;
    }

    @Override
    protected void on_after() {
        boxes.free(box);
        Wn.WC().setSession(null);
        super.on_after();

        // 最后等一下再第二个测试
        Lang.sleep(200);
    }

    private WnBoxService _create_box_service() {
        JvmExecutorFactory jef = new JvmExecutorFactory();
        Mirror.me(jef).setValue(jef, "scanPkgs", Lang.array("com.site0.walnut.impl.box.cmd"));
        return new JvmBoxService(jef);
    }

}
