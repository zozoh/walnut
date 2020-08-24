package org.nutz.walnut.core.eot.mongo;

import org.nutz.mongo.annotation.MoField;
import org.nutz.walnut.api.io.WnExpiObj;

public class MoExpiObj implements WnExpiObj {

    @MoField("id")
    private String id;

    @MoField("expi")
    private long expiTime;

    @MoField("hold")
    private long holdTime;

    @MoField("ow")
    private String owner;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getExpiTime() {
        return expiTime;
    }

    @Override
    public long getHoldTime() {
        return holdTime;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setExpiTime(long expiTime) {
        this.expiTime = expiTime;
    }

    public void setHoldTime(long holdTime) {
        this.holdTime = holdTime;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}
