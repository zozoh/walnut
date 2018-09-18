package org.nutz.walnut.api.usr;

import java.util.List;

import org.nutz.walnut.api.io.WnObj;

public interface WnUsr extends WnObj {

    boolean isNameSameAsId();

    String mainGroup();

    WnUsr mainGroup(String grp);

    List<String> myGroups();

    WnUsr myGroups(List<String> groups);

    String alias();

    WnUsr alias(String alias);

    String password();

    WnUsr password(String passwd);

    String salt();

    WnUsr salt(String salt);

    String email();

    WnUsr email(String email);

    String phone();

    WnUsr phone(String phone);

    String home();

    WnUsr home(String home);

    String defaultObjPath();

    WnUsr defaultObjPath(String dftObjPath);
    
    boolean hasDefaultObjPath();

    WnUsr clone();

}