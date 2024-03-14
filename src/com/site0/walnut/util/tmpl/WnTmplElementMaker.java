package com.site0.walnut.util.tmpl;

import com.site0.walnut.util.tmpl.ele.TmplEle;

public interface WnTmplElementMaker {

    /**
     * 根据指定根据传入的占位符内容，创建特殊的。模板元素。
     * @param str 占位符的内容。
     * @return 自定义模板元素。 <code>null</code>表示不支持。
     * 符号解析器将会采用默认规则去生成这个欧美元素。
     */
    TmplEle make(String str);
    
}
