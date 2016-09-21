package org.nutz.walnut.impl.srv;

/**
 * 提供了许可证验证相关逻辑的封装
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
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
        return null;
    }

    /**
     * 根据激活码获取许可证对象
     * 
     * @param acode
     *            激活码
     * @return 许可证
     */
    public WnLicence getLicence(WnActiveCode acode) {
        return null;
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
