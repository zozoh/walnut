package org.nutz.walnut;

import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

public class BaseSessionTest extends BaseUsrTest {

    protected WnAccount me;

    protected WnAuthSession session;

    @Override
    protected void on_before() {
        super.on_before();

        // 准备测试用户
        WnAccount me = auth.getAccount("wendal");
        if (null == me) {
            me = auth.createAccount(new WnAccount("wendal"));
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
