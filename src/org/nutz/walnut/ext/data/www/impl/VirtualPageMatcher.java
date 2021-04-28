package org.nutz.walnut.ext.data.www.impl;

import java.util.Arrays;

import org.nutz.lang.Strings;

public class VirtualPageMatcher {

    int step; // 匹配时从哪个下标开始，1 表示从开始， -1 表示从路径结尾, 0 精确匹配
    String[] _phs;

    VirtualPageMatcher(String s) {
        String[] ss = Strings.splitIgnoreBlank(s, "/");
        // 后缀模式 "*/xx/xx"
        if (ss.length > 0 && "*".equals(ss[0])) {
            step = -1;
            _phs = Arrays.copyOfRange(ss, 1, ss.length);
        }
        // 前缀模式 "/xx/xx/*"
        else if (s.endsWith("*")) {
            step = 1;
            _phs = Arrays.copyOfRange(ss, 0, ss.length - 1);
        }
        // 精确匹配模式
        else {
            _phs = Strings.splitIgnoreBlank(s, "/");
        }
    }

    /**
     * 匹配给定路径数组
     * 
     * @param path
     *            路径（已经被拆分好的数组）
     * @return null 表示不匹配, [] 表示完全匹配, ["xx","xx"] 表示还剩下的路径部分
     */
    String[] match(String[] paths) {
        // 后缀模式 "*/xx/xx"
        if (-1 == step) {
            int len = paths.length - _phs.length;
            if (len < 0) {
                return null;
            }
            int i = _phs.length - 1;
            for (; i >= 0; i--) {
                if (!paths[i].equals(_phs[i]))
                    return null;
            }
            return Arrays.copyOfRange(paths, 0, len);
        }
        // 前缀模式 "/xx/xx/*"
        else if (1 == step) {
            int len = paths.length - _phs.length;
            if (len < 0) {
                return null;
            }
            int i = 0;
            for (; i < _phs.length; i++) {
                if (!paths[i].equals(_phs[i]))
                    return null;
            }
            return Arrays.copyOfRange(paths, _phs.length, paths.length);
        }
        // 精确匹配模式
        if (paths.length != _phs.length) {
            return null;
        }
        for (int i = 0; i < _phs.length; i++) {
            if (!paths[i].equals(_phs[i]))
                return null;
        }
        return new String[0];
    }

}
