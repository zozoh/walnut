package org.nutz.walnut.web.setup;

import java.util.List;

import org.nutz.ioc.Ioc;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.walnut.ext.sys.mq.WnMqApi;
import org.nutz.walnut.ext.sys.mq.WnMqException;
import org.nutz.walnut.ext.sys.mq.WnMqHandler;
import org.nutz.walnut.ext.sys.mq.impl.WnMqDefaultHandler;
import org.nutz.walnut.web.WnConfig;

public class WnMqSetup implements Setup {

    private static final Log log = Logs.get();

    private WnMqApi api;

    @Override
    public void init(NutConfig nc) {
        Ioc ioc = nc.getIoc();
        WnConfig conf = ioc.get(WnConfig.class, "conf");

        // 这个开关用来在配置文件里关闭消息队列消费者
        if (!conf.getBoolean("mq-enabled", false)) {
            log.infof("MqSetup: off");
            return;
        }

        // 准备接口
        String beanName = conf.get("mq-api-name", "messageQueueApi");
        log.infof("MqSetup: api=%s", beanName);
        this.api = ioc.get(WnMqApi.class, beanName);

        // 获取监听器
        List<String> lists = conf.getList("mq-listeners");
        if (null == lists || lists.isEmpty()) {
            lists.add("sys");
        }

        // 默认监听器
        WnMqHandler dftHandler = ioc.get(WnMqDefaultHandler.class);

        // 循环主题列表，并注册
        log.infof("init %d listeners", lists.size());
        for (String li : lists) {
            try {
                String[] ss = Strings.splitIgnoreBlank(li, ":");
                // 采用默认监听器
                if (ss.length == 1) {
                    log.infof(" - listen [%s] by default", ss[0]);
                    api.subscribe(ss[0], dftHandler);
                }
                // 指定监听器
                else if (ss.length > 1) {
                    String topic = ss[0];
                    String lisnm = ss[1].substring(1).trim();
                    log.infof(" - listen [%s] by IocBean('%s')", ss[0], lisnm);
                    WnMqHandler hdl = ioc.get(WnMqHandler.class, lisnm);
                    api.subscribe(topic, hdl);
                }
            }
            catch (WnMqException e) {
                throw Lang.wrapThrow(e);
            }
        }
    }

    @Override
    public void destroy(NutConfig nc) {
        if (null != this.api) {
            try {
                this.api.unsubscribe(null);
            }
            catch (WnMqException e) {
                throw Lang.wrapThrow(e);
            }
        }
    }
}
