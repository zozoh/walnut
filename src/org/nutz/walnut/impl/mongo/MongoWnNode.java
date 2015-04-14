package org.nutz.walnut.impl.mongo;

import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.mongo.annotation.MoField;
import org.nutz.mongo.annotation.MoIgnore;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.impl.AbstractWnNode;

public class MongoWnNode extends AbstractWnNode {

    @MoField
    private String id;

    @MoField
    private String pid;

    @MoField
    private String nm;

    @MoField
    private WnRace race;

    @MoField
    private String mnt;

    @MoIgnore
    private WnNode parent;

    @MoIgnore
    private WnTree tree;

    @MoIgnore
    private String path;

    public WnTree tree() {
        return tree;
    }

    public void setTree(WnTree tree) {
        this.tree = tree;
    }

    @Override
    public WnNode parent() {
        return parent;
    }

    @Override
    public void setParent(WnNode parent) {
        this.parent = parent;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public WnNode path(String path) {
        this.path = path;
        return this;
    }

    @Override
    public boolean hasID() {
        return !Strings.isBlank(id);
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
    public boolean isSameId(WnNode nd) {
        return isSameId(id);
    }

    @Override
    public boolean isSameId(String id) {
        if (!hasID() || null == id)
            return false;
        return this.id.equals(id);
    }

    public String parentId() {
        return pid;
    }

    public WnNode parentId(String pid) {
        this.pid = pid;
        return this;
    }

    @Override
    public String name() {
        return nm;
    }

    private static final Pattern P_NM = Pattern.compile("[/\\\\]");

    @Override
    public WnNode name(String nm) {
        if (Strings.isBlank(nm))
            throw Er.create("e.io.obj.nm.blank");

        if (nm.equals(".") || nm.equals("..") || P_NM.matcher(nm).find())
            throw Er.create("e.io.obj.nm.invalid", nm);

        this.nm = nm;
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
        return mnt;
    }

    @Override
    public WnNode mount(String mnt) {
        this.mnt = mnt;
        return this;
    }

    @Override
    public boolean isMount() {
        return !Strings.isBlank(mnt);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WnNode) {
            WnNode nd = (WnNode) obj;
            return nd.id().equals(id()) && nd.name().equals(name());
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", id(), name());
    }

}
