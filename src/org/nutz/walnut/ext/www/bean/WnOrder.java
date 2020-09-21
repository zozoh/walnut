package org.nutz.walnut.ext.www.bean;

import org.nutz.json.Json;
import org.nutz.json.JsonField;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.ext.payment.WnPay3xRe;

public class WnOrder {

    private String id;

    /**
     * 订单类型，默认为 "order"
     */
    @JsonField("tp")
    private String type;

    private WnProduct[] products;

    @JsonField("pro_c")
    private int productCount;

    @JsonField("proid0")
    private String productId0;

    @JsonField("proids")
    private String[] productIds;

    // -------------------------------
    // 发票信息
    // -------------------------------
    @JsonField("invoice_id")
    private String invoiceId;

    // -------------------------------
    // 物流信息
    // -------------------------------
    @JsonField("waybil_com")
    private String waybilCompany;

    @JsonField("waybil_nb")
    private String waybilNumber;

    // -------------------------------
    // 卖家发货地址
    // -------------------------------
    /**
     * 国家编码，默认 CN
     */
    @JsonField("addr_ship_country")
    private String addrShipCountry;
    /**
     * 12位地址编码
     */
    @JsonField("addr_ship_code")
    private String addrShipCode;
    /**
     * 详细到门牌的地址
     */
    @JsonField("addr_ship_door")
    private String addrShipDoor;

    // -------------------------------
    // 用户收货地址
    // -------------------------------
    /**
     * 国家编码，默认 CN
     */
    @JsonField("addr_user_country")
    private String addrUserCountry;
    /**
     * 12位地址编码
     */
    @JsonField("addr_user_code")
    private String addrUserCode;
    /**
     * 详细到门牌的地址
     */
    @JsonField("addr_user_door")
    private String addrUserDoor;

    /**
     * 【冗余】省/直辖市
     */
    @JsonField("addr_user_province")
    private String addrUserProvince;

    /**
     * 【冗余】城市
     */
    @JsonField("addr_user_city")
    private String addrUserCity;

    /**
     * 【冗余】区县
     */
    @JsonField("addr_user_area")
    private String addrUserArea;

    /**
     * 【冗余】乡镇/街道
     */
    @JsonField("addr_user_street")
    private String addrUserStreet;

    /**
     * 联系人姓名
     */
    @JsonField("user_name")
    private String userName;

    /**
     * 联系人手机
     */
    @JsonField("user_phone")
    private String userPhone;

    /**
     * 联系人邮箱
     */
    @JsonField("user_email")
    private String userEmail;

    // -------------------------------
    // 优惠券
    // -------------------------------
    /**
     * 优惠券列表
     */
    private WnCoupon[] coupons;

    // -------------------------------
    // 其他订单信息
    // -------------------------------

    private String title;

    private String note;

    /**
     * 买家名称（支付配置目录名）
     */
    private String seller;

    /**
     * 账户库的ID
     */
    private String accounts;

    @JsonField("buyer_id")
    private String buyerId;

    /**
     * 商品总金额
     */
    private float total;

    /**
     * 运费
     */
    private float freight;

    /**
     * 优惠金额
     */
    private float discount;

    /**
     * 订单总金额（包括运费）
     */
    private float price;

    /**
     * 优惠后金额，用来实际支付
     */
    private float fee;

    private String currency;

    @JsonField("pay_tp")
    private String payType;

    @JsonField("pay_id")
    private String payId;

    @JsonField("pay_re")
    private WnPay3xRe payReturn;

    @JsonField("or_st")
    private WnOrderStatus status;

    @JsonField("ct")
    private long createTime;

    @JsonField("lm")
    private long lastModified;

    @JsonField("expi")
    private long expireTime;

    @JsonField("wt_at")
    private long waitAt;

    @JsonField("ok_at")
    private long okAt;

    @JsonField("fa_at")
    private long failAt;

    @JsonField("sp_at")
    private long shipAt;

    @JsonField("dn_at")
    private long doneAt;

    @Override
    public WnOrder clone() {
        return this.duplicate(true);
    }

    /**
     * @param or
     *            拷贝的目标，会将自身的内容复制过去
     * @param fullCopy
     *            true 表示完整复制。false 表示复制主要内容以便以此为模板
     * @return 新的实例
     */
    public void copyTo(WnOrder or, boolean fullCopy) {
        // 产品
        if (null != this.products) {
            or.products = new WnProduct[this.products.length];
            or.productIds = new String[this.products.length];
            for (int i = 0; i < this.products.length; i++) {
                WnProduct pro = this.products[i];
                or.products[i] = pro.clone();
                or.productIds[i] = pro.getId();
                if (0 == i) {
                    or.productId0 = pro.getId();
                }
            }
            or.productCount = this.productCount;
        }
        // 优惠券
        if (null != this.coupons) {
            or.coupons = new WnCoupon[this.coupons.length];
            for (int i = 0; i < this.coupons.length; i++) {
                WnCoupon cou = this.coupons[i];
                or.coupons[i] = cou.clone();
            }
        }
        // 其他关键字段
        or.title = this.title;
        or.note = this.note;

        // 物流
        or.waybilCompany = this.waybilCompany;
        or.waybilNumber = this.waybilNumber;

        // 收货
        or.addrUserCountry = this.addrUserCountry;
        or.addrUserCode = this.addrUserCode;
        or.addrUserDoor = this.addrUserDoor;
        or.addrUserProvince = this.addrUserProvince;
        or.addrUserCity = this.addrUserCity;
        or.addrUserArea = this.addrUserArea;
        or.addrUserStreet = this.addrUserStreet;
        or.userName = this.userName;
        or.userEmail = this.userEmail;
        or.userPhone = this.userPhone;

        // 发货地址
        or.addrShipCountry = this.addrShipCountry;
        or.addrShipCode = this.addrShipCode;
        or.addrShipDoor = this.addrShipDoor;

        // 支付信息
        or.seller = this.seller;
        or.buyerId = this.buyerId;
        or.total = this.total;
        or.discount = this.discount;
        or.freight = this.freight;
        or.price = this.price;
        or.fee = this.fee;
        or.currency = this.currency;
        or.payType = this.payType;

        // 其他字段
        if (fullCopy) {
            or.id = this.id;
            or.type = this.type;
            or.status = this.status;
            or.createTime = this.createTime;
            or.lastModified = this.lastModified;
            or.expireTime = this.expireTime;
            or.payId = this.payId;
            or.waitAt = this.waitAt;
            or.okAt = this.okAt;
            or.failAt = this.failAt;
            or.shipAt = this.shipAt;
            or.doneAt = this.doneAt;

            if (this.payReturn != null)
                or.payReturn = this.payReturn.clone();
        }
    }

    public void updatePrice(OrderPrice price) {
        this.total = price.total;
        this.discount = price.discount;
        this.freight = price.freight;
        this.price = price.price;
        this.fee = price.fee;
        this.currency = price.currency;
    }

    /**
     * @param fullCopy
     *            true 表示完整复制。false 表示复制主要内容以便以此为模板
     * @return 新的实例
     */
    public WnOrder duplicate(boolean fullCopy) {
        WnOrder or = new WnOrder();
        this.copyTo(or, fullCopy);
        return or;
    }

    public void updateBy(NutBean bean) {
        String json = Json.toJson(bean);
        WnOrder or = Json.fromJson(WnOrder.class, json);
        or.copyTo(this, true);
    }

    public void setTo(NutBean bean, String actived, String locked) {
        NutMap meta = this.toMeta(actived, locked);
        bean.putAll(meta);
    }

    public NutMap toMeta() {
        return this.toMeta(null, null);
    }

    public NutMap toMeta(String actived, String locked) {
        String json = Json.toJson(this);
        NutMap map = Json.fromJson(NutMap.class, json);
        if (!Strings.isBlank(actived)) {
            map = map.pickBy(actived);
        }
        if (!Strings.isBlank(locked)) {
            map = map.pickBy(Regex.getPattern(locked), true);
        }
        // 默认的不输出字段
        map.pickAndRemoveBy("^(c|m|g|d0|d1|md|ph|pid|sha1|len)$");

        // 强制一下 mime
        map.put("mime", "text/plain");

        return map;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return 是否为普通订单
     *         <p>
     *         所谓普通订单，即，值为"A": 会执行下单->支付->发货->完成这一标准步骤
     */
    public boolean isTypeA() {
        return "A".equals(this.type);
    }

    /**
     * @return 是否为简单订单
     *         <p>
     *         所谓普通订单，即，值为"Q": 适用于虚拟物品，支付成功就直接完成
     */
    public boolean isTypeQ() {
        return "Q".equals(this.type);
    }

    public boolean hasProducts() {
        return null != products && products.length > 0;
    }

    public int getProductsCount() {
        if (null == products)
            return 0;
        return products.length;
    }

    public WnProduct[] getProducts() {
        return products;
    }

    public void setProducts(WnProduct[] products) {
        this.products = products;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public String getProductId0() {
        return productId0;
    }

    public void setProductId0(String productId0) {
        this.productId0 = productId0;
    }

    public String[] getProductIds() {
        return productIds;
    }

    public void setProductIds(String[] productIds) {
        this.productIds = productIds;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public boolean hasWaybilCompany() {
        return !Strings.isBlank(waybilCompany);
    }

    public String getWaybilCompany() {
        return waybilCompany;
    }

    public void setWaybilCompany(String waybilCompany) {
        this.waybilCompany = waybilCompany;
    }

    public boolean hasWaybilNumber() {
        return !Strings.isBlank(waybilNumber);
    }

    public String getWaybilNumber() {
        return waybilNumber;
    }

    public void setWaybilNumber(String waybilNumber) {
        this.waybilNumber = waybilNumber;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean hasAddrShip(boolean includeDoor) {
        if (Strings.isBlank(addrShipCode))
            return false;
        if (includeDoor && Strings.isBlank(addrShipDoor))
            return false;
        return true;
    }

    public String getAddrShipCountry() {
        return Strings.sBlank(addrShipCountry, "CN");
    }

    public void setAddrShipCountry(String addrShipCountry) {
        this.addrShipCountry = addrShipCountry;
    }

    public String getAddrShipCode() {
        return addrShipCode;
    }

    public void setAddrShipCode(String addrShipCode) {
        this.addrShipCode = addrShipCode;
    }

    public String getAddrShipDoor() {
        return addrShipDoor;
    }

    public void setAddrShipDoor(String addrShipDoor) {
        this.addrShipDoor = addrShipDoor;
    }

    public boolean hasAddrUser(boolean includeDoor) {
        if (Strings.isBlank(addrUserCode))
            return false;
        if (includeDoor && Strings.isBlank(addrUserDoor))
            return false;
        return true;
    }

    public String getAddrUserCountry() {
        return Strings.sBlank(addrUserCountry, "CN");
    }

    public void setAddrUserCountry(String addrUserCountry) {
        this.addrUserCountry = addrUserCountry;
    }

    public String getAddrUserCode() {
        return addrUserCode;
    }

    public void setAddrUserCode(String addrUserCode) {
        this.addrUserCode = addrUserCode;
    }

    public String getAddrUserDoor() {
        return addrUserDoor;
    }

    public void setAddrUserDoor(String addrUserDoor) {
        this.addrUserDoor = addrUserDoor;
    }

    public String getAddrUserProvince() {
        return addrUserProvince;
    }

    public void setAddrUserProvince(String addrUserProvince) {
        this.addrUserProvince = addrUserProvince;
    }

    public String getAddrUserCity() {
        return addrUserCity;
    }

    public void setAddrUserCity(String addrUserCity) {
        this.addrUserCity = addrUserCity;
    }

    public String getAddrUserArea() {
        return addrUserArea;
    }

    public void setAddrUserArea(String addrUserArea) {
        this.addrUserArea = addrUserArea;
    }

    public String getAddrUserStreet() {
        return addrUserStreet;
    }

    public void setAddrUserStreet(String addrUserStreet) {
        this.addrUserStreet = addrUserStreet;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean hasCoupons() {
        return null != coupons && coupons.length > 0;
    }

    public int getCouponsCount() {
        if (null == coupons)
            return 0;
        return coupons.length;
    }

    public WnCoupon[] getCoupons() {
        return coupons;
    }

    public void setCoupons(WnCoupon[] coupons) {
        this.coupons = coupons;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getAccounts() {
        return accounts;
    }

    public void setAccounts(String accounts) {
        this.accounts = accounts;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getFreight() {
        return freight;
    }

    public void setFreight(float freight) {
        this.freight = freight;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getFee() {
        return fee;
    }

    public void setFee(float fee) {
        this.fee = fee;
    }

    public boolean hasCurrency() {
        return !Strings.isBlank(this.currency);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDefaultCurrency(String currency) {
        if (Strings.isBlank(this.currency)) {
            this.currency = currency;
        }
    }

    public String getPayTypePrefix() {
        if (null != payType) {
            int pos = payType.indexOf('.');
            if (pos >= 0) {
                return payType.substring(0, pos);
            }
            return payType;
        }
        return null;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public boolean hasPayId() {
        return !Strings.isBlank(payId);
    }

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId;
    }

    public boolean hasPayReturn() {
        return null != payReturn;
    }

    public WnPay3xRe getPayReturn() {
        return payReturn;
    }

    public void setPayReturn(WnPay3xRe payReturn) {
        this.payReturn = payReturn;
    }

    public WnOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WnOrderStatus status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public long getWaitAt() {
        return waitAt;
    }

    public void setWaitAt(long waitAt) {
        this.waitAt = waitAt;
    }

    public long getOkAt() {
        return okAt;
    }

    public void setOkAt(long okAt) {
        this.okAt = okAt;
    }

    public long getFailAt() {
        return failAt;
    }

    public void setFailAt(long failAt) {
        this.failAt = failAt;
    }

    public long getShipAt() {
        return shipAt;
    }

    public void setShipAt(long shipAt) {
        this.shipAt = shipAt;
    }

    public long getDoneAt() {
        return doneAt;
    }

    public void setDoneAt(long doneAt) {
        this.doneAt = doneAt;
    }

}
