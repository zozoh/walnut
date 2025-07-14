package com.site0.walnut;

import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.log.Log;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnSimpleUser;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;

public abstract class BaseSessionTest extends BaseUsrTest {

    static Log log = Wlog.getTEST();

    protected WnUser me;

    protected WnUser _old_me;

    protected WnSession session;

    @Override
    protected void on_before() {
        super.on_before();

        log.info("> BaseSessionTest.on_before enter");
        // 准备测试用户
        WnUser me = auth.getUser("demo");
        if (null == me) {
            me = auth.addUser(new WnSimpleUser("demo"));
        }

        // 创建测试会话
        session = auth.createSession(me);
        log.infof("> BaseSessionTest.on_before session => %s", session.getTicket());

        // 切换会话当前用户
        log.infof("> BaseSessionTest.on_before switch me=> %s", me.getName());
        _old_me = Wn.WC().getMe();
        Wn.WC().setMe(me);
        log.info("> BaseSessionTest.on_before quiet");
    }

    @Override
    protected void on_after() {
        super.on_after();
        Wn.WC().setMe(_old_me);
        log.info("> BaseSessionTest.on_after");
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
        String path = "com/site0/walnut/" + confPath;
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
