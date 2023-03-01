package org.nutz.walnut.ext.net.sendmail.api;

import org.apache.commons.mail.EmailException;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.net.sendmail.bean.WnMail;

public interface WnMailApi {

    void smtp(WnMail mail, NutBean vars) throws EmailException;

}
