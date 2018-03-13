package org.nutz.walnut.ext.hmaker.util.com;

import java.util.List;

import org.jsoup.nodes.Element;
import org.nutz.walnut.ext.hmaker.util.bean.HmcDynamicScriptInfo;

public abstract class AbstractNoneValueCom extends AbstractSimpleCom {

    @Override
    public void loadValue(Element eleCom, String key, HmcDynamicScriptInfo hdsi) {}

    @Override
    public void joinParamList(Element eleCom, List<String> list) {}

}
