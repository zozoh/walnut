package com.site0.walnut.ext.net.whoisx;

import java.util.Date;

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

    /**
     * 联系人
     */
    private String registrant;

    /**
     * 注册商
     */
    private String registrar;

    /**
     * DNS服务器
     */
    private String[] dnsServers;

    /**
     * 域名状态
     */
    private String domainStatus;

    /**
     * 备案号
     */
    private String icpNo;

    private Date creationDate;

    private Date expirationDate;

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

    public String getRegistrar() {
        return registrar;
    }

    public void setRegistrar(String registrar) {
        this.registrar = registrar;
    }

    public String[] getDnsServers() {
        return dnsServers;
    }

    public void setDnsServers(String[] dnsServers) {
        this.dnsServers = dnsServers;
    }

    public String getDomainStatus() {
        return domainStatus;
    }

    public void setDomainStatus(String domainStatus) {
        this.domainStatus = domainStatus;
    }

    public String getIcpNo() {
        return icpNo;
    }

    public void setIcpNo(String icpNo) {
        this.icpNo = icpNo;
    }

    public String getRegistrant() {
        return registrant;
    }

    public void setRegistrant(String registrant) {
        this.registrant = registrant;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

}
