package com.site0.walnut.ext.media.edi.msg.reply.clreg;


import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

public class IcsReplyCLNTDUP extends IcsCommonReply {

    /**
     * 注册成功，会返回注册码，这里说明注册码的类型
     * <ul>
     * <li><code>ABN</code>
     * <li><code>CCI</code>
     * </ul>
     */
    private String type;

    /**
     * ABN 或者 CCID 的具体代码
     */
    private String code;

    private String name;

    private String addr;

    private String addrType;

    private String roleNames;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
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

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getAddrType() {
        return addrType;
    }

    public void setAddrType(String addrType) {
        this.addrType = addrType;
    }

    public String getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(String roleNames) {
        this.roleNames = roleNames;
    }
}
