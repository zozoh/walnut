package org.nutz.walnut.ooml;

public class OomlRelationship {

    private String id;

    private String target;

    private OomlRelType type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isType(OomlRelType type) {
        return this.type == type;
    }

    public OomlRelType getType() {
        return type;
    }

    public void setType(OomlRelType type) {
        this.type = type;
    }

}
