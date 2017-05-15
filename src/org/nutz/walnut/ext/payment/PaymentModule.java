package org.nutz.walnut.ext.payment;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.walnut.web.module.AbstractWnModule;

/**
 * 提供基础支付方面的支持
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
@At("/pay")
public class PaymentModule extends AbstractWnModule {

}
