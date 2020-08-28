package org.nutz.walnut.ext.entity.buy;

import java.util.List;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.entity.JvmRedisEntityExecutor;
import org.nutz.walnut.ext.entity.RedisEntityPrinter;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.WnStr;
import org.nutz.walnut.util.ZParams;

public class cmd_buy extends JvmRedisEntityExecutor<BuyIt> {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqn", "^(ajax|json|rever|quiet|reset)$");

        String action = params.val_check(0);
        String uid = params.val_check(1);

        // 准备配置
        WedisConfig conf = this.prepareConfig(sys, params, "buy");

        // 准备接口
        BuyApi api = new WnRedisBuyService(conf);

        // 准备返回
        Object re;

        // buy it
        if ("it".equals(action)) {
            String taId = params.val_check(2);
            int count = 1;
            if (params.vals.length > 3) {
                count = params.val_check_int(3);
            }
            boolean reset = params.is("reset");
            int count2 = api.buyIt(uid, taId, count, reset);

            // 读取对象
            if (params.is("obj")) {
                BuyIt it = new BuyIt();
                it.setCount(count2);
                it.setName(taId);
                WnObj obj = sys.io.get(taId);
                it.setObj(obj);
                re = it;
            }
            // 默认就设一个值咯
            else {
                re = count2;
            }
        }
        // buy remove
        else if ("rm".equals(action)) {
            int len = params.vals.length - 2;
            if (len > 0) {
                String[] taIds = new String[len];
                System.arraycopy(params.vals, 2, taIds, 0, len);
                String[] taIds2 = WnStr.flatArray(taIds);
                System.arraycopy(params.vals, 2, taIds2, 0, len);
                re = api.remove(uid, taIds);
            } else {
                re = 0;
            }
        }
        // buy cancel
        else if ("clean".equals(action)) {
            re = api.clean(uid);
        }
        // buy list all
        else if ("all".equals(action)) {
            List<BuyIt> list;
            if (params.is("rever")) {
                list = api.revAll(uid);
            } else {
                list = api.getAll(uid);
            }
            // 读取对象
            if (params.is("obj")) {
                for (BuyIt it : list) {
                    String oid = it.getName();
                    WnObj obj = sys.io.get(oid);
                    it.setObj(obj);
                }
            }

            // 计入返回
            re = list;
        }
        // buy count
        else if ("count".equals(action)) {
            re = api.count(uid);
        }
        // buy sum
        else if ("sum".equals(action)) {
            re = api.sum(uid);
        }
        // buy is
        else if ("get".equals(action)) {
            String taId = params.val_check(2);
            int dft = 0;
            if (params.vals.length > 3) {
                dft = params.val_check_int(3);
            }
            re = api.getBuy(uid, taId, dft);
        }
        // 不支持的动作
        else {
            throw Er.create("e.cmd.buy.invalidAction", action);
        }

        // 输出
        if (!params.is("quiet")) {
            String fmt = params.get("out", "%d) %d <- %s");
            output(sys, params, re, new RedisEntityPrinter<BuyIt>() {
                public void print(BuyIt it, int i) {
                    sys.out.printlnf(fmt, i++, it.getCount(), it.getName());
                }
            });
        }
    }

}
