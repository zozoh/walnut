package org.nutz.walnut.ext.entity.newsfeed;

import java.util.Arrays;

import org.nutz.dao.Dao;
import org.nutz.lang.Files;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.sql.WnDaos;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_newsfeed extends JvmHdlExecutor {

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        // 记录从哪里 copy args 的位置
        int pos;

        // 没有参数
        if (hc.args.length == 0) {
            throw Er.create("e.cmd.bizhook.lackArgs", hc.args);
        }
        // 第一个参数就是 hdl，那么用 "default" 作为 feedName
        // :> thing hdlName xxx
        else if (null != this.getHdl(hc.args[0])) {
            String feedPath = "~/.domain/newsfeed/default.json";
            hc.oRefer = Wn.checkObj(sys, feedPath);
            hc.hdlName = hc.args[0];
            pos = 1;
        }
        // 第一个参数表示一个 newsfeed 的配置文件名
        // 路径固定是在 ~/.domain/newsfeed/ 目录下
        else if (hc.args.length >= 2) {
            String feedName = hc.args[0];
            if (!feedName.endsWith(".json")) {
                feedName += ".json";
            }
            String feedPath = "~/.domain/newsfeed/" + feedName;
            hc.oRefer = Wn.checkObj(sys, feedPath);
            hc.hdlName = hc.args[1];
            pos = 2;
        }
        // 否则还是缺参数
        else {
            throw Er.create("e.cmd.thing.lackArgs", hc.args);
        }

        // Copy 剩余参数
        hc.args = Arrays.copyOfRange(hc.args, pos, hc.args.length);

    }

    @Override
    protected void _before_invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 读取数据源的配置信息
        FeedConfig config = sys.io.readJson(hc.oRefer, FeedConfig.class);
        if (!config.hasFeedName()) {
            String feedName = Files.getMajorName(hc.oRefer.name());
            config.setFeedName(feedName);
        }
        hc.put("config", config);

        // 读取数据库连接信息
        String url = config.getJdbcUrl();
        String usernm = config.getJdbcUserName();
        String passwd = config.getJdbcPassword();
        Dao dao = WnDaos.getOrCreate(url, usernm, passwd);
        hc.put("dao", dao);

        // 准备 API
        NewfeedApi api = new WnNewsfeedApi(config, dao);
        hc.put("api", api);
    }
}
