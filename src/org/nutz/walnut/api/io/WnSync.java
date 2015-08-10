package org.nutz.walnut.api.io;

import org.nutz.trans.Atom;
import org.nutz.trans.Proton;

public interface WnSync {

    void mutex_lock(String key);

    void mutex_unlock(String key);

    void mutex(String key, Atom atom);

    <T> T mutex2(String key, Proton<T> proton);

    void cnd_await(String key, long ms);

    void cnd_signal(String key);

    void cnd_signalAll(String key);

}
