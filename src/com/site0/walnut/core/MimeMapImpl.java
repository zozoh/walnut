package com.site0.walnut.core;

import java.util.Map;
import java.util.Set;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Strings;
import com.site0.walnut.api.io.MimeMap;

public class MimeMapImpl implements MimeMap {

    private Map<String, String> map;

    public MimeMapImpl(PropertiesProxy pp) {
        this(pp.toMap());
    }

    public MimeMapImpl(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public String getMime(String type, String dftMime) {
        return Strings.sNull(map.get(type), dftMime);
    }

    @Override
    public String getMime(String type) {
        return getMime(type, "application/octet-stream");
    }

    @Override
    public Set<String> keys() {
        return map.keySet();
    }

}