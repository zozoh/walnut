package org.nutz.walnut.ext.thing;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.util.Things;

public abstract class ThingAction<T> {

    protected WnIo io;

    protected WnObj oTs;

    protected T output;

    public abstract T invoke();

    public WnObj checkThIndex(String id) {
        return Things.checkThIndex(io, oTs, id);
    }

    public WnObj checkDirTsIndex() {
        return Things.dirTsIndex(io, oTs);
    }

    public WnIo getIo() {
        return io;
    }

    public ThingAction<T> setIo(WnIo io) {
        this.io = io;
        return this;
    }

    public WnObj getThingSet() {
        return oTs;
    }

    public ThingAction<T> setThingSet(WnObj oTs) {
        this.oTs = oTs;
        return this;
    }

    public T getOutput() {
        return output;
    }

    public ThingAction<T> setOutput(T output) {
        this.output = output;
        return this;
    }

}
