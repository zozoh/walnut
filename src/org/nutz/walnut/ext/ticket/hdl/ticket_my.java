package org.nutz.walnut.ext.ticket.hdl;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

/**
 * 管理我的工单，普通用户只能通过该接口与工单系统交互
 * 
 * 一个用户可以同时注册不同的工单系统
 * 
 * @author pw
 * 
 */

public class ticket_my implements JvmHdl {

    private Log log = Logs.get();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        ZParams params = ZParams.parse(hc.args, null);
        // 需要指定工单系统的名称
        String tservice = params.get("ts");

        // 注册

        // 查询我的工单

        // 提交工单

    }

}