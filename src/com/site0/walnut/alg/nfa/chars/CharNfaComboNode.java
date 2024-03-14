package com.site0.walnut.alg.nfa.chars;

import com.site0.walnut.alg.ds.buf.WnLinkedArrayList;

public abstract class CharNfaComboNode extends CharNfaNode {

    public WnLinkedArrayList<CharNfaNode> children;

    public CharNfaComboNode() {
        super();
        this.children = new WnLinkedArrayList<>(CharNfaNode.class, 10);
    }

}
