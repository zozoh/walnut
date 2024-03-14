package com.site0.walnut.cheap.api;

import com.site0.walnut.cheap.dom.bean.CheapResource;

public interface CheapResourceLoader {

    CheapResource loadByPath(String path);

    CheapResource loadById(String id);

    String getMime(String typeName);

}
