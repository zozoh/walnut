package com.site0.walnut.ext.media.edi.msg.reply.atd;

import java.util.List;

public class AtdResTransLine {

    // DOC trigger value
    private String trigVal;

    // DOC line number (transport line)
    private int lineNum;

    private List<Pack> pacs;
    private List<Ref> refs;
    private List<Txt> ftxs;
    private List<Item> items;

    public String getTrigVal() {
        return trigVal;
    }

    public void setTrigVal(String trigVal) {
        this.trigVal = trigVal;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public List<Pack> getPacs() {
        return pacs;
    }

    public void setPacs(List<Pack> pacs) {
        this.pacs = pacs;
    }

    public List<Ref> getRefs() {
        return refs;
    }

    public void setRefs(List<Ref> refs) {
        this.refs = refs;
    }

    public List<Txt> getFtxs() {
        return ftxs;
    }

    public void setFtxs(List<Txt> ftxs) {
        this.ftxs = ftxs;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class Pack {
        private String num;
        private String code;

        // Import cargo type, e.g. FCL/LCL
        private String cargoTp;

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getCargoTp() {
            return cargoTp;
        }

        public void setCargoTp(String cargoTp) {
            this.cargoTp = cargoTp;
        }
    }

    public static class Ref {
        private String code;
        private String val;
        private String ver;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public String getVer() {
            return ver;
        }

        public void setVer(String ver) {
            this.ver = ver;
        }
    }

    public static class Txt {
        private String subj;
        private String code;
        private String txt;

        public String getSubj() {
            return subj;
        }

        public void setSubj(String subj) {
            this.subj = subj;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }
    }

    public static class Item {
        private int lineNum;
        private String goodsDesc;
        private List<Tax> taxes;

        public int getLineNum() {
            return lineNum;
        }

        public void setLineNum(int lineNum) {
            this.lineNum = lineNum;
        }

        public String getGoodsDesc() {
            return goodsDesc;
        }

        public void setGoodsDesc(String goodsDesc) {
            this.goodsDesc = goodsDesc;
        }

        public List<Tax> getTaxes() {
            return taxes;
        }

        public void setTaxes(List<Tax> taxes) {
            this.taxes = taxes;
        }
    }

    public static class Tax {
        private String tp;

        public String getTp() {
            return tp;
        }

        public void setTp(String tp) {
            this.tp = tp;
        }
    }
}

