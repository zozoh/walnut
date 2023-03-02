package org.nutz.walnut.ext.old.push;

import java.util.Map;

import org.nutz.walnut.impl.box.WnSystem;

public interface XXPush {

    String send(WnSystem sys, String text, String platform, String alias, String msgtype, Map<String, String> extras);
}
