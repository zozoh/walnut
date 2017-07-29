package org.nutz.walnut.ext.sshd.srv;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.apache.sshd.common.file.util.BasePath;
import org.apache.sshd.common.file.util.ImmutableList;

public class WnJdkPath extends BasePath<WnJdkPath, WnJdkFileSystem> {

    public WnJdkPath(WnJdkFileSystem fileSystem, String root, ImmutableList<String> names) {
        super(fileSystem, root, names);
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return this;
    }

}
