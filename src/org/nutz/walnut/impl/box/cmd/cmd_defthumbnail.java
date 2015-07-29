package org.nutz.walnut.impl.box.cmd;

import java.io.File;
import java.net.URL;

import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

/**
 * 生成默认缩率图
 * 
 * @author pw
 *
 */
public class cmd_defthumbnail extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "r");
        String tp = params.get("tp");
        boolean reset = params.is("r");
        WnObj thumbnailDir = sys.io.createIfNoExists(null, "/etc/thumbnail/", WnRace.DIR);
        WnObj tpDir = sys.io.createIfNoExists(thumbnailDir, tp, WnRace.DIR);
        try {
            // copy对应的图片过去
            URL tpurl = this.getClass().getResource("/thumbnail/" + tp + ".png");
            String tppath = tpurl.getPath();
            if (!Strings.isBlank(tppath)) {
                File tpf = new File(tppath);
                WnObj s256 = sys.io.createIfNoExists(tpDir,
                                                     Wn.thumbnail.size_256 + ".png",
                                                     WnRace.FILE);
                if (s256.len() == 0 || reset) {
                    sys.io.writeAndClose(s256, Streams.fileIn(tpf));
                    createThunbnail(sys, tpDir, s256, Wn.thumbnail.size_128);
                    createThunbnail(sys, tpDir, s256, Wn.thumbnail.size_64);
                    createThunbnail(sys, tpDir, s256, Wn.thumbnail.size_32);
                    createThunbnail(sys, tpDir, s256, Wn.thumbnail.size_24);
                    createThunbnail(sys, tpDir, s256, Wn.thumbnail.size_16);
                }
            } else {
                sys.err.printf("not find %s.png", tp);
            }
        }
        catch (Exception e) {}
    }

    private void createThunbnail(WnSystem sys, WnObj targetDir, WnObj src, String size) {
        sys.exec(String.format("chimg %s -z -s %s -o %s",
                               src.path(),
                               size,
                               targetDir.path() + "/" + size + "." + src.type()));
    }

}
