package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.ext.sys.redis.RedisContext;

import redis.clients.jedis.Jedis;

public abstract class RedisHanlder {

    private String action;

    private String key;

    private int handleIndex;

    public RedisHanlder(String action, String key) {
        this.action = action;
        this.key = key;
    }

    private final char[] DFT_RE_KEY = "IKA".toCharArray();

    public String toString() {
        return this.toResultKey(DFT_RE_KEY, ":");
    }

    public String toResultKey(char[] resultKey, String sep) {
        StringBuilder sb = new StringBuilder();
        int N = resultKey.length;

        // 搞第一个字符
        char c = resultKey[0];
        if ('I' == c) {
            sb.append(Integer.toString(handleIndex));
        } else if ('A' == c) {
            sb.append(action);
        } else if ('K' == c) {
            if (null != key && key.length() > 0) {
                sb.append(key);
            }
        }

        // 剩下的字符
        for (int i = 1; i < N; i++) {

            c = resultKey[i];
            String s = null;
            if ('I' == c) {
                s = Integer.toString(handleIndex);
            } else if ('A' == c) {
                s = action;
            } else if ('K' == c) {
                s = key;
            }
            if (null != s && s.length() > 0) {
                sb.append(sep).append(s);
            }
        }

        return sb.toString();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getHandleIndex() {
        return handleIndex;
    }

    public void setHandleIndex(int handleIndex) {
        this.handleIndex = handleIndex;
    }

    public abstract Object run(RedisContext fc, Jedis jed);

}
