package org.nutz.walnut.ext.media.edi.bean.segment;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.media.edi.bean.EdiSegment;
import org.nutz.walnut.util.Wlang;

/**
 * <code>UNH</code>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ICS_UNH {

    private String refNumber;

    private String typeId;

    private String typeVersion;

    private String typeReleaseNumber;

    private String controlingAgency;

    public ICS_UNH() {}

    public ICS_UNH(EdiSegment seg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null,
                                    "refNumber",
                                    "typeId,typeVersion,typeReleaseNumber,controlingAgency");
        seg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public ICS_UNH valueOf(NutBean bean) {
        this.refNumber = bean.getString("refNumber");
        this.typeId = bean.getString("typeId");
        this.typeVersion = bean.getString("typeVersion");
        this.typeReleaseNumber = bean.getString("typeReleaseNumber");
        this.controlingAgency = bean.getString("controlingAgency");

        return this;
    }

    public String getRefNumber() {
        return refNumber;
    }

    public void setRefNumber(String refNumber) {
        this.refNumber = refNumber;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeVersion() {
        return typeVersion;
    }

    public void setTypeVersion(String typeVersion) {
        this.typeVersion = typeVersion;
    }

    public String getTypeReleaseNumber() {
        return typeReleaseNumber;
    }

    public void setTypeReleaseNumber(String typeReleaseNumber) {
        this.typeReleaseNumber = typeReleaseNumber;
    }

    public String getControlingAgency() {
        return controlingAgency;
    }

    public void setControlingAgency(String controlingAgency) {
        this.controlingAgency = controlingAgency;
    }

}
