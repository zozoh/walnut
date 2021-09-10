package org.nutz.walnut.ooml;

import org.nutz.walnut.util.Wpath;
import org.nutz.walnut.util.Ws;

public class OomlRel {

    public static final String DRAWING = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing";
    public static final String IMAGE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";
    public static final String SHARED_STRINGS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings";
    public static final String STYLES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles";
    public static final String WORKSHEET = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet";
    public static final String WEB_SETTINGS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/webSettings";
    public static final String THEME = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme";
    public static final String FONT_TABLE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable";
    public static final String RECOVERED = "http://schemas.microsoft.com/office/2006/relationships/recovered";

    private String id;

    private String target;

    private String type;

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

    public boolean isType(String type) {
        if (null == type) {
            return null == this.type;
        }
        return type.equals(this.type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
