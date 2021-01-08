package org.nutz.walnut.alg.nfa.chars;

public abstract class CharNfaNode {

    public int minMatch;

    public int maxMatch;

    public CharNfaNode() {
        this.minMatch = 1;
        this.maxMatch = 1;
    }

}
