package com.site0.walnut.api;

import java.io.InputStream;
import java.io.OutputStream;

public interface WnExecutable {

    void exec(String cmdText);

    void execf(String fmt, Object... args);

    void exec(String cmdText, OutputStream stdOut, OutputStream stdErr, InputStream stdIn);

    void exec(String cmdText, StringBuilder stdOut, StringBuilder stdErr, CharSequence stdIn);

    void exec(String cmdText, CharSequence input);

    String exec2(String cmdText);

    String exec2f(String fmt, Object... args);

    String exec2(String cmdText, CharSequence input);

}
