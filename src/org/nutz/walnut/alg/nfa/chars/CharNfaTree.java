package org.nutz.walnut.alg.nfa.chars;

import org.nutz.walnut.alg.ds.buf.WnCharArray;

public class CharNfaTree {

    CharNfaNode root;

    public CharNfaTree() {}

    public CharNfaTree(String input) {
        this(0, input.toCharArray());
    }

    public CharNfaTree(char[] input) {
        this(0, input, 0, input.length);
    }

    public CharNfaTree(char[] input, int offset, int len) {
        this.parse(0, input, offset, len);
    }

    public CharNfaTree(int startLineNumber, String input) {
        this(startLineNumber, input.toCharArray());
    }

    public CharNfaTree(int startLineNumber, char[] input) {
        this(startLineNumber, input, 0, input.length);
    }

    public CharNfaTree(int startLineNumber, char[] input, int offset, int len) {
        this.parse(startLineNumber, input, offset, len);
    }

    public void parse(char[] input, int offset, int len) {
        parse(0, input, offset, len);
    }

    public void parse(int startLineNumber, char[] input, int offset, int len) {
        CharNfaTreeParsing ing = new CharNfaTreeParsing();
        ing.input = new WnCharArray(input, offset, len);
        ing.lineNumber = startLineNumber;
    }

}
