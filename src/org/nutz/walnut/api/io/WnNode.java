package org.nutz.walnut.api.io;

import java.util.List;

public interface WnNode {

    boolean isRootNode();

    String id();

    WnNode id(String id);

    boolean hasID();

    boolean isSameId(WnNode o);

    boolean isSameId(String id);

    boolean isMyParent(WnNode p);

    String path();

    WnNode path(String path);

    WnNode appendPath(String path);

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

    WnNode loadParents(List<WnNode> list, boolean force);

    void setParent(WnNode parent);

    String parentId();

    String mount();

    WnNode mount(String mnt);

    boolean isMount(WnTree myTree);

    WnTree tree();

    void setTree(WnTree tree);

    void assertTree(WnTree tree);

    boolean equals(Object obj);

    String toString();

    WnNode clone();

    WnNode duplicate();
}