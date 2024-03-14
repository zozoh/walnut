package com.site0.walnut.ext.data.entity;

import java.util.Arrays;

import org.nutz.dao.Dao;
import org.nutz.lang.born.Borning;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.sys.sql.WnDaoConfig;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public abstract class JvmDaoEntityExecutor extends JvmHdlExecutor {

    protected void findHdlName(WnSystem sys, JvmHdlContext hc, String name) {
        // 记录从哪里 copy args 的位置
        int pos;

        // 没有参数
        if (hc.args.length == 0) {
            throw Er.create("e.cmd." + name + ".lackArgs", hc.args);
        }
        // 第一个参数就是 hdl，那么用 "default" 作为 feedName
        // :> thing hdlName xxx
        else if (null != this.getHdl(hc.args[0])) {
            String feedPath = "~/.domain/" + name + "/_" + name + ".json";
            hc.oRefer = Wn.checkObj(sys, feedPath);
            hc.hdlName = hc.args[0];
            pos = 1;
        }
        // 第一个参数表示配置文件名
        // 路径固定是在 ~/.domain/${name}/ 目录下
        else if (hc.args.length >= 2) {
            String confName = hc.args[0];
            if (!confName.endsWith(".json")) {
                confName += ".json";
            }
            String feedPath = "~/.domain/" + name + "/" + confName;
            hc.oRefer = Wn.checkObj(sys, feedPath);
            hc.hdlName = hc.args[1];
            pos = 2;
        }
        // 否则还是缺参数
        else {
            throw Er.create("e.cmd." + name + ".lackArgs", hc.args);
        }

        // Copy 剩余参数
        hc.args = Arrays.copyOfRange(hc.args, pos, hc.args.length);
    }

    protected <T> void setupContext(WnSystem sys,
                                    JvmHdlContext hc,
                                    Class<? extends WnDaoConfig> configType,
                                    Borning<T> born)
            throws Exception {
        // 读取数据源的配置信息
        WnDaoConfig conf = WnDaos.loadConfig(configType, sys, hc.oRefer);
        hc.put("config", conf);

        // 读取数据库连接信息
        Dao dao = WnDaos.get(conf.getAuth());
        hc.put("dao", dao);

        // 准备 API
        T api = born.born(conf, dao);
        hc.put("api", api);
    }
}
