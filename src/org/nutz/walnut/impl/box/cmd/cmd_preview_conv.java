package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Streams;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZType;

import com.danoo.videox.ConvTask;
import com.danoo.videox.bean.VideoxTask;
import com.danoo.videox.task.simple.VideoTaskFactory;

public class cmd_preview_conv extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        WnObj obj = getObj(sys, args);
        if (obj == null) {
            return;
        }
        // 圖片
        if (ZType.isImage(obj.type())) {
            createImagePreview(sys, obj);
        }
        // 視頻
        else if (ZType.isVideo(obj.type())) {
            createVideoPreview(sys, obj);
        }
        // 其他文件
        else {
            // TODO 暂时不处理
        }
    }

    private String bgcolor_white = "rgb(255,255,255)";
    private String bgcolor_black = "rgb(0,0,0)";
    private String size_l = "256x256";
    private String size_m = "64x64";
    private String size_s = "24x24";

    private void createImagePreview(WnSystem sys, WnObj obj) {
        WnObj pdir = createThunbnailDirBySha1(sys, obj);
        createThunbnail(sys, pdir, obj, size_l);
        createThunbnail(sys, pdir, obj, size_m);
        createThunbnail(sys, pdir, obj, size_s);
    }

    private void createThunbnail(WnSystem sys, WnObj targetDir, WnObj src, String size) {
        sys.exec(String.format("chimg %s -z -bg %s -s %s -o %s",
                               src.path(),
                               bgcolor_black,
                               size,
                               targetDir.path() + "/" + size + "." + src.type()));
    }

    private static String sysTmpDir = System.getProperty("java.io.tmpdir");

    private void createVideoPreview(WnSystem sys, WnObj obj) {
        if (Wn.hasVCLibrary()) {
            String tmpdir = sysTmpDir + "/" + obj.id();
            String srcPath = sys.io.getRealPath(obj);
            WnObj pdir = createThunbnailDirBySha1(sys, obj);
            WnObj vdir = createPreviewVideoDirBySha1(sys, obj);
            org.nutz.lang.Files.createDirIfNoExists(tmpdir);
            // 生成预览图片
            createVideoPreviewImage(sys, obj, tmpdir, srcPath, pdir);
            // 生成预览视频
        }
    }

    private void createVideoPreviewImage(WnSystem sys,
                                         WnObj obj,
                                         String tmpdir,
                                         String srcPath,
                                         WnObj pdir) {
        String previewImg = tmpdir + "/preview.jpg";
        VideoxTask task = new VideoxTask();
        task.setSrc(srcPath);
        task.setThumb(previewImg);
        ConvTask ztask = VideoTaskFactory.createVideoConvTask("/", task);
        ztask.runTask(task);
        // 1.生成临时文件
        String ptmp = Wn.normalizeFullPath("~/.tmp/" + obj.id() + "_preview.jpg", sys);
        WnObj ptmpObj = sys.io.createIfNoExists(null, ptmp, WnRace.FILE);
        sys.io.writeAndClose(ptmpObj, Streams.fileIn(previewImg));
        // 2.生成缩略图
        createThunbnail(sys, pdir, ptmpObj, size_l);
        createThunbnail(sys, pdir, ptmpObj, size_m);
        createThunbnail(sys, pdir, ptmpObj, size_s);
        // 3.删除临时文件
        sys.io.delete(ptmpObj);
    }

    private WnObj createThunbnailDirById(WnSystem sys, WnObj obj) {
        String id = obj.id();
        String path = Wn.normalizeFullPath("~/.preview_thumbnail/id/" + id, sys);
        return sys.io.createIfNoExists(null, path, WnRace.DIR);
    }

    private WnObj createThunbnailDirBySha1(WnSystem sys, WnObj obj) {
        String sha1 = obj.sha1();
        String path = Wn.normalizeFullPath("~/.preview_thumbnail/sha1/" + sha1, sys);
        return sys.io.createIfNoExists(null, path, WnRace.DIR);
    }

    private WnObj createPreviewVideoDirBySha1(WnSystem sys, WnObj obj) {
        String sha1 = obj.sha1();
        String path = Wn.normalizeFullPath("~/.preview_video/sha1/" + sha1, sys);
        return sys.io.createIfNoExists(null, path, WnRace.DIR);
    }

}
