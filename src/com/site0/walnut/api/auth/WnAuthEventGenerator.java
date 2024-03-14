package com.site0.walnut.api.auth;

public interface WnAuthEventGenerator {

    WnAuthEvent create(String name, WnAccount account, WnAuthSession session);

}
