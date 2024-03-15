package com.site0.walnut.impl.box.docker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.http.Request;
import org.nutz.http.Request.METHOD;
import org.nutz.http.Response;
import org.nutz.http.Sender;
import org.nutz.json.Json;
import org.nutz.lang.Streams;
import org.nutz.lang.random.R;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.api.box.WnBox;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.box.WnBoxService;
import com.site0.walnut.api.box.WnBoxStatus;
import com.site0.walnut.util.Wlog;

/**
 * Docker版本的WnBox, 未完成状态
 * @author wendal(wendal1985@gmail.com)
 *
 */
public class DockerBox implements WnBox {
    
    protected String id;
    protected WnBoxStatus stat;
    protected WnBoxContext bc;
    
    protected Callback<WnBoxContext> on_before_free;
    
    protected OutputStream out;
    protected OutputStream err;
    protected InputStream ins;
    protected WnBoxService boxes;
    
    protected String dockerImageName = "nutzam/wanlut-docker";
    
    private static final Log log = Wlog.getCMD();
    
    public DockerBox(WnBoxService boxes) {
        id = R.UU32();
        this.boxes = boxes;
        stat = WnBoxStatus.FREE;
    }

    public String id() {
        return id;
    }

    public WnBoxStatus status() {
        return stat;
    }

    public void setup(WnBoxContext bc) {
        this.bc = bc;
    }

    public void run(String cmdText) {
        // TODO 改成socket,这样就能双向了
    	Process p = null;
        try {
            try {
            	// TODO 怎样拿到ip呢?
    			p = Runtime.getRuntime().exec(("docker run -it --rm --privileged --name walnut_"+id+" "+dockerImageName).split(" "));
    		} catch (IOException e) {
    			//throw Wlang.wrapThrow(e);
    		}
        	NutMap params = new NutMap();
        	params.put("cmd", cmdText);
        	if (bc != null) {
        		params.put("seid", bc.session.getTicket());
        		params.put("me", bc.session.getMe());
        		params.put("env", bc.session.getVars());
        	}
        	Request req = Request.create("http://127.0.0.1:12099/walnut/call", METHOD.POST);
        	req.setData(Json.toJson(params));
        	Response resp = Sender.create(req).setTimeout(60*1000).send();
			if (resp.isOK()) {
				if (out != null)
					Streams.writeAndClose(out, resp.getStream());
			} else {
				if (err != null)
					Streams.writeAndClose(err, resp.getStream());
			}
		} catch (Exception e) {
			try {
				if (err != null)
					Streams.write(err, e.getMessage().getBytes());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
        if (p != null) {
        	
        }
    }

    public void setStdout(OutputStream ops) {
        this.out = ops;
    }

    public void setStderr(OutputStream ops) {
        this.err = ops;
    }

    public void setStdin(InputStream ins) {
        this.ins = ins;
    }

    public void onBeforeFree(Callback<WnBoxContext> handler) {
        this.on_before_free = handler;
    }

    void free() {
        // 调用回调
        if (null != this.on_before_free) {
            this.on_before_free.invoke(bc);
            this.on_before_free = null;
        }

        // 释放主运行器? what?
        //runner.__free();

        // 释放其他资源
        if (log.isDebugEnabled())
            log.debug("box: release resources");
        Streams.safeClose(out);
        Streams.safeClose(ins);
        Streams.safeClose(err);
        stat = WnBoxStatus.FREE;
    }
}