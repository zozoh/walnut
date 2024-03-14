package com.site0.walnut.ext.net.sshd.srv;

import org.apache.sshd.common.session.Session.AttributeKey;
import com.site0.walnut.api.auth.WnAuthSession;

public class WnSshd {

    public static AttributeKey<WnAuthSession> KEY_WN_SESSION = new AttributeKey<WnAuthSession>();
}
