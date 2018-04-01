package org.nutz.walnut.ext.whoisx;

/**
 * 记录了一个域名的摘要信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WhoInfo {

    /**
     * 注册域名
     */
    private String host;

    /**
     * 可联系的固定电话
     */
    private String phone;

    /**
     * 可联系的移动电话
     */
    private String mobile;

    /**
     * 可联系的 Email
     */
    private String email;

    /**
     * 可联系的地址
     */
    private String address;

    /**
     * 组织名
     */
    private String org;

    private String country;

    private String city;

    private String street;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

}
