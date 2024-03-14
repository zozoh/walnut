package com.site0.walnut.ooml.xlsx;

import org.nutz.json.JsonField;
import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.ooml.OomlEntry;
import com.site0.walnut.ooml.OomlPackage;
import com.site0.walnut.ooml.OomlRels;
import com.site0.walnut.util.Wpath;

public class XlsxObj {

    @JsonField(ignore = true)
    protected OomlEntry entry;

    protected String parentPath;

    protected String objPath;

    @JsonField(ignore = true)
    protected OomlPackage ooml;

    @JsonField(ignore = true)
    protected CheapDocument doc;

    @JsonField(ignore = true)
    protected OomlRels rels;

    public String getObjPath() {
        return objPath;
    }

    public void setObjPath(String objPath) {
        this.objPath = objPath;
        this.parentPath = Wpath.getParent(this.objPath);
        this.entry = ooml.getEntry(this.objPath);
    }

    /**
     * 根据当前实体的路径，按照约定，获取对应的资源映射表的路径
     * 
     * @return 资源映射表路径
     */
    protected String getObjRelsPath() {
        String fnm = Wpath.getName(this.objPath);
        return Wpath.appendPath(this.parentPath, "_rels", fnm + ".rels");
    }

    protected void loadRelationships() {
        String relsPath = this.getObjRelsPath();
        OomlEntry re = ooml.getEntry(relsPath);
        // 某些电子表格，由于没有嵌入式图片等外部资源，因此没有关系表
        if (null == re) {
            this.rels = new OomlRels();
        }
        // 加载关系表
        else {
            this.rels = new OomlRels(entry.getPath(), re.getContentStr());
        }
    }

}
