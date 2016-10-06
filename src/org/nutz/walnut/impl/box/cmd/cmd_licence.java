package org.nutz.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.srv.WnActiveCode;
import org.nutz.walnut.impl.srv.WnLicence;
import org.nutz.walnut.impl.srv.WnLicenceService;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.ZParams;

public class cmd_licence extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数们
        ZParams params = ZParams.parse(args, "cqlnHtihbs", "^(ibase|pager|acode)$");

        // 得到翻页信息
        WnPager wp = new WnPager(params);

        // 分析参数
        String client = params.get("client");
        String provider = params.get("provider");
        String appName = params.get("app");

        // 默认情况下，client 和 provider 什么都没给，则认为是读取自己当前域
        if (Strings.isBlank(client) && Strings.isBlank(provider)) {
            client = sys.me.name();
        }
        final String clientName = client;

        // 检查执行权限
        sys.nosecurity(new Atom() {
            public void run() {
                __check_right(sys, clientName, provider);
            }
        });

        // 进入内核态获取激活码列表
        WnLicenceService licenceService = ioc.get(WnLicenceService.class);
        List<WnActiveCode> acodes = sys.nosecurity(new Proton<List<WnActiveCode>>() {
            protected List<WnActiveCode> exec() {
                return licenceService.queryActiveCode(clientName, provider, appName, wp);
            }
        });

        // 准备输出结果列表
        final List<NutMap> list = new ArrayList<NutMap>(acodes.size());

        // 如果是仅仅输出激活码，直接就输出了
        if (params.is("acode")) {
            for (WnActiveCode acode : acodes)
                list.add(acode.toMap());
        }
        // 否则
        else {
            // 进入内核态
            sys.nosecurity(new Atom() {
                // 转换成 licence
                public void run() {
                    for (WnActiveCode acode : acodes) {
                        WnLicence lice = licenceService.getLicence(acode);
                        list.add(lice.toMap());
                    }
                }
            });
        }

        // 输出结果
        Cmds.output_beans(sys, params, wp, list);
    }

    /**
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * <p>
     * 检查一下权限
     */
    private void __check_right(WnSystem sys, String clientName, String provider) {
        // 任何域的管理员都能查看本域对于任何域提供商任何应用的许可证信息
        if (null != clientName && sys.usrService.isAdminOfGroup(sys.me, clientName))
            return;

        // 任何域的管理与都能查看本域提供的全部许可证
        if (null != provider && sys.usrService.isAdminOfGroup(sys.me, provider))
            return;

        // root 和 op 组不受任何限制
        if (sys.usrService.isMemberOfGroup(sys.me, "root")
            || sys.usrService.isMemberOfGroup(sys.me, "op"))
            return;

        throw Er.create("e.cmd.licence.nopvg");
    }

}
