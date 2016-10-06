package org.nutz.walnut.impl.srv;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;

/**
 * 提供了许可证验证相关逻辑的封装
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean(name = "licenceService")
public class WnLicenceService extends WnService {

    /**
     * 获取一个用户的激活码，这个激活码必须是“已使用”状态。但是无所谓是否过期
     * <ul>
     * <li>一个客户在服务商域里只能有一个激活码
     * <li>如果客户域不存在 acode 的索引文件，或者索引文件损坏，恢复它
     * </ul>
     * 
     * @param ali
     *            应用许可证信息
     * @param clientName
     *            客户名称
     * @return 激活码对象
     */
    public WnActiveCode getActiveCode(WnAppLicenceInfo ali, String clientName) {
        // 首先得到客户对象
        WnUsr client = usrService.check(clientName);

        // 首先从服务商处得到 ActiveCode
        WnObj oHomeProvider = io.check(null, "/home/" + ali.getProvider());
        WnObj oHomeAcode = io.check(oHomeProvider, ".acode");
        WnQuery q = Wn.Q.pid(oHomeAcode);
        q.setv("ow_dmn_id", oHomeProvider.id());
        q.setv("buyer_id", client.id());
        q.setv("ac_app", ali.getAppName());
        WnObj oAcode = io.getOne(q);

        // 不存在，直接返回 null
        if (null == oAcode)
            return null;

        // 再从客户处得到 .code 文件，如果不存在，创建之
        WnObj oHomeClient = io.check(null, "/home/" + clientName);
        WnObj oClientCode = io.createIfNoExists(oHomeClient,
                                                ".app_licence/" + ali.getAppName() + ".code",
                                                WnRace.FILE);

        // 文件的内容必须是激活码的 ID，如果不相等，则修改
        String codeId = Strings.trim(io.readText(oClientCode));
        if (null == codeId || !codeId.equals(oAcode.id())) {
            io.writeText(oClientCode, codeId);
        }

        // 最后返回
        return new WnActiveCode(oAcode);
    }

    /**
     * 根据激活码引用文件得到对应的激活码对象
     * 
     * @param oCode
     *            客户域的激活码引用文件
     * @return 激活码对象
     */
    public WnActiveCode getActiveCode(WnObj oCode) {
        // 得到激活码 ID
        String acodeId = Strings.trim(io.readText(oCode));
        if (Strings.isBlank(acodeId))
            throw Er.create("e.licence.MyCodeEmpty", oCode);

        // 找到激活码文件
        WnObj oAcode = io.checkById(acodeId);

        // 激活码文件的位置必须正确
        if (!oAcode.path().equals("/home/" + oAcode.d1() + "/.acode/" + oAcode.name())) {
            throw Er.create("e.licence.badAcode", oAcode);
        }

        // 生成激活码对象
        WnActiveCode acode = new WnActiveCode(oAcode);

        // 文件名就是 appName
        String appName = Files.getMajorName(oCode.name());

        // 验证激活码对象的内容正确性
        if (!acode.ac_app.equals(appName)) {
            throw Er.create("e.licence.nomatch.ac_app", appName + "!=" + acode.ac_app);
        }
        if (!acode.buyer_nm.equals(oCode.d1())) {
            throw Er.create("e.licence.nomatch.buyer_nm", acode.buyer_nm + "!=" + oCode.d1());
        }
        if (!acode.ow_dmn_nm.equals(oAcode.d1())) {
            throw Er.create("e.licence.nomatch.ow_dmn_nm", acode.ow_dmn_nm + "!=" + oAcode.d1());
        }

        // 返回
        return acode;
    }

    /**
     * 根据激活码获取许可证对象
     * 
     * @param acode
     *            激活码
     * @return 许可证
     */
    public WnLicence getLicence(WnActiveCode acode) {
        // 得到服务商的域
        WnObj oHomeProvider = io.checkById(acode.ow_dmn_id);

        // 得到许可证目录
        WnObj oHomeLicence = io.check(oHomeProvider, ".licence");

        // 得到许可证文件对象
        WnObj oLicence = io.check(oHomeLicence, acode.ac_licence + ".licence");

        // 返回
        return io.readJson(oLicence, WnLicence.class);
    }

    /**
     * 查询激活码对象。
     * <p>
     * <b>!注意</b> <code>clientName</code> 和 <code>privider</code> 必须有一个是非空
     * 
     * @param clientName
     *            客户域名
     * @param provider
     *            提供商域名
     * @param appName
     *            指定应用名称
     * @param pager
     *            翻页信息，只有在 provider 有效的情况下才生效
     * @return 激活码列表
     */
    public List<WnActiveCode> queryActiveCode(String clientName,
                                              String provider,
                                              String appName,
                                              WnPager pager) {
        // 首先得到客户对象
        WnUsr client = Strings.isBlank(clientName) ? null : usrService.check(clientName);

        // 得到服务商的域
        WnObj oHomeProvider = Strings.isBlank(provider) ? null
                                                        : io.check(null, "/home/" + provider);

        // ...............................................
        // 没指定域名提供商
        if (null == oHomeProvider) {
            // 那么必须指定客户域，从客户域里面读取
            if (null == client) {
                throw Er.create("e.licence.noclientName");
            }

            // 找到客户域主目录
            WnObj oHomeClient = io.check(null, client.home());

            // 准备返回值列表

            // 找到客户域的激活码存放目录
            WnObj oHomeMyCode = io.fetch(oHomeClient, ".app_licence");
            if (null == oHomeMyCode) {
                return new ArrayList<WnActiveCode>(1);
            }

            // 如果指定了特殊的 appName
            if (!Strings.isBlank(appName)) {
                WnObj oCode = io.fetch(oHomeMyCode, appName + ".code");
                WnActiveCode acode = this.getActiveCode(oCode);
                return Lang.list(acode);
            }
            // 否则列出全部的激活码
            else {
                List<WnObj> oCodes = io.getChildren(oHomeMyCode, "*.code");
                ArrayList<WnActiveCode> list = new ArrayList<WnActiveCode>(oCodes.size());
                for (WnObj oCode : oCodes) {
                    WnActiveCode acode = this.getActiveCode(oCode);
                    list.add(acode);
                }
                return list;
            }
        }
        // ...............................................
        // 否则再域名提供商域内查询
        else {
            // 得到激活码目录
            WnObj oHomeAcode = io.check(oHomeProvider, ".acode");

            // 准备查询条件
            WnQuery q = Wn.Q.pid(oHomeAcode);
            q.setv("ow_dmn_id", oHomeProvider.id());
            if (null != client) {
                q.setv("buyer_id", client.id());
            }
            if (!Strings.isBlank(appName))
                q.setv("ac_app", appName);

            // 分页信息
            if (null != pager)
                pager.setupQuery(io, q);

            // 查询返回的结果
            List<WnActiveCode> list = new LinkedList<WnActiveCode>();
            io.each(q, new Each<WnObj>() {
                public void invoke(int index, WnObj oAcode, int length) {
                    list.add(new WnActiveCode(oAcode));
                }
            });

            return list;
        }
    }

    /**
     * 判断当前的应用对应某个激活码是否可以自动生成
     * 
     * @param ali
     *            应用许可证信息
     * @param acode
     *            激活码
     * @return true 可以自动生成，false 不可以自动生成
     */
    public boolean isCanAutoGenActiveCode(WnAppLicenceInfo ali, WnActiveCode acode) {
        if (ali.isCanAutoGenCode()) {
            // 已经生成过了，就不能再生成了
            if (null != acode && acode.isAutoGen()) {
                return false;
            }
        }
        // 否则可以自动生成
        return true;
    }

    /**
     * 判断当前的应用对应某个激活码是否可以自动生成
     * 
     * @see #getActiveCode(WnAppLicenceInfo, String)
     * @see #isCanAutoGenActiveCode(WnAppLicenceInfo, WnActiveCode)
     */
    public boolean isCanAutoGenActiveCode(WnAppLicenceInfo ali, String clientName) {
        WnActiveCode acode = this.getActiveCode(ali, clientName);
        return this.isCanAutoGenActiveCode(ali, acode);
    }

}
