package com.site0.walnut.util.bean;

public class WnSummaryData {

    private byte[] data;

    private String sha1;

    public WnSummaryData() {}

    public WnSummaryData(byte[] data, String sha1) {
        this.data = data;
        this.sha1 = sha1;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

}
