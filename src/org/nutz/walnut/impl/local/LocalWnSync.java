package org.nutz.walnut.impl.local;

import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.io.WnSync;

public class LocalWnSync implements WnSync {

    private long delaySignalAll;

    static class MyCondition implements Condition {

        private Condition cnd;

        private long lastSignalAll;

        private long _delay_signal_all;

        MyCondition(Condition cnd, long dsa) {
            this.cnd = cnd;
            this.lastSignalAll = -1;
            this._delay_signal_all = dsa;
        }

        boolean _not_be_signaled_already() {
            if (_delay_signal_all > 0)
                return System.currentTimeMillis() - lastSignalAll > _delay_signal_all;
            return true;
        }

        public void await() throws InterruptedException {
            if (_not_be_signaled_already())
                cnd.await();
        }

        public void awaitUninterruptibly() {
            throw Lang.noImplement();
        }

        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            throw Lang.noImplement();
        }

        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            if (_not_be_signaled_already())
                return cnd.await(time, unit);
            return false;
        }

        public boolean awaitUntil(Date deadline) throws InterruptedException {
            throw Lang.noImplement();
        }

        public void signal() {
            cnd.signal();
        }

        public void signalAll() {
            cnd.signalAll();
            this.lastSignalAll = System.currentTimeMillis();
        }

    }

    static class Lock extends ReentrantLock {

        private Map<String, MyCondition> cnds;

        public Lock() {
            super();
            cnds = new WeakHashMap<String, MyCondition>();
        }

        MyCondition checkCondition(String type, long dsa) {
            MyCondition cnd = cnds.get(type);
            if (null == cnd) {
                synchronized (cnds) {
                    cnd = cnds.get(type);
                    if (null == cnd) {
                        cnd = new MyCondition(this.newCondition(), dsa);
                        cnds.put(type, cnd);
                    }
                }
            }
            return cnd;
        }

        boolean hasCondition(String type) {
            return cnds.containsKey(type);
        }

    }

    private Map<String, Lock> map;

    public LocalWnSync() {
        map = new WeakHashMap<String, Lock>();
        delaySignalAll = 100;
    }

    private Lock _check_lock(String key) {
        Lock lo = map.get(key);
        if (null == lo) {
            synchronized (map) {
                lo = map.get(key);
                if (null == lo) {
                    lo = new Lock();
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
    public void cnd_await(String key, final String type, final long ms) {
        _mutex(key, new Callback<Lock>() {
            public void invoke(Lock l) {
                Condition cnd = l.checkCondition(type, delaySignalAll);
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
    public void cnd_signal(String key, final String type) {
        _mutex(key, new Callback<Lock>() {
            public void invoke(Lock l) {
                Condition cnd = l.checkCondition(type, delaySignalAll);
                cnd.signal();
            }
        });
    }

    @Override
    public void cnd_signalAll(String key, final String type) {
        _mutex(key, new Callback<Lock>() {
            public void invoke(Lock l) {
                Condition cnd = l.checkCondition(type, delaySignalAll);
                cnd.signalAll();
            }
        });
    }

}
