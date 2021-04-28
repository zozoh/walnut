package org.nutz.walnut.ext.net.sshd.srv;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.Collection;

import org.apache.sshd.common.util.GenericUtils;

public class WnJdkFileStore extends FileStore {
    
    protected FileSystem fs;
    
    public WnJdkFileStore(FileSystem fs) {
        this.fs = fs;
    }

    public String name() {
        return "walnut";
    }

    public String type() {
        return "remote";
    }

    public boolean isReadOnly() {
        return fs.isReadOnly();
    }

    public long getTotalSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    public long getUsableSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    public long getUnallocatedSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        Collection<String> views = fs.supportedFileAttributeViews();
        if ((type == null) || GenericUtils.isEmpty(views)) {
            return false;
        } else if (PosixFileAttributeView.class.isAssignableFrom(type)) {
            return views.contains("posix");
        } else if (AclFileAttributeView.class.isAssignableFrom(type)) {
            return views.contains("acl");   // must come before owner view
        } else if (FileOwnerAttributeView.class.isAssignableFrom(type)) {
            return views.contains("owner");
        } else if (BasicFileAttributeView.class.isAssignableFrom(type)) {
            return views.contains("basic"); // must be last
        } else {
            return false;
        }
    }

    public boolean supportsFileAttributeView(String name) {
        return fs.supportedFileAttributeViews().contains(name);
    }

    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        return null;
    }

    @Override
    public Object getAttribute(String attribute) throws IOException {
        return null;
    }

}
