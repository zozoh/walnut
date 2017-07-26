package org.nutz.walnut.ext.sshd;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashSet;
import java.util.Set;

import org.apache.sshd.common.file.util.BaseFileSystem;
import org.apache.sshd.common.file.util.ImmutableList;
import org.nutz.json.Json;
import org.nutz.lang.Strings;

public class WnJdkFileSystem extends BaseFileSystem<WnJdkPath> {

    protected Set<String> supportedViews = new HashSet<>();

    public WnJdkFileSystem(FileSystemProvider fileSystemProvider) {
        super(fileSystemProvider);
        supportedViews.add("basic");
    }

    protected WnJdkPath create(String root, ImmutableList<String> names) {
        try {
            return (WnJdkPath) provider().getPath(new URI(root
                                                          + Strings.join("/", names.toArray())));
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("bad path root=" + root + ",names=" + Json.toJson(names));
        }
    }

    public void close() throws IOException {}

    public boolean isOpen() {
        return true;
    }

    public Set<String> supportedFileAttributeViews() {
        return supportedViews;
    }

    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return null;
    }

}
