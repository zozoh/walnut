package com.site0.walnut.ext.net.xapi;

import com.site0.walnut.ext.net.xapi.bean.XApiRequest;
import org.nutz.web.WebException;

public class XApiException extends WebException {

    public XApiException(Throwable e) {
        super(e);
    }

    public XApiException(XApiRequest req, String code, String reason) {
        this.key("e.ThirdXApi." + req.getApiName() + "." + code);
        if (null == reason) {
            reason = req.getPath();
        } else {
            reason = req.getPath() + " : " + reason;
        }
        this.reason(reason);
    }

    public XApiException(XApiRequest req, String code) {
        this(req, code, null);
    }

}
