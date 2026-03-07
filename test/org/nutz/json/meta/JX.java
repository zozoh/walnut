package org.nutz.json.meta;

import java.util.List;

import org.nutz.json.JsonField;
import org.nutz.lang.util.IntRange;

/**
 * 测试一下强制某字段输出 String
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class JX {

    private int n;

    @JsonField(forceString = true)
    private IntRange region;

    @JsonField(forceString = true)
    private IntRange[] regionArray;

    @JsonField(forceString = true)
    private List<IntRange> regionList;

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public IntRange getRegion() {
        return region;
    }

    public void setRegion(IntRange region) {
        this.region = region;
    }

    public IntRange[] getRegionArray() {
        return regionArray;
    }

    public void setRegionArray(IntRange[] regionArray) {
        this.regionArray = regionArray;
    }

    public List<IntRange> getRegionList() {
        return regionList;
    }

    public void setRegionList(List<IntRange> regionList) {
        this.regionList = regionList;
    }

}
