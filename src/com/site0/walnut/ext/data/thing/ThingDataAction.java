package com.site0.walnut.ext.data.thing;

import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.util.Wn;

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

    public boolean hasDirName(String fnm) {
        return null != myFileDirName(fnm);
    }

    public String myFilePath(String fnm) {
        if (this.hasDirName(fnm))
            return Wn.appendPath(dirName, fnm);
        return fnm;
    }

    public String myFileDirName(String fnm) {
        if (!Strings.isBlank(this.dirName))
            return this.dirName;
        String pph = Files.getParent(fnm);
        if ("/".equals(pph))
            return null;
        return Files.getName(pph);
    }

    public WnObj myDir() {
        if (null == oDir) {
            WnObj oData = Things.dirTsData(io, oTs);
            if (!Strings.isBlank(dirName)) {
                String rph = Wn.appendPath(oT.myId(), dirName);
                oDir = io.createIfNoExists(oData, rph, WnRace.DIR);
            } else {
                oDir = io.createIfNoExists(oData, oT.myId(), WnRace.DIR);
            }
        }
        return oDir;
    }

    public WnQuery _Q() {
        WnQuery q = Wn.Q.pid(myDir());
        q.setv("race", WnRace.FILE);
        return q;
    }

}
