package org.nutz.walnut;

import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

public class BaseSessionTest extends BaseUsrTest {

    protected WnAccount me;

    protected WnAuthSession session;

    @Override
    protected void on_before() {
        super.on_before();

        // 准备测试用户
        WnAccount me = auth.getAccount("demo");
        if (null == me) {
            me = auth.createAccount(new WnAccount("demo"));
        }

        // 创建测试会话
        session = auth.createSession(me, true);

        // 切换会话当前用户
        Wn.WC().setMe(me);
    }

    @Override
    protected void on_after() {
        super.on_after();
    }

    protected String APH(String ph) {
        return Wn.normalizeFullPath(ph, session);
    }

    protected WnObj _created(String ph) {
        String aph = APH(ph);
        return io.createIfNoExists(null, aph, WnRace.DIR);
    }

    protected WnObj _createf(String ph) {
        String aph = APH(ph);
        return io.createIfNoExists(null, aph, WnRace.FILE);
    }

    protected WnObj _write(String ph, String text) {
        WnObj o = _createf(ph);
        io.writeText(o, text);
        return o;
    }

    protected WnObj _write_by(String oph, String fph) {
        WnObj o = _createf(oph);
        String text = Files.read(fph);
        text = setup.explainConfig(text);
        io.writeText(o, text);
        return o;
    }

    protected void _init_files(String confPath) {
        String path = "org/nutz/walnut/" + confPath;
        String content = Files.read(path);
        String[] lines = Strings.splitIgnoreBlank(content, "\n");
        this._init_files_by(lines);
    }

    protected void _init_files_by(String... paths) {
        for (String ph : paths) {
            String aph = Wn.normalizeFullPath(ph, session);
            WnRace race = aph.endsWith("/") ? WnRace.DIR : WnRace.FILE;
            io.create(null, aph, race);
        }
    }

}
