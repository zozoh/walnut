package com.site0.walnut.ext.net.sshd.srv;

import java.io.IOException;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;

public class WnJdkAclFileAttributeView implements AclFileAttributeView {
    
    protected WnObj wobj;
    
    protected WnIo io;

    public WnJdkAclFileAttributeView(WnObj wobj, WnIo io) {
        this.wobj = wobj;
        this.io = io;
    }

    public UserPrincipal getOwner() throws IOException {
        return new WnJdkUserPrincipal(wobj.creator());
    }

    public void setOwner(UserPrincipal owner) throws IOException {
        // TODO 设置用户
    }

    public String name() {
        return "acl";
    }

    @Override
    public List<AclEntry> getAcl() throws IOException {
        return null;
    }

    @Override
    public void setAcl(List<AclEntry> acl) throws IOException {
        // TODO 设置ACL
    }

}
