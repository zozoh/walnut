package org.nutz.walnut.ext.sshd.srv;

import org.apache.sshd.common.session.Session.AttributeKey;
import org.nutz.walnut.api.auth.WnAuthSession;

public class WnSshd {

    public static AttributeKey<WnAuthSession> KEY_WN_SESSION = new AttributeKey<WnAuthSession>();
}
