package org.nutz.walnut.impl.io.local;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.io.AbstractWnNode;

public class LocalWnNode extends AbstractWnNode {

    private String id;

    private File file;

    private String mount;

    public LocalWnNode(File f) {
        this.file = f;
    }

    public LocalWnNode(LocalWnNode nd) {
        this.id = nd.id;
        this.file = nd.file;
        this.mount = nd.mount;
        this.setTree(nd.tree());
        this.path(nd.path());
    }

    public File getFile() {
        return file;
    }

    public WnNode parent() {
        if (null == parent) {
            File pf = file.getParentFile();
            if (Files.getFile(pf, ".wn").exists()) {
                parent = tree().getTreeNode();
            } else {
                LocalWnTree tree = (LocalWnTree) tree();
                parent = tree._file_to_node(pf, null, false);
            }
        }
        return parent;
    }

    @Override
    public String parentId() {
        return parent().id();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public WnNode id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public WnNode name(String nm) {
        return this;
    }

    @Override
    public long len() {
        return file.length();
    }

    @Override
    public WnRace race() {
        if (file.isDirectory())
            return WnRace.DIR;
        if (file.isFile())
            return WnRace.FILE;
        throw Lang.impossible();
    }

    @Override
    public WnNode race(WnRace race) {
        return this;
    }

    @Override
    public boolean isRace(WnRace race) {
        return race() == race;
    }

    @Override
    public boolean isOBJ() {
        return false;
    }

    @Override
    public boolean isDIR() {
        return file.isDirectory();
    }

    @Override
    public boolean isFILE() {
        return file.isFile();
    }

    @Override
    public boolean isHidden() {
        return file.isHidden();
    }

    @Override
    public String mount() {
        return mount;
    }

    @Override
    public WnNode mount(String mnt) {
        this.mount = mnt;
        return this;
    }

    public WnNode duplicate() {
        return new LocalWnNode(this);
    }

}
