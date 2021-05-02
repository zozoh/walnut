package org.nutz.walnut.util.callback;

public interface WnStrTokenCallback {

    void invoke(WnStrToken token);
    
    char escape(char c);

}
