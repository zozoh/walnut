package org.nutz.walnut.ooml;

import org.nutz.walnut.util.Wpath;
import org.nutz.walnut.util.Ws;

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

    public void renameSuffix(String suffixName) {
        this.target = Wpath.renameSuffix(target, suffixName);
    }

    public String getTargetType() {
        return Wpath.getSuffixName(this.target);
    }

    public boolean isTargetType(String typeName) {
        String tt = this.getTargetType();
        if (null == typeName) {
            return Ws.isEmpty(tt);
        }
        return typeName.equals(tt);
    }

    public boolean isType(OomlRelType type) {
        return this.type == type;
    }

    public OomlRelType getType() {
        return type;
    }

    public String getTypeName(String prefix) {
        if (null != type) {
            String s = Ws.camelCase(type.toString());
            return prefix + s;
        }
        return null;
    }

    public void setType(OomlRelType type) {
        this.type = type;
    }

}
