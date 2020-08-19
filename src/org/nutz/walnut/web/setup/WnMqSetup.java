package org.nutz.walnut.web.setup;

import java.util.List;

import org.nutz.ioc.Ioc;
import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.walnut.ext.mq.WnMqApi;
import org.nutz.walnut.ext.mq.WnMqException;
import org.nutz.walnut.web.WnConfig;

public class WnMqSetup implements Setup {

    private static final Log log = Logs.get();

    private WnMqApi api;

    @Override
    public void init(NutConfig nc) {
        Ioc ioc = nc.getIoc();
        WnConfig conf = ioc.get(WnConfig.class, "conf");
        this.api = ioc.get(WnMqApi.class, "messageQueueApi");

        // 获取主题
        String beanName = conf.get("mq-api-name", "messageQueueApi");
        List<String> topicList = conf.getList("mq-consumers");
        if (null == topicList || topicList.isEmpty()) {
            topicList.add("sys");
        }

        // 循环主题列表，并注册
        for (String topic : topicList) {

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
