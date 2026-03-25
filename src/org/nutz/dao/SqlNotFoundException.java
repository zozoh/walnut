package org.nutz.dao;

public class SqlNotFoundException extends RuntimeException {

    public SqlNotFoundException(String key) {
        super(String.format("fail to find SQL '%s'!", key));
    }

}
