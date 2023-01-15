package org.nutz.walnut.ext.net.payment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.net.payment.alipay.ZfbQrcodePay3x;
import org.nutz.walnut.ext.net.payment.alipay.ZfbScanPay3x;
import org.nutz.walnut.ext.net.payment.free.FreePay3x;
import org.nutz.walnut.ext.net.payment.paypal.PaypalPay3x;
import org.nutz.walnut.ext.net.payment.weixin.WxJsApiPay3x;
import org.nutz.walnut.ext.net.payment.weixin.WxQrcodePay3x;
import org.nutz.walnut.ext.net.payment.weixin.WxScanPay3x;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;

/**
 * 通用的付款流程逻辑封装接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean(create = "on_create")
public class WnPayment {

    @Inject
    private WnRun run;

    private Map<WnPayType, WnPay3x> _3xes;

    public WnPayment() {
        _3xes = new HashMap<>();
        _3xes.put(WnPayType.WX_JSAPI, new WxJsApiPay3x());
        _3xes.put(WnPayType.WX_QRCODE, new WxQrcodePay3x());
        _3xes.put(WnPayType.WX_SCAN, new WxScanPay3x());
        _3xes.put(WnPayType.ZFB_QRCODE, new ZfbQrcodePay3x());
        _3xes.put(WnPayType.ZFB_SCAN, new ZfbScanPay3x());
        _3xes.put(WnPayType.PAYPAL, new PaypalPay3x());
        _3xes.put(WnPayType.FREE, new FreePay3x());
    }

    public void on_create() {
        for (WnPay3x p3x : _3xes.values()) {
            p3x.run = this.run;
            p3x.io = this.run.io();
        }
    }

    private WnPay3x _3X(WnPayObj po) {
        WnPayType payType = po.getPayType();
        WnPay3x pay = _3xes.get(payType);
        if (null == pay) {
            throw Er.create("e.pay.no3x", po.id() + " -> " + payType);
        }
        return pay;
    }

    private void __assert_the_seller(WnPayObj po) {
        // 得到当前操作用户
        WnAccount me = Wn.WC().getMe();

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // 权限检查
        // 如果不是 root/op 组成员只能设置自己域的支付单
        if (!po.isTheSeller(me)) {
            if (!run.auth().isMemberOfGroup(me, "root", "op")) {
                throw Er.create("e.pay.nopvg");
            }
        }
    }

    private WnPay3xRe __re(WnPayObj po, WnPay3xRe re) {
        // 如果订单成功, 去掉过期时间
        if (po.isStatusOk()) {
            po.expireTime(-1);
            re.addChangeKeys("expi");
        }
        // 如果订单失败，延长7天的过期时间，以便追踪问题
        else if (po.isStatusFail()) {
            po.expireTime(Wn.now() + 86400000L * 7);
            re.addChangeKeys("expi");
        }

        // 持久化改动
        if (re.hasChangedKeys()) {
            String regex = "^(" + Strings.join("|", re.getChangedKeys()) + ")$";
            NutBean meta = po.pickBy(regex);
            run.io().appendMeta(po, meta);
        }

        // 确保设置了 poId
        re.setPayObjId(po.id());

        // 返回
        return re;
    }

    /**
     * 创建一个支付单
     * 
     * @param pi
     *            支付单创建信息
     * 
     * @return 支付单对象
     */
    public WnPayObj create(WnPayInfo wpi) {
        // 执行操作
        return run.nosecurity(new Proton<WnPayObj>() {
            protected WnPayObj exec() {
                return __do_create(wpi);
            }
        });
    }

    /**
     * 获取一个支付单
     * 
     * @param poId
     *            支付单 ID
     * @param quiet
     *            true 如果订单不存在，返回null； false 不存在抛错
     * 
     * @return 支付单对象
     * 
     * @throws "e.pay.noexist"
     *             不存在
     * @throws "e.pay.outOfHome"
     *             不在指定 Home 中
     * 
     */
    public WnPayObj get(String poId, boolean quiet) {
        return run.nosecurity(new Proton<WnPayObj>() {
            protected WnPayObj exec() {
                return __do_get(poId, quiet);
            }
        });
    }

    /**
     * 查询一组支付单
     * 
     * @param q
     *            查询条件
     * @return 支付单列表
     */
    public List<WnPayObj> query(WnQuery q) {
        return run.nosecurity(new Proton<List<WnPayObj>>() {
            protected List<WnPayObj> exec() {
                return __do_query(q);
            }
        });
    }

    /**
     * 在第三方平台创建订单
     * 
     * @param po
     *            支付单对象
     * @param payType
     *            第三方平台类型，支持
     *            <ul>
     *            <li>wx.qrcode : 微信主动扫付款码
     *            <li>wx.jsapi : 微信公众号支付
     *            <li>wx.scan : 微信被物理码枪扫码支付
     *            <li>zfb.scan : 支付宝主动扫付款码
     *            </ul>
     * 
     * @param target
     *            付款目标（比如商户号）
     * 
     * @param args
     *            更多发送请求时需要的参数，是不用持久化的
     * 
     * @return 支付单处理结果
     */
    public WnPay3xRe send(WnPayObj po, String payType, String target, String... args) {
        if (po.isSended()) {
            return po.getPayReturn();
        }
        return run.nosecurity(new Proton<WnPay3xRe>() {
            protected WnPay3xRe exec() {
                return __do_send(po, payType, target, args);
            }
        });
    }

    /**
     * 在第三方平台检查支付单状态
     * 
     * @param po
     *            支付单对象
     * 
     * @return 支付单处理结果
     */
    public WnPay3xRe check(WnPayObj po) {
        if (po.isDone()) {
            return po.getPayReturn();
        }
        return run.nosecurity(new Proton<WnPay3xRe>() {
            protected WnPay3xRe exec() {
                return __do_check(po);
            }
        });
    }

    /**
     * 对支付单进行后续处理
     * 
     * @param po
     *            支付单对象
     * @param req
     *            第三方平台返回的支付结果参数表
     * 
     * @return 支付单处理结果
     */
    public WnPay3xRe complete(WnPayObj po, NutMap req) {
        if (po.isDone()) {
            return po.getPayReturn();
        }
        return run.nosecurity(new Proton<WnPay3xRe>() {
            protected WnPay3xRe exec() {
                return __do_complete(po, req);
            }
        });
    }

    private WnPayObj __do_create(WnPayInfo wpi) {
        // 得到当前操作用户
        WnAccount me = Wn.WC().getMe();

        // 确保买家的信息完备
        wpi.assertBuyerPerfect();

        // 确保卖家的信息完备
        WnAccount seller = wpi.checkSeller(run.auth(), me);

        // 确保有简介
        wpi.checkBrief();

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // 权限检查
        // 执行操作的如果不是 root 组管理员，那么标定的卖家必须是自己
        if (!me.isSameId(wpi.seller_id)) {
            if (!run.auth().isAdminOfGroup(me, "root")) {
                throw Er.create("e.pay.nopvg");
            }
        }
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // 查看后续回调脚本
        String callback = null;
        if (wpi.hasCallback()) {
            callback = wpi.readCallback(run.io(), seller);
        }

        // 得到主目录
        WnObj oPayHome = WnPays.getPayHome(run.io());

        // ....................................................
        // 设置元数据
        NutMap meta = new NutMap();

        // 用户自定义的元数据优先级最低
        if (null != wpi.meta && wpi.meta.size() > 0) {
            meta.putAll(wpi.meta);
        }

        // 关键元数据
        NutMap m2 = Lang.obj2map(wpi, NutMap.class);
        m2.remove("meta");
        meta.putAll(m2);

        // 固定的初始化值
        meta.put("tp", "wn_payment");
        // meta.put("c", wpi.buyer_nm); // 将创建者设置成买家
        if (!meta.containsKey(WnPays.KEY_CUR))
            meta.put(WnPays.KEY_CUR, "RMB");
        meta.setnx(WnPays.KEY_ST, WnPay3xStatus.NEW);
        meta.setnx(WnPays.KEY_SEND_AT, 0);
        meta.setnx(WnPays.KEY_CLOSE_AT, 0);

        // ....................................................
        // 创建对象
        WnObj oPayObj = run.io().create(oPayHome, "${id}", WnRace.FILE);

        // ....................................................
        // 持久化
        run.io().appendMeta(oPayObj, meta);

        // 根据模板写入回调脚本
        if (!Strings.isBlank(callback)) {
            String text = WnTmpl.exec(callback, oPayObj, false);
            run.io().writeText(oPayObj, text);
        }

        // 返回
        WnPayObj po = new IoWnPayObj();
        po.updateBy(oPayObj);
        return po;
    }

    private WnPayObj __do_get(String poId, boolean quiet) {
        // 得到当前操作用户
        WnAccount me = Wn.WC().getMe();

        // 执行获取
        WnObj o = run.io().get(poId);
        if (null == o) {
            if (quiet)
                return null;
            throw Er.create("e.pay.noexists", poId);
        }

        WnObj oPayHome = WnPays.getPayHome(run.io());
        if (!o.isMyParent(oPayHome)) {
            if (quiet)
                return null;
            throw Er.create("e.pay.outOfHome", poId);
        }

        // 转换为支付单对象
        WnPayObj po = new IoWnPayObj();
        po.updateBy(o);

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // 权限检查
        // 如果不是 root/op 组成员只能获取自己域的支付单
        if (!po.isTheSeller(me)) {
            if (!run.auth().isMemberOfGroup(me, "root", "op")) {
                if (quiet)
                    return null;
                throw Er.create("e.pay.nopvg");
            }
        }

        // 返回对象
        return po;
    }

    private List<WnPayObj> __do_query(WnQuery q) {
        // 得到当前操作用户
        WnAccount me = Wn.WC().getMe();

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // 权限检查
        // 如果不是 root/op 组成员只能查询自己的域
        if (!run.auth().isMemberOfGroup(me, "root", "op")) {
            q.setv(WnPays.KEY_SELLER_ID, me.getId());
            q.unset(WnPays.KEY_SELLER_NM);
        }

        // 确保 pid 是付款目录
        WnObj oPayHome = WnPays.getPayHome(run.io());
        q.setv("pid", oPayHome.id());

        // 必须限制一下大小
        if (q.limit() <= 0) {
            q.limit(10);
        }

        // 得到返回值
        List<WnPayObj> list = new LinkedList<>();
        run.io().each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                WnPayObj po = new IoWnPayObj();
                po.updateBy(o);
                list.add(po);
            }
        });
        return list;
    }

    private WnPay3xRe __do_send(WnPayObj po, String payType, String target, String... args) {
        // 检查权限
        __assert_the_seller(po);

        // 首先保存一下支付类型
        po.setPayType(payType);

        // 得到接口
        WnPay3x pay = _3X(po);

        // 设置商户号
        po.setPayTarget(target);

        // 执行
        WnPay3xRe re = pay.send(po, args);

        // 记录结果
        po.setStatus(re.getStatus());
        po.setReturnType(re.getDataType());
        po.setReturnData(re.getData());
        po.setSendAt(Wn.now());
        re.addChangeKeys(WnPays.KEY_PAY_TP,
                         WnPays.KEY_PAY_TARGET,
                         WnPays.KEY_ST,
                         WnPays.KEY_RE_TP,
                         WnPays.KEY_RE_OBJ,
                         WnPays.KEY_SEND_AT);

        // 持久化中间的执行步骤并返回
        return __re(po, re);
    }

    private WnPay3xRe __do_check(WnPayObj po) {
        // 检查权限
        __assert_the_seller(po);

        // 得到原先的状态
        WnPay3xStatus oldSt = po.getStatus();

        // 得到接口
        WnPay3x pay = _3X(po);

        // 执行
        WnPay3xRe re = pay.check(po);

        // 记录结果
        if (oldSt != re.getStatus()) {
            po.setStatus(re.getStatus());
            po.setReturnType(re.getDataType());
            po.setReturnData(re.getData());
            re.addChangeKeys(WnPays.KEY_ST, WnPays.KEY_RE_TP, WnPays.KEY_RE_OBJ);
        }

        // 持久化中间的执行步骤并返回
        return __re(po, re);
    }

    private WnPay3xRe __do_complete(WnPayObj po, NutMap req) {
        // 检查权限
        __assert_the_seller(po);

        // 得到接口
        WnPay3x pay = _3X(po);

        // 执行
        WnPay3xRe re = pay.complete(po, req);

        // 记录结果
        if (re.isDone()) {
            po.setStatus(re.getStatus());
            po.setReturnType(re.getDataType());
            po.setReturnData(re.getData());
            po.setCloseAt(Wn.now());
            re.addChangeKeys(WnPays.KEY_ST,
                             WnPays.KEY_RE_TP,
                             WnPays.KEY_RE_OBJ,
                             WnPays.KEY_CLOSE_AT);

        }

        // 持久化中间的执行步骤并返回
        return __re(po, re);
    }

}
