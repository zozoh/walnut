package com.site0.walnut.ext.net.sshd.srv;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;

public class WnJdkPosixFileAttributeView implements PosixFileAttributeView {
    
    WnObj wobj;
    
    WnIo io;

    public WnJdkPosixFileAttributeView(WnObj wobj, WnIo io) {
        super();
        this.wobj = wobj;
        this.io = io;
    }

    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime)
            throws IOException {
        // TODO 设置时间
    }

    public UserPrincipal getOwner() throws IOException {
        return new WnJdkUserPrincipal(wobj.creator());
    }

    public void setOwner(UserPrincipal owner) throws IOException {
        // TODO 实现 sftp的owner设置
    }

    public String name() {
        return "posix";
    }

    public PosixFileAttributes readAttributes() throws IOException {
        return new WnJdkPosixFileAttributes(wobj);
    }

    public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
        // TODO 设置权限
    }

    public void setGroup(GroupPrincipal group) throws IOException {
        // TODO 设置主
    }

}
