package org.nutz.walnut.impl.local;

public abstract class Locals {

    public static String key2path(String key){
        return key.substring(0, 2) + "/" + key.substring(2);
    }
    
}
