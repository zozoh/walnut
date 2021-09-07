package org.nutz.walnut.ext.media.ooml.tmpl;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;

public class OomlWPlaceholder {

    private String name;

    private OomlWPhType type;

    private String boolTest;

    private String defaultValue;

    private String itemName;

    private CheapElement runProperty;

    private OomlWPhMark runBegin;

    private OomlWPhMark runEnd;

    public String toString() {
        StringBuilder sb = new StringBuilder("${");
        if (null != name) {
            sb.append(name);
        }
        if (null != type && !this.isNormal()) {
            String typeName = Ws.kebabCase(this.type.toString());
            sb.append('<').append(typeName).append('>');
        }
        if (null != boolTest) {
            sb.append("==").append(boolTest);
        }
        if (null != defaultValue) {
            sb.append('?').append(defaultValue);
        }
        if (null != itemName) {
            sb.append(':').append(itemName);
        }
        sb.append('}');
        return sb.toString();
    }

    public String toBrief() {
        StringBuilder sb = new StringBuilder(this.toString());
        if (null != runBegin) {
            sb.append(runBegin.toString());
        }
        if (null != runEnd) {
            sb.append(runEnd.toString());
        }
        return sb.toString();
    }

    public OomlWPlaceholder clone() {
        OomlWPlaceholder ph = new OomlWPlaceholder();
        ph.name = this.name;
        ph.type = this.type;
        ph.boolTest = this.boolTest;
        ph.defaultValue = this.defaultValue;
        ph.itemName = this.itemName;
        ph.runProperty = this.runProperty;
        if (null != this.runBegin) {
            ph.runBegin = this.runBegin.clone();
        }
        if (null != this.runEnd) {
            ph.runEnd = this.runEnd.clone();
        }
        return ph;
    }

    public boolean hasName() {
        return !Ws.isBlank(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String varName) {
        this.name = varName;
    }

    public boolean isNormal() {
        return OomlWPhType.NORMAL == type;
    }

    public boolean isCheckbox() {
        return OomlWPhType.CHECKBOX == type;
    }

    public boolean isPBegin() {
        return OomlWPhType.P_BEGIN == type;
    }

    public boolean isPEnd() {
        return OomlWPhType.P_END == type;
    }

    public boolean isTrBegin() {
        return OomlWPhType.TR_BEGIN == type;
    }

    public boolean isTrEnd() {
        return OomlWPhType.TR_END == type;
    }

    public OomlWPhType getType() {
        return type;
    }

    public void setType(OomlWPhType type) {
        this.type = type;
    }

    public void setType(String typeName) {
        if (Ws.isBlank(typeName)) {
            this.type = OomlWPhType.NORMAL;
        } else {
            String tn = Ws.snakeCase(typeName).toUpperCase();
            this.type = OomlWPhType.valueOf(tn);
        }
    }

    public boolean hasBoolTest() {
        return !Ws.isBlank(this.boolTest);
    }

    public String getBoolTest() {
        return boolTest;
    }

    public void setBoolTest(String boolTest) {
        this.boolTest = boolTest;
    }

    public boolean hasDefaultValue() {
        return !Ws.isBlank(this.defaultValue);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean hasItemName() {
        return !Ws.isBlank(this.itemName);
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean hasRunProperty() {
        return null != this.runProperty;
    }

    public CheapElement getRunProperty() {
        return runProperty;
    }

    public void setRunProperty(CheapElement runProperty) {
        this.runProperty = runProperty;
    }

    public boolean hasRunBegin() {
        return null != this.runBegin;
    }

    public OomlWPhMark getRunBegin() {
        return runBegin;
    }

    public void setRunBegin(OomlWPhMark runBegin) {
        this.runBegin = runBegin;
    }

    public boolean hasRunEnd() {
        return null != this.runEnd;
    }

    public OomlWPhMark getRunEnd() {
        return runEnd;
    }

    public void setRunEnd(OomlWPhMark runEnd) {
        this.runEnd = runEnd;
    }

}
