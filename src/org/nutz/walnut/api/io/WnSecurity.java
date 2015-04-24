package org.nutz.walnut.api.io;

public interface WnSecurity {

    <T extends WnNode> T enter(T nd);

    <T extends WnNode> T access(T nd);

    <T extends WnNode> T view(T nd);

    <T extends WnNode> T read(T nd);

    <T extends WnNode> T write(T nd);

}
