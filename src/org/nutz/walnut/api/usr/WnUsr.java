package org.nutz.walnut.api.usr;

public interface WnUsr {

    String id();

    WnUsr id(String id);

    String name();

    WnUsr name(String name);

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