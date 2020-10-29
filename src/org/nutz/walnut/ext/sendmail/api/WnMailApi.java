package org.nutz.walnut.ext.sendmail.api;

import org.apache.commons.mail.EmailException;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.sendmail.bean.WnMail;

public interface WnMailApi {

    void smtp(WnMail mail, NutBean vars) throws EmailException;

}
