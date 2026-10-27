package com.site0.walnut.security.otp.impl.sender;

import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

import com.site0.walnut.ext.net.mailx.bean.MailxSmtpConfig;
import com.site0.walnut.ext.net.mailx.bean.WnSmtpMail;
import com.site0.walnut.ext.net.mailx.impl.WnMailPosting;
import com.site0.walnut.security.otp.WnOTPSender;
import com.site0.walnut.security.otp.bean.WnOTPBean;
import com.site0.walnut.security.otp.bean.WnOTPType;

public abstract class WnEmailOTPSender implements WnOTPSender {

    @Override
    public WnOTPType getType() {
        return WnOTPType.EMAIL;
    }

    /**
     * @return 发送邮件的配置
     */
    protected abstract MailxSmtpConfig getSmtpConfig();

    /**
     * @param otp
     *            动态密码信息
     * @return 渲染后的邮件对象
     */
    protected abstract WnSmtpMail buildSmtpEmail(WnOTPBean otp);

    /**
     * @return 邮件发送进行时
     */
    protected abstract WnMailPosting getMailPosting();

    /**
     * 将结果写成日志
     * 
     * @param otp
     *            动态密码信息
     * @param result
     *            邮件发送结果
     */
    protected abstract void writeLog(WnOTPBean otp, AjaxReturn result);

    @Override
    public AjaxReturn send(WnOTPBean otp) {
        // 生成 Email
        WnSmtpMail mail = buildSmtpEmail(otp);

        // 获取邮件发送配置
        MailxSmtpConfig config = getSmtpConfig();

        // 发送邮件
        WnMailPosting posting = getMailPosting();
        WebException ree = posting.send(config, mail);

        // 处理结果
        AjaxReturn result = Ajax.ok().setData(mail);
        // 成功
        if (null == ree) {
            result = Ajax.ok().setData(mail.toInfo());
        }
        // 失败
        else {
            result = Ajax.fail().setData(ree);
            result.setErrCode("e.otp.mail.sendFailed");
        }

        // 写入结果
        writeLog(otp, result);

        // 搞定返回
        return result;

    }

}
