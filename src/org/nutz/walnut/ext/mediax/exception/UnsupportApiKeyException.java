package org.nutz.walnut.ext.mediax.exception;

public class UnsupportApiKeyException extends RuntimeException {

    public UnsupportApiKeyException(String apiKey) {
        super(apiKey);
    }

}
