package com.site0.walnut.ext.media.edi.msg.reply.ubm;

import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import java.util.List;

/**
 * Underbond Movement 结果项
 * 对应 Segment Group 6: DOC-PAC-RFF-PCI-FTX
 */
public class UbmLineRst {

    private String cntrNum;

    private String mbl;

    private String hbl;

    private String pkgNum;

    private String cargoTp;

    private String pkgTp;

    private String marks;

    private String goodsDesc;


    public UbmLineRst() {
    }

    public UbmLineRst(List<EdiSegment> segs) {
        NutMap rff = new NutMap();
        for (EdiSegment seg : segs) {
            if (seg.is("PAC")) {
                // PAC+NUMBER OF PKGS'
                // PAC+++FCL:67:95'
                // PAC+++BX:185:95'
                rff.clear();
                seg.fillBean(rff, null, "pkgNum", null, "code,codeType,agencyCode");
                if (rff.is("codeType", "67")) {
                    this.cargoTp = rff.getString("code");
                } else if (rff.is("codeType", "185")) {
                    this.pkgTp = rff.getString("code");
                } else if (Strings.isBlank(rff.getString("codeType"))) {
                    this.pkgNum = rff.getString("pkgNum");
                }
            } else if (seg.isTag("RFF")) {
                // RFF+AAQ:{ContainerNumber}'
                // RFF+BH:{HouseBillOfLading}'
                // RFF+HWB:{HawNumber}'
                // RFF+MB:{OceanBillLading}'
                // RFF+MWB:{MasterAirWaybillNumber}'
                // RFF+UCN:{ConsignRefNumber}'
                rff.clear();
                seg.fillBean(rff, null, "refCode,refVal");
                String refCode = rff.getString("refCode");
                String refVal = rff.getString("refVal");
                if (rff.is("refCode", "AAQ")) {
                    this.cntrNum = refVal;
                } else if (rff.is("refCode", "BH") || rff.is("refCode", "HWB")) {
                    this.hbl = refVal;
                } else if (rff.is("refCode", "MB") || rff.is("refCode", "MWB")) {
                    this.mbl = refVal;
                }
            } else if (seg.isTag("PCI")) {
                rff.clear();
                seg.fillBean(rff, null, "code", "markNum");
                List<String> markNum = rff.getList("markNum", String.class);
                if (rff.is("code", "28") && markNum != null && markNum.size() > 0) {
                    this.marks = String.join(" ", markNum);
                }
            } else if (seg.isTag("FTX")) {
                rff.clear();
                seg.fillBean(rff, null, "subjectCode", "descTxt");
                if (rff.is("subjectCode", "AAA")) {
                    List<String> descList = rff.getList("descTxt", String.class);
                    if (descList != null && descList.size() > 0) {
                        this.goodsDesc = String.join(" ", descList);
                    }
                }
            }
        }
    }

    public String getCntrNum() {
        return cntrNum;
    }

    public void setCntrNum(String cntrNum) {
        this.cntrNum = cntrNum;
    }

    public String getMbl() {
        return mbl;
    }

    public void setMbl(String mbl) {
        this.mbl = mbl;
    }

    public String getHbl() {
        return hbl;
    }

    public void setHbl(String hbl) {
        this.hbl = hbl;
    }

    public String getPkgNum() {
        return pkgNum;
    }

    public void setPkgNum(String pkgNum) {
        this.pkgNum = pkgNum;
    }

    public String getCargoTp() {
        return cargoTp;
    }

    public void setCargoTp(String cargoTp) {
        this.cargoTp = cargoTp;
    }

    public String getPkgTp() {
        return pkgTp;
    }

    public void setPkgTp(String pkgTp) {
        this.pkgTp = pkgTp;
    }

    public String getMarks() {
        return marks;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }

    public String getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(String goodsDesc) {
        this.goodsDesc = goodsDesc;
    }
}
