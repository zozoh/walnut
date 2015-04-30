package org.nutz.walnut.api.box;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.util.Callback;

public interface WnBox {

    WnBoxFactory factory();

    String getId();

    WnBoxStatus getStatus();

    WnBoxRuntime getRuntime();

    void setup(WnBoxInfo boxInfo);

    void run(String cmd);

    void onClose(Callback<WnBox> callback);

    void onOutput(Callback<InputStream> callback);

    void onError(Callback<InputStream> callback);

    void onInput(Callback<OutputStream> callback);

}
