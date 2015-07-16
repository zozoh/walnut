package org.nutz.walnut.impl.box.cmd;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZType;

import com.danoo.videox.bean.VideoInfo;

/**
 * 读取视频信息
 * 
 * 需要VIDEO_CONVERT库的支持
 * 
 * @author pw
 *
 */
public class cmd_video extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        if (hasVCLibrary(sys)) {
            WnObj obj = getObj(sys, args);
            if (obj == null) {
                return;
            }
            if (ZType.isVideo(obj.type())) {
                String vpath = sys.io.getRealPath(obj);
                VideoInfo vi = readVideoInfo(vpath);
                if (vi != null) {
                    sys.out.print(Json.toJson(vi));
                } else {
                    sys.err.printf("read %s(%s) videoInfo has err", obj.name(), obj.id());
                }
            } else {
                sys.err.printf("obj %s(%s) is not a video", obj.name(), obj.id());
            }
        }
    }

    private VideoInfo readVideoInfo(String path) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"read_info.sh", path, "ALL"});
            Reader r = new InputStreamReader(p.getInputStream());
            return Json.fromJson(VideoInfo.class, r);
        }
        catch (IOException e) {}
        return null;
    }

    protected boolean hasVCLibrary(WnSystem sys) {
        if (!Wn.hasVCLibrary()) {
            sys.err.print("need VIDEO_CONVERT library!");
            return false;
        }
        return true;
    }

}
