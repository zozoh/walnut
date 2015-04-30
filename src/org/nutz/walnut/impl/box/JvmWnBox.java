package org.nutz.walnut.impl.box;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxFactory;
import org.nutz.walnut.api.box.WnBoxInfo;
import org.nutz.walnut.api.box.WnBoxRuntime;
import org.nutz.walnut.api.box.WnBoxStatus;

public class JvmWnBox implements WnBox {

    @Override
    public WnBoxFactory factory() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public WnBoxStatus getStatus() {
        return null;
    }

    @Override
    public WnBoxRuntime getRuntime() {
        return null;
    }

    @Override
    public void setup(WnBoxInfo boxInfo) {}

    @Override
    public void run(String cmd) {}

    @Override
    public void onClose(Callback<WnBox> callback) {}

    @Override
    public void onOutput(Callback<InputStream> callback) {}

    @Override
    public void onError(Callback<InputStream> callback) {}

    @Override
    public void onInput(Callback<OutputStream> callback) {}

}
