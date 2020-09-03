package org.nutz.walnut.ext.lbs.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

/**
 * 封装了一个地址的细节
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class LbsChinaAddr {

    private static final Map<String, Integer> PTYPES = new HashMap<>();

    static {
        // 直辖市
        PTYPES.put("110000", 1); // 北京市
        PTYPES.put("120000", 1); // 天津市
        PTYPES.put("310000", 1); // 上海市
        PTYPES.put("500000", 1); // 重庆市

        // 拥有自治县的自治区
        PTYPES.put("150000", 2); // 内蒙古自治区
        PTYPES.put("410000", 2); // 河南省
        PTYPES.put("420000", 2); // 湖北省
        PTYPES.put("450000", 2); // 广西壮族自治区
        PTYPES.put("460000", 2); // 海南省
        PTYPES.put("540000", 2); // 西藏自治区
        PTYPES.put("640000", 2); // 宁夏回族自治区
        PTYPES.put("650000", 2); // 新疆维吾尔自治区

        // 特别行政区
        PTYPES.put("810000", 3); // 香港特别行政区
        PTYPES.put("820000", 3); // 澳门特别行政区
    }

    /**
     * 省地址类型：
     * <ul>
     * <li>0-普通省份
     * <li>1-直辖市
     * <li>2-自治区
     * <li>3-特别行政区
     * </ul>
     */
    private int provinceType;

    /**
     * 地址级别
     * <ul>
     * <li>1-省直辖市
     * <li>2-城市
     * <li>3-区/县
     * <li>4-街道/乡镇
     * </ul>
     */
    private int level;

    /**
     * 如果是区县一级，true 表示不再有下级街道了
     */
    private boolean noTown;

    private String code;
    private String name;
    private String fullName;
    private String province;
    private String provinceCode;
    private String provinceName;
    private String city;
    private String cityCode;
    private String cityName;
    private String area;
    private String areaCode;
    private String areaName;
    private String town;

    public LbsChinaAddr() {}

    public LbsChinaAddr(String str) {
        this.fromString(str);
    }

    public LbsChinaAddr(String code, String name, boolean noTown) {
        this.code = code;
        this.name = name;
        this.noTown = noTown;
        this.__eval_info();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.code);
        sb.append('=').append(this.name);
        if (this.noTown) {
            sb.append('!');
        }
        return sb.toString();
    }

    public LbsChinaAddr clone() {
        LbsChinaAddr lca = new LbsChinaAddr();
        lca.provinceType = this.provinceType;
        lca.level = this.level;
        lca.noTown = this.noTown;
        lca.code = this.code;
        lca.name = this.name;
        lca.fullName = this.fullName;
        lca.province = this.province;
        lca.provinceCode = this.provinceCode;
        lca.provinceName = this.provinceName;
        lca.city = this.city;
        lca.cityCode = this.cityCode;
        lca.cityName = this.cityName;
        lca.area = this.area;
        lca.areaCode = this.areaCode;
        lca.areaName = this.areaName;
        lca.town = this.town;
        return lca;
    }

    /**
     * @return 当前地址的城市级是否为虚拟地址
     */
    public boolean isVirtualCity() {
        if (this.city != null) {
            return this.city.matches("^(01|02|90)$");
        }
        return false;
    }

    /**
     * 创建直辖市的虚拟地址
     * 
     * @param type
     *            虚拟地址类型
     *            <ul>
     *            <li><code>1</code> - 市辖区
     *            <li><code>2</code> - 市辖县
     *            <li><code>9</code> - 自治县
     *            </ul>
     * @return 虚拟地址对象
     */
    public LbsChinaAddr getMyVirtualAddr(int type) {
        String vcode, name;
        // 市辖区
        if (1 == type) {
            vcode = "01";
            name = "市辖区";
        }
        // 市辖县
        else if (2 == type) {
            vcode = "02";
            name = "市辖县";
        }
        // 自治县
        else if (9 == type) {
            vcode = "90";
            name = "自治县";
        } else {
            throw Lang.makeThrow("Invalid viruatal address type", type);
        }

        // 创建地址
        LbsChinaAddr lca = this.clone();
        String code = lca.getProvince() + vcode + "00";
        lca.setCode(code);
        lca.setName(name);
        lca.setCity(vcode);
        lca.setCityCode(code);
        lca.setCityName(name);
        lca.setLevel(2);
        lca.setProvinceType(0);
        return lca;
    }

    private static final Pattern _P = Pattern.compile("^(\\d+)=([^!]+)(!?)$");
    private static final Pattern _P2 = Pattern.compile("^(\\d{2})(\\d{2})(\\d{2})(\\d{6})?$");

    /**
     * 从一个字符串解析本类
     * 
     * <pre>
     * 字符串的结构为 [CODE]=[NAME][!]
     * 譬如： 110108=海淀区
     * 譬如： 110108003000=羊坊店街道
     * 譬如： 659010=胡杨河市! <- 叹号结尾表示无下级街道的城市
     * </pre>
     */
    public void fromString(String str) {
        Matcher m = _P.matcher(str);
        if (!m.find())
            throw Lang.makeThrow("Invalid input", str);

        this.code = Strings.trim(m.group(1));
        this.name = Strings.trim(m.group(2));
        this.noTown = "!".equals(m.group(3));

        __eval_info();
    }

    private void __eval_info() {
        Matcher m;
        // 首先解析一下地址码，搞清楚地址级别，以及对应的父地址编码
        m = _P2.matcher(this.code);

        if (!m.find()) {
            throw Lang.makeThrow("Invalid Addr Code", this.code);
        }

        // 提取码
        this.province = m.group(1);
        this.city = m.group(2);
        this.area = m.group(3);
        if (m.groupCount() == 4)
            this.town = m.group(4);

        // 判断级别
        if (!Strings.isBlank(this.town)) {
            this.noTown = true;
            this.level = 4;
            this.provinceCode = Strings.alignLeft(this.province, 6, '0');
            this.cityCode = Strings.alignLeft(this.province + this.city, 6, '0');
            this.areaCode = this.province + this.city + this.area;
        }
        // 区县级
        else if (!"00".equals(this.area)) {
            this.level = 3;
            this.provinceCode = Strings.alignLeft(this.province, 6, '0');
            this.cityCode = Strings.alignLeft(this.province + this.city, 6, '0');
            this.areaCode = this.province + this.city + this.area;
        }
        // 地市级
        else if (!"00".equals(this.city)) {
            this.level = 2;
            this.provinceCode = Strings.alignLeft(this.province, 6, '0');
            this.cityCode = Strings.alignLeft(this.province + this.city, 6, '0');
            this.area = null;
        }
        // 省级
        else if (!"00".equals(this.province)) {
            this.level = 1;
            this.provinceCode = this.code;
            this.city = null;
            this.area = null;
        }
        // 不可能
        else {
            throw Lang.makeThrow("Invalid Addr Code", this.code);
        }

        // 判断省类型
        Integer ptype = PTYPES.get(this.code);
        if (null != ptype) {
            this.provinceType = (int) ptype;
        } else {
            this.provinceType = 0;
        }
    }

    /**
     * 根据自身信息，从字典里完善全部的字段信息。 <br>
     * 假设已经至少填充了 code
     * 
     * @param map
     *            地址字典
     */
    public void completeInfo(Map<String, LbsChinaAddr> map) {
        // 街道级
        if (4 == this.level) {
            this.provinceName = map.get(this.provinceCode).getName();
            this.cityName = map.get(this.cityCode).getName();
            this.areaName = map.get(this.areaCode).getName();
            if (!this.isVirtualCity()) {
                this.fullName = this.provinceName + this.cityName + this.areaName + this.name;
            } else {
                this.fullName = this.provinceName + this.areaName + this.name;
            }
        }
        // 区县级
        else if (3 == this.level) {
            this.provinceName = map.get(this.provinceCode).getName();
            this.cityName = map.get(this.cityCode).getName();
            this.areaName = this.name;
            if (!this.isVirtualCity()) {
                this.fullName = this.provinceName + this.cityName + this.areaName;
            } else {
                this.fullName = this.provinceName + this.areaName;
            }
        }
        // 地市级
        else if (2 == this.level) {
            this.provinceName = map.get(this.provinceCode).getName();
            this.cityName = this.name;
            if (!this.isVirtualCity()) {
                this.fullName = this.provinceName + this.cityName;
            } else {
                this.fullName = this.provinceName;
            }
        }
        // 省级
        else if (1 == this.level) {
            this.provinceName = this.name;
            this.fullName = this.provinceName;
        }
        // 不可能
        else {
            throw Lang.impossible();
        }
    }

    public void fromStringAndCompleteInfo(String str, Map<String, LbsChinaAddr> map) {
        this.fromString(str);
        this.completeInfo(map);
    }

    /**
     * @return 是否为普通省份
     */
    public boolean isNormalProvince() {
        return 0 == this.provinceType;
    }

    /**
     * @return 是否为直辖市
     */
    public boolean isMunicipalities() {
        return 1 == this.provinceType;
    }

    /**
     * @return 是否为自治区
     */
    public boolean isAutonomousRegion() {
        return 2 == this.provinceType;
    }

    /**
     * @return 是否为特别行政区
     */
    public boolean isSpecialRegion() {
        return 3 == this.provinceType;
    }

    public int getProvinceType() {
        return provinceType;
    }

    public void setProvinceType(int provinceType) {
        this.provinceType = provinceType;
    }

    /**
     * @return 是否为省或者直辖市级别地址
     */
    public boolean isLevelProvince() {
        return 1 == this.level;
    }

    /**
     * @return 是否为城市级别地址
     */
    public boolean isLevelCity() {
        return 2 == this.level;
    }

    /**
     * @return 是否为区县级别地址
     */
    public boolean isLevelArea() {
        return 3 == this.level;
    }

    /**
     * @return 是否为街道乡县级别地址
     */
    public boolean isLevelTown() {
        return 4 == this.level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isNoTown() {
        return noTown;
    }

    public void setNoTown(boolean noTown) {
        this.noTown = noTown;
    }

    public String getCode() {
        return code;
    }

    public String[] toAreaCodeArray() {
        String[] ar = new String[3];
        ar[0] = Strings.sBlank(this.province, "00");
        ar[1] = Strings.sBlank(this.city, "00");
        ar[2] = Strings.sBlank(this.area, "00");
        return ar;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

}
