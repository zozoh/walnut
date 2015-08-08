package org.nutz.walnut.impl.io;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.io.WnSync;

public class WnSyncImpl implements WnSync {

    private Map<String, Lock> map;

    public WnSyncImpl() {
        map = new WeakHashMap<String, Lock>();
    }

    private Lock _check_lock(String key) {
        Lock lo = map.get(key);
        if (null == lo) {
            synchronized (map) {
                lo = map.get(key);
                if (null == lo) {
                    lo = new ReentrantLock();
                    map.put(key, lo);
                }
            }
        }
        return lo;
    }

    @Override
    public void mutex_lock(String key) {
        Lock l = _check_lock(key);
        l.lock();
    }

    @Override
    public void mutex_unlock(String key) {
        Lock l = _check_lock(key);
        l.unlock();
    }

    private void _mutex(String key, Callback<Lock> callback) {
        Lock l = _check_lock(key);
        l.lock();
        try {
            callback.invoke(l);
        }
        finally {
            l.unlock();
        }
    }

    @Override
    public void mutex(String key, final Atom atom) {
        _mutex(key, new Callback<Lock>() {
            public void invoke(Lock obj) {
                atom.run();
            }
        });
    }

    @Override
    public <T> T mutex2(String key, Proton<T> proton) {
        mutex(key, proton);
        return proton.get();
    }

    @Override
    public void cnd_await(String key, final long ms) {
        _mutex(key, new Callback<Lock>() {
            public void invoke(Lock l) {
                Condition cnd = l.newCondition();
                try {
                    if (0 == ms)
                        cnd.await();
                    else
                        cnd.await(ms, TimeUnit.MILLISECONDS);
                }
                catch (InterruptedException e) {
                    throw Lang.wrapThrow(e);
                }
            }
        });
    }

    @Override
    public void cnd_signal(String key) {
        _mutex(key, new Callback<Lock>() {
            public void invoke(Lock l) {
                Condition cnd = l.newCondition();
                cnd.signal();
            }
        });
    }

    @Override
    public void cnd_signalAll(String key) {
        _mutex(key, new Callback<Lock>() {
            public void invoke(Lock l) {
                Condition cnd = l.newCondition();
                cnd.signalAll();
            }
        });
    }

}
