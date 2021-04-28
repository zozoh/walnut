package org.nutz.walnut.ext.net.sshd.srv;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.sshd.client.subsystem.sftp.SftpFileSystem;
import org.apache.sshd.common.file.util.ImmutableList;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.GenericUtils;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public class WnJdkFileSystemProvider extends FileSystemProvider {

    WnAuthSession se;

    WnIo io;

    protected WnJdkFileSystem fs;

    public WnJdkFileSystemProvider(Session session, WnIo io) {
        this.se = session.getAttribute(WnSshd.KEY_WN_SESSION);
        this.io = io;
        this.fs = new WnJdkFileSystem(this);
    }

    public String getScheme() {
        return "walnut";
    }

    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        return fs;
    }

    public FileSystem getFileSystem(URI uri) {
        return fs;
    }

    public Path getPath(URI uri) {
        return new WnJdkPath((WnJdkFileSystem) getFileSystem(uri),
                             uri.getPath(),
                             new ImmutableList<>(new String[]{}));
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path,
                                              Set<? extends OpenOption> options,
                                              FileAttribute<?>... attrs)
            throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter)
            throws IOException {
        String path = dir.normalize().toString();
        List<Path> list = new ArrayList<>();
        // 根目录
        for (WnObj wobj : io.query(Wn.Q.pid(io.check(null, path)))) {
            list.add(new WnJdkPath(fs, "/", new ImmutableList<>(wobj.path().split("/"))));
        }
        ;
        return new DirectoryStream<Path>() {

            public void close() throws IOException {}

            public Iterator<Path> iterator() {
                return list.iterator();
            }

        };
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete(Path path) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return new WnJdkFileStore(path.getFileSystem());
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path,
                                                                Class<V> type,
                                                                LinkOption... options) {
        if (AclFileAttributeView.class.isAssignableFrom(type)) {
            return type.cast(new WnJdkAclFileAttributeView(io.check(null,
                                                                    path.normalize().toString()),
                                                           io));
        } else if (BasicFileAttributeView.class.isAssignableFrom(type)) {
            return type.cast(new WnJdkPosixFileAttributeView(io.check(null,
                                                                      path.normalize().toString()),
                                                             io));
        }
        return null;
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path,
                                                            Class<A> type,
                                                            LinkOption... options)
            throws IOException {
        if (type.isAssignableFrom(PosixFileAttributes.class)) {
            return type.cast(getFileAttributeView(path,
                                                  PosixFileAttributeView.class,
                                                  options).readAttributes());
        }

        throw new UnsupportedOperationException("readAttributes("
                                                + path
                                                + ")["
                                                + type.getSimpleName()
                                                + "] N/A");
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
            throws IOException {
        String view;
        String attrs;
        int i = attributes.indexOf(':');
        if (i == -1) {
            view = "basic";
            attrs = attributes;
        } else {
            view = attributes.substring(0, i++);
            attrs = attributes.substring(i);
        }

        return readAttributes(path, view, attrs, options);
    }

    public Map<String, Object> readAttributes(Path path,
                                              String view,
                                              String attrs,
                                              LinkOption... options)
            throws IOException {
        Collection<String> views = fs.supportedFileAttributeViews();
        if (GenericUtils.isEmpty(views) || (!views.contains(view))) {
            throw new UnsupportedOperationException("readAttributes("
                                                    + path
                                                    + ")["
                                                    + view
                                                    + ":"
                                                    + attrs
                                                    + "] view not supported: "
                                                    + views);
        }

        if ("basic".equalsIgnoreCase(view)
            || "posix".equalsIgnoreCase(view)
            || "owner".equalsIgnoreCase(view)) {
            return readPosixViewAttributes(path, view, attrs, options);
        } else if ("acl".equalsIgnoreCase(view)) {
            return readAclViewAttributes(path, view, attrs, options);
        } else {
            return readCustomViewAttributes(path, view, attrs, options);
        }
    }

    protected Map<String, Object> readCustomViewAttributes(Path path,
                                                           String view,
                                                           String attrs,
                                                           LinkOption... options)
            throws IOException {
        throw new UnsupportedOperationException("readCustomViewAttributes("
                                                + path
                                                + ")["
                                                + view
                                                + ":"
                                                + attrs
                                                + "] view not supported");
    }

    protected Map<String, Object> readAclViewAttributes(Path path,
                                                        String view,
                                                        String attrs,
                                                        LinkOption... options)
            throws IOException {
        if ("*".equals(attrs)) {
            attrs = "acl,owner";
        }

        AclFileAttributeView attributes = getFileAttributeView(path,
                                                               AclFileAttributeView.class,
                                                               options);

        Map<String, Object> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String attr : attrs.split(",")) {
            switch (attr) {
            case "acl":
                List<AclEntry> acl = attributes.getAcl();
                if (acl != null) {
                    map.put(attr, acl);
                }
                break;
            case "owner":
                String owner = attributes.getOwner().getName();
                if (GenericUtils.length(owner) > 0) {
                    map.put(attr, new SftpFileSystem.DefaultUserPrincipal(owner));
                }
                break;
            default:
                ;// nop
            }
        }

        return map;
    }

    protected Map<String, Object> readPosixViewAttributes(Path path,
                                                          String view,
                                                          String attrs,
                                                          LinkOption... options)
            throws IOException {
        PosixFileAttributes v = readAttributes(path, PosixFileAttributes.class, options);
        if ("*".equals(attrs)) {
            attrs = "lastModifiedTime,lastAccessTime,creationTime,size,isRegularFile,isDirectory,isSymbolicLink,isOther,fileKey,owner,permissions,group";
        }

        Map<String, Object> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String attr : attrs.split(",")) {
            switch (attr) {
            case "lastModifiedTime":
                map.put(attr, v.lastModifiedTime());
                break;
            case "lastAccessTime":
                map.put(attr, v.lastAccessTime());
                break;
            case "creationTime":
                map.put(attr, v.creationTime());
                break;
            case "size":
                map.put(attr, v.size());
                break;
            case "isRegularFile":
                map.put(attr, v.isRegularFile());
                break;
            case "isDirectory":
                map.put(attr, v.isDirectory());
                break;
            case "isSymbolicLink":
                map.put(attr, v.isSymbolicLink());
                break;
            case "isOther":
                map.put(attr, v.isOther());
                break;
            case "fileKey":
                map.put(attr, v.fileKey());
                break;
            case "owner":
                map.put(attr, v.owner());
                break;
            case "permissions":
                map.put(attr, v.permissions());
                break;
            case "group":
                map.put(attr, v.group());
                break;
            default:
                ; // nop
            }
        }
        return map;
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options)
            throws IOException {
        // TODO Auto-generated method stub

    }

    // ---------------------------------------------------------

    @Override
    public FileChannel newFileChannel(Path path,
                                      Set<? extends OpenOption> options,
                                      FileAttribute<?>... attrs)
            throws IOException {
        return super.newFileChannel(path, options, attrs);
    }
}
