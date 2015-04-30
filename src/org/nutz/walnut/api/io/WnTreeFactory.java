package org.nutz.walnut.api.io;

public interface WnTreeFactory {

    WnTree check(WnNode nd);

    WnTree get(String key);

}
