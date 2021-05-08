package org.nutz.walnut.ext.net.xapi;

import org.nutz.walnut.ext.net.xapi.bean.ThirdXRequest;
import org.nutz.web.WebException;

public class ThirdXException extends WebException {

    public ThirdXException(Throwable e) {
        super(e);
    }

    public ThirdXException(ThirdXRequest req, String code, String reason) {
        this.key("e.ThirdXApi." + req.getApiName() + "." + code);
        if (null == reason) {
            reason = req.getPath();
        } else {
            reason = req.getPath() + " : " + reason;
        }
        this.reason(reason);
    }

    public ThirdXException(ThirdXRequest req, String code) {
        this(req, code, null);
    }

}
