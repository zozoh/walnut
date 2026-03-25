package org.nutz.http;

public class HttpException extends RuntimeException {

    public HttpException(String url, Throwable cause) {
        super("url="+url, cause);
    }

}
