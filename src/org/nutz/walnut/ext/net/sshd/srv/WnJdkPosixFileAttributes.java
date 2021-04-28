package org.nutz.walnut.ext.net.sshd.srv;

import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public class WnJdkPosixFileAttributes implements PosixFileAttributes {
    
    protected WnObj wobj;

    public WnJdkPosixFileAttributes(WnObj wobj) {
        this.wobj = wobj;
    }

    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(wobj.lastModified(), TimeUnit.MILLISECONDS);
    }

    @Override
    public FileTime lastAccessTime() {
        return FileTime.from(wobj.lastModified(), TimeUnit.MILLISECONDS);
    }

    @Override
    public FileTime creationTime() {
        return FileTime.from(wobj.createTime(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isRegularFile() {
        return wobj.isFILE();
    }

    @Override
    public boolean isDirectory() {
        return wobj.isDIR();
    }

    @Override
    public boolean isSymbolicLink() {
        return wobj.isLink();
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public long size() {
        return wobj.len();
    }

    @Override
    public Object fileKey() {
        return wobj.id();
    }

    @Override
    public UserPrincipal owner() {
        return new WnJdkUserPrincipal(wobj.creator());
    }

    @Override
    public GroupPrincipal group() {
        return new WnJdkGroupPrincipal(wobj.group());
    }

    @Override
    public Set<PosixFilePermission> permissions() {
        Set<PosixFilePermission> permissions = new HashSet<>();
        int md = wobj.mode();
        for (int i = 2; i >= 0; i--) {
            int m = md >> (i * 3) & Wn.Io.RWX;
            if ((m & Wn.Io.R) > 0) {
                if (i == 2)
                    permissions.add(PosixFilePermission.OWNER_READ);
                else if (i == 1)
                    permissions.add(PosixFilePermission.GROUP_READ);
                else
                    permissions.add(PosixFilePermission.OTHERS_READ);
                    
            }
            if ((m & Wn.Io.W) > 0) {
                if (i == 2)
                    permissions.add(PosixFilePermission.OWNER_WRITE);
                else if (i == 1)
                    permissions.add(PosixFilePermission.GROUP_WRITE);
                else
                    permissions.add(PosixFilePermission.OTHERS_WRITE);
            }
            if ((m & Wn.Io.X) > 0) {
                if (i == 2)
                    permissions.add(PosixFilePermission.OWNER_EXECUTE);
                else if (i == 1)
                    permissions.add(PosixFilePermission.GROUP_EXECUTE);
                else
                    permissions.add(PosixFilePermission.OTHERS_EXECUTE);
            }
        }
        return permissions;
    }

}
