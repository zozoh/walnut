package org.nutz.walnut.impl.local;

import java.io.File;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.AbstractWnNode;

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
        this.setParent(nd.parent());
        this.path(nd.path());
    }

    public File getFile() {
        return file;
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
    public boolean isMount() {
        return !Strings.isBlank(mount);
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
