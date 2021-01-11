package org.nutz.walnut.cheap.dom;

public class CheapRawData extends CheapNode {

    private String data;

    public CheapRawData() {
        this(null);
    }

    public CheapRawData(String data) {
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
