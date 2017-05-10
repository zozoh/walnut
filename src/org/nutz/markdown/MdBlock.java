package org.nutz.markdown;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Strings;

class MdBlock {

    String type;

    int level;

    String codeType;

    List<String> content;

    String[] cellAligns;

    MdBlock() {
        this.level = 0;
        this.content = new LinkedList<>();
    }

    boolean isType(String regex) {
        return null != type && type.matches(regex);
    }

    boolean hasType() {
        return !Strings.isEmpty(type);
    }

    boolean hasCodeType() {
        return !Strings.isBlank(codeType);
    }

    void setContent(String[] list) {
        content.clear();
        for (String s : list)
            content.add(s);
    }
}
