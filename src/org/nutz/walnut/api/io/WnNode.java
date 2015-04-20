package org.nutz.walnut.api.io;

public interface WnNode {

    String id();

    WnNode id(String id);

    WnNode genID();

    boolean hasID();

    boolean isSameId(WnNode o);

    boolean isSameId(String id);

    String path();

    WnNode path(String path);

    String name();

    WnNode name(String nm);

    WnRace race();

    WnNode race(WnRace race);

    boolean isRace(WnRace race);

    boolean isOBJ();

    boolean isDIR();

    boolean isFILE();

    boolean isHidden();

    boolean hasParent();
    
    WnNode parent();

    void setParent(WnNode parent);

    String parentId();

    String mount();

    WnNode mount(String mnt);

    boolean isMount();

    WnTree tree();

    void setTree(WnTree tree);

    void assertTree(WnTree tree);

    boolean equals(Object obj);

    String toString();

}