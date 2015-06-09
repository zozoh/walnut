package org.nutz.walnut.api.usr;

import org.nutz.lang.util.NutBean;

public interface WnUsr extends NutBean {

    String id();

    WnUsr id(String id);

    String name();

    WnUsr name(String name);

    String group();

    WnUsr group(String grp);

    String alias();

    WnUsr alias(String alias);

    String password();

    WnUsr password(String passwd);

    String email();

    WnUsr email(String email);

    String phone();

    WnUsr phone(String phone);

    String home();

    WnUsr home(String home);

    WnUsr clone();

}