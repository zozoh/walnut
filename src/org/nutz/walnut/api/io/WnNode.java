package org.nutz.walnut.api.io;

public interface WnNode {

    WnNode parent();

    void setParent(WnNode parent);

    String path();

    WnNode path(String path);

    WnNode genID();

    boolean hasID();

    String id();

    WnNode id(String id);

    boolean isSameId(WnNode nd);

    boolean isSameId(String id);

    String name();

    WnNode name(String nm);

    WnRace race();

    WnNode race(WnRace race);

    boolean isRace(WnRace race);

    boolean isOBJ();

    boolean isDIR();

    boolean isFILE();

    boolean isHidden();

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
    String mount();

    WnNode mount(String mnt);
    
    boolean isMount();

    /**
     * @return 节点所在的树对象
     */
    WnTree tree();

    void setTree(WnTree tree);

    void assertTree(WnTree tree);

    boolean equals(Object obj);

    String toString();

}