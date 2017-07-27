package org.nutz.walnut.ext.sshd.srv;

import org.apache.sshd.common.session.Session.AttributeKey;
import org.nutz.walnut.api.usr.WnSession;

public class WnSshd {

    public static AttributeKey<WnSession> KEY_WN_SESSION = new AttributeKey<WnSession>();
}
