package org.nutz.walnut.alg.nfa.chars;

import org.nutz.walnut.alg.ds.buf.WnLinkedArrayList;

public abstract class CharNfaComboNode extends CharNfaNode {

    public WnLinkedArrayList<CharNfaNode> children;

    public CharNfaComboNode() {
        super();
        this.children = new WnLinkedArrayList<>(CharNfaNode.class, 10);
    }

}
