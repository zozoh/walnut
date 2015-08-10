package org.nutz.walnut.api.io;

public interface WnHandleManager {

    WnHandle create();

    WnHandle get(String hid);

    WnHandle check(String hid);

    void save(WnHandle hdl);

    void remove(String hid);

    void dropAll();
}
