package org.nutz.walnut.ext.xapi;

import org.nutz.walnut.ext.xapi.bean.ThirdXRequest;
import org.nutz.web.WebException;

public class ThirdXException extends WebException {

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