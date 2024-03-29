package com.site0.walnut.ext.data.www;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.www.bean.WnOrder;
import com.site0.walnut.ext.data.www.bean.WnOrderStatus;
import com.site0.walnut.ext.data.www.impl.WnWebService;
import com.site0.walnut.ext.net.payment.WnPay3xRe;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import org.nutz.web.ajax.Ajax;

/**
 * 提供了域名映射的查询和管理功能
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class cmd_www extends JvmHdlExecutor {

    private static final Log log = Wlog.getCMD();

    public static WnObj checkSite(WnSystem sys, JvmHdlContext hc) {
        String site = hc.params.val_check(0);
        return checkSite(sys, site);
    }

    public static WnObj checkSite(WnSystem sys, String site) {
        if (Wn.isFullObjId(site)) {
            return sys.io.checkById(site);
        }

        return Wn.checkObj(sys, site);
    }

    public static WnAccount checkTargetUser(WnSystem sys,
                                            WnWebService webs,
                                            String unm,
                                            String ticket) {
        WnAccount me = sys.getMe();
        WnAccount u;
        // -u 模式，则当前操作会话必须为站点管理员或者 root/op组成员
        if (!Strings.isBlank(unm)) {
            // 检查权限
            String domainGroup = webs.getSite().getDomainGroup();
            if (!sys.auth.isAdminOfGroup(me, domainGroup)) {
                if (!sys.auth.isMemberOfGroup(me, "root", "op")) {
                    throw Er.create("e.cmd.www.nopvg");
                }
            }
            // 通过
            u = webs.getAuthApi().checkAccount(unm);
        }
        // 否则就用当前会话
        else if (!Strings.isBlank(ticket)) {
            u = webs.getAuthApi().checkSession(ticket).getMe();
        }
        // unm/ticket 必须得有一个啊
        else {
            throw Er.create("e.cmd.www.LackTarget");
        }
        return u;
    }

    /**
     * 为订单准备支付单
     * 
     * @param sys
     *            系统上下文
     * @param webs
     *            服务类接口
     * @param or
     *            订单
     * @param bu
     *            支付者
     * @param upick
     *            创建支付单时，从用户元数据挑选选数据
     */
    public static void prepareToPayOrder(WnSystem sys,
                                         WnWebService webs,
                                         WnOrder or,
                                         WnAccount bu,
                                         NutMap upick) {

        // 防守一下
        if (null == bu || null == or) {
            return;
        }

        // 得到账户目录
        WnObj oAccountDir = webs.getSite().getAccountDir();

        // -------------------------------
        // 得到支付类型
        String payType = or.getPayType();

        // -------------------------------
        // 准备元数据
        NutMap meta = new NutMap();
        meta.put("or_id", or.getId());

        // -------------------------------
        // 检查回调
        WnObj oCallback = Wn.getObj(sys, "~/.domain/payment/after");

        // -------------------------------
        // 准备命令
        List<String> cmds = new LinkedList<>();
        cmds.add("pay create");
        if (!Strings.isBlank(payType)) {
            String sellerName = webs.getSite().getSellerName(payType);
            cmds.add("-pt '" + payType + "'");
            cmds.add("-ta '" + sellerName + "'");
        }
        cmds.add("-br '" + or.getTitle() + "'");
        cmds.add("-bu " + oAccountDir.id() + ":" + bu.getId());
        cmds.add("-fee " + Math.round(or.getFee() * 100f));
        if (or.hasCurrency()) {
            cmds.add("-cur " + or.getCurrency());
        }
        if (null != oCallback) {
            cmds.add("-callback id:" + oCallback.id());
        }
        cmds.add("-meta");

        // 从 buyer 的元数据中 copy 更多的的元数据信息，譬如 wx_openid 之类的
        // 这里是要做一个映射，因为 buyer 那边可能是 wx_mp_abc:"XXX"
        // 到支付单这边，就应该变成 "wx_openid" 因为微信支付(JSAPI)的实现，需要这个固定的元数据
        if (null != upick && !upick.isEmpty()) {
            NutMap buBean = bu.toBean();
            for (String key : upick.keySet()) {
                Object val = buBean.get(key);
                String k2 = upick.getString(key);
                meta.put(k2, val);
            }
        }

        String cmdText = Strings.join(" ", cmds);

        // -------------------------------
        // 执行
        if (log.isInfoEnabled()) {
            log.infof("Create Payment for [%s] by:\n   %s", or.getId(), cmdText);
        }
        String input = Json.toJson(meta, JsonFormat.compact());
        String re = sys.exec2(cmdText, input);
        WnPay3xRe payRe = Json.fromJson(WnPay3xRe.class, re);
        String payId = payRe.getPayObjId();

        // 更新订单的支付单关联
        or.setPayReturn(payRe);
        or.setPayId(payId);
        or.setStatus(WnOrderStatus.WT);
        or.setWaitAt(Wn.now());
        NutMap orMeta = or.toMeta("^(pay_re|pay_id|or_st|wt_at|pay_tp)$", null);
        webs.getOrderApi().updateOrder(or.getId(), orMeta, sys);

    }

    /**
     * 将订单信息写入系统输出流
     * 
     * @param sys
     *            系统上下文
     * @param hc
     *            上下文
     * @param or
     *            订单对象
     */
    public static void outputOrder(WnSystem sys, JvmHdlContext hc, WnOrder or) {
        Object re = null;
        if (null != or) {
            re = or.toMeta();
        }
        if (hc.params.is("ajax")) {
            if (null != re) {
                re = Ajax.ok().setData(re);
            } else {
                re = Ajax.fail().setErrCode("e.cmd.www.order.noexists");
            }
        }
        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

    public static void outputJsonOrAjax(WnSystem sys, Object data, JvmHdlContext hc) {
        boolean ajax = hc.params.is("ajax");
        outputJsonOrAjax(sys, data, hc.jfmt, ajax);
    }

    public static void outputJsonOrAjax(WnSystem sys, Object data, JsonFormat jfmt, boolean ajax) {
        Object reo = data;
        if (ajax) {
            reo = Ajax.ok().setData(data);
        }
        String json = Json.toJson(reo, jfmt);
        sys.out.println(json);
    }
}
