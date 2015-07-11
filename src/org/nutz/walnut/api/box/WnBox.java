package org.nutz.walnut.api.box;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.util.Callback;

public interface WnBox {

    String id();

    WnBoxStatus status();

    void setup(WnBoxContext bc);

    // void submit(String cmdLine);

    void run(String cmdText);

    void setStdout(OutputStream ops);

    void setStderr(OutputStream ops);

    void setStdin(InputStream ins);

    void onBeforeFree(Callback<WnBoxContext> handler);

}
