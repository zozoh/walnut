package org.nutz.walnut.cheap.dom;

public class CheapRawData extends CheapNode {

    private String data;

    protected CheapRawData() {
        this(null);
    }

    protected CheapRawData(String data) {
        this.type = CheapNodeType.RAW_DATA;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String content) {
        this.data = content;
    }

}
