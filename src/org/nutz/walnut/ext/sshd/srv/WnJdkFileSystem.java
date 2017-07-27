package org.nutz.walnut.ext.sshd.srv;

import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashSet;
import java.util.Set;

import org.apache.sshd.common.file.util.BaseFileSystem;
import org.apache.sshd.common.file.util.ImmutableList;

public class WnJdkFileSystem extends BaseFileSystem<WnJdkPath> {

    protected Set<String> supportedViews = new HashSet<>();

    public WnJdkFileSystem(FileSystemProvider fileSystemProvider) {
        super(fileSystemProvider);
        supportedViews.add("basic");
        supportedViews.add("posix");
        supportedViews.add("owner");
    }

    protected WnJdkPath create(String root, ImmutableList<String> names) {
        if (root == null && names.size() == 1 && names.get(0).equals("."))
            new WnJdkPath((WnJdkFileSystem) provider().getFileSystem(null),
                          "/",
                          new ImmutableList<>(new String[]{}));
        return new WnJdkPath((WnJdkFileSystem) provider().getFileSystem(null), root, names);
    }

    public void close() throws IOException {}

    public boolean isOpen() {
        return true;
    }

    public Set<String> supportedFileAttributeViews() {
        return supportedViews;
    }

    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return new UserPrincipalLookupService() {
            public GroupPrincipal lookupPrincipalByGroupName(String group) throws IOException {
                return new WnJdkGroupPrincipal(group);
            }
            public UserPrincipal lookupPrincipalByName(String name) throws IOException {
                return new WnJdkUserPrincipal(name);
            }
        };
    }

}
