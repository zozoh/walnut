package org.nutz.walnut.core.bean;

import org.nutz.walnut.api.io.WnObj;

public class WnObjId {

    private String homeId;

    private String myId;

    public WnObjId(String str) {
        if (null != str) {
            int pos = str.indexOf(':');
            if (pos < 0) {
                this.homeId = null;
                this.myId = str.trim();
            } else {
                this.homeId = str.substring(0, pos).trim();
                this.myId = str.substring(pos + 1).trim();
            }
        }
    }

    public WnObjId(WnObj oHome, WnObj obj) {
        if (null != oHome) {
            this.homeId = oHome.id();
        }
        this.myId = obj.id();
    }

    public boolean hasHomeId() {
        return null != homeId;
    }

    public boolean isInSubMapping() {
        return null != homeId;
    }

    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId;
    }

    public String toString() {
        if (null != homeId) {
            return homeId + ":" + myId;
        }
        return myId;
    }

}
