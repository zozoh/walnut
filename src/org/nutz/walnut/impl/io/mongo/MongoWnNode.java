package org.nutz.walnut.impl.io.mongo;

import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.mongo.annotation.MoField;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.io.AbstractWnNode;

public class MongoWnNode extends AbstractWnNode {

    @MoField
    private String id;

    @MoField("pid")
    private String parentId;

    @MoField("nm")
    private String name;

    @MoField("race")
    private WnRace race;

    @MoField("mnt")
    private String mount;

    public MongoWnNode() {}

    public MongoWnNode(MongoWnNode nd) {
        id = nd.id;
        parentId = nd.parentId;
        name = nd.name;
        race = nd.race;
        mount = nd.mount;
        this.setTree(nd.tree());
        this.path(nd.path());
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

    public String parentId() {
        return parentId;
    }

    public MongoWnNode parentId(String pid) {
        this.parentId = pid;
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    private static final Pattern P_NM = Pattern.compile("[/\\\\]");

    @Override
    public MongoWnNode name(String nm) {
        if (Strings.isBlank(nm))
            throw Er.create("e.io.obj.nm.blank");

        if (nm.equals(".") || nm.equals("..") || P_NM.matcher(nm).find())
            throw Er.create("e.io.obj.nm.invalid", nm);

        this.name = nm;
        return this;
    }

    @Override
    public WnRace race() {
        return race;
    }

    @Override
    public WnNode race(WnRace race) {
        this.race = race;
        return this;
    }

    @Override
    public boolean isRace(WnRace race) {
        return race() == race;
    }

    @Override
    public boolean isOBJ() {
        return isRace(WnRace.OBJ);
    }

    @Override
    public boolean isDIR() {
        return isRace(WnRace.DIR);
    }

    @Override
    public boolean isFILE() {
        return isRace(WnRace.FILE);
    }

    @Override
    public boolean isHidden() {
        return name().startsWith(".");
    }

    /**
     * 一个描述挂载点的字符串。 挂载点可以是
     * 
     * <ul>
     * <li>"file:///path/to/dir" 指向一个本地目录（不能是文件）
     * <li>"persist" 表示永久存储，并记录每次修改的 sha1 指纹
     * <li>"swap" 表示为其分配一块固定的数据区域，读写，每次修改不记录历史
     * </ul>
     * 
     * @return 挂载点
     */
    @Override
    public String mount() {
        return mount;
    }

    @Override
    public WnNode mount(String mnt) {
        this.mount = mnt;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WnNode) {
            WnNode nd = (WnNode) obj;
            return nd.id().equals(id()) && nd.name().equals(name());
        }
        return false;
    }

    public WnNode duplicate() {
        return new MongoWnNode(this);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", id(), name());
    }

}
