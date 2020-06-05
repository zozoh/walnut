package org.nutz.walnut.ext.thing;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.util.Wn;

public abstract class ThingDataAction<T> extends ThingAction<T> {

    protected String dirName;

    protected WnObj oT;

    private WnObj oDir;

    public String getDirName() {
        return dirName;
    }

    public ThingDataAction<T> setDirName(String dirName) {
        this.dirName = dirName;
        return this;
    }

    public WnObj getThing() {
        return oT;
    }

    public ThingDataAction<T> setThing(WnObj oT) {
        this.oT = oT;
        return this;
    }

    public WnObj myDir() {
        if (null == oDir) {
            WnObj oData = Things.dirTsData(io, oTs);
            String rph = Wn.appendPath(oT.id(), dirName);
            oDir = io.createIfNoExists(oData, rph, WnRace.DIR);
        }
        return oDir;
    }

    public WnQuery _Q() {
        WnQuery q = Wn.Q.pid(myDir());
        q.setv("race", WnRace.FILE);
        return q;
    }

}
