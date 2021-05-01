package org.nutz.walnut.ext.media.ffmpeg;

import java.io.File;
import java.io.IOException;

import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZType;
import org.nutz.web.Webs.Err;

/**
 * 读取视频信息
 * 
 * @author pw,wendal
 *
 */
public class cmd_videoi extends JvmExecutor {

	@Override
	public void exec(WnSystem sys, String[] args) throws Exception {
		WnObj obj = getObj(sys, args);
		if (obj == null) {
			return;
		}
		if (ZType.isVideo(obj.type())) {
			File tmpFile = Wn.getCopyFile(sys.io, obj);
			String vpath = tmpFile.getAbsolutePath();

			VideoInfo vi = readVideoInfo(vpath);
            tmpFile.delete();
			if (vi != null) {
				sys.out.print(Json.toJson(vi));
			} else {
                //sys.err.printf("read %s(%s) videoInfo has err", obj.name(), obj.id());
			    throw Err.create("e.cmds.videoi.video_info_null");
			}
		} else {
		    throw Err.create("e.cmds.videoi.not_video", String.format("obj %s(%s) is not a video", obj.name(), obj.id()));
		}
	}

	public static VideoInfo readVideoInfo(String path) {
		try {
			VideoInfo vi = new VideoInfo();
			String cmd = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 " + path;
			StringBuilder tmp = Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
			vi.setLength(Double.parseDouble(tmp.toString().trim()));

			cmd = "ffprobe -v error -of default=noprint_wrappers=1:nokey=1 -select_streams v:0 -show_entries stream=width " + path;
			tmp = Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
			vi.setWidth(Integer.parseInt(tmp.toString().trim()));

			cmd = "ffprobe -v error -of default=noprint_wrappers=1:nokey=1 -select_streams v:0 -show_entries stream=height " + path;
			tmp = Lang.execOutput(cmd, Encoding.CHARSET_UTF8);
			vi.setHeight(Integer.parseInt(tmp.toString().trim()));

			if (vi.getFrameCount() > 0 && vi.getLength() > 0 && vi.getFrameRate() == 0) {
				vi.setFrameRate((int) Math.ceil(vi.getFrameCount() / vi.getLength()));
			}
			return vi;
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return null;
	}

	protected Integer[] getSize(WnSystem sys, WnObj vobj) {
		Integer[] sz = new Integer[2];
		if (!vobj.has("width")) {
			// 读取并写入对象中
			File srcFile = Wn.getCopyFile(sys.io, vobj);
			VideoInfo vi = readVideoInfo(srcFile.getAbsolutePath());

			vobj.setv("width", vi.getWidth());
			vobj.setv("height", vi.getHeight());
			vobj.setv("length", vi.getLength());
			sys.io.appendMeta(vobj, "^width|height|length$");
			srcFile.delete();
		}
		sz[0] = vobj.getInt("width");
		sz[1] = vobj.getInt("height");
		return sz;
	}

}
