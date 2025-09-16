package com.site0.walnut.ext.net.http.hdl;

import java.io.IOException;

import org.nutz.lang.Files;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.net.http.HttpClientContext;
import com.site0.walnut.ext.net.http.HttpClientFilter;
import com.site0.walnut.ext.net.http.bean.HttpContentDisposition;
import com.site0.walnut.ext.net.http.bean.WnHttpResponse;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class httpc_output extends HttpClientFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(autoname)$");
    }

    @Override
    protected void process(WnSystem sys, HttpClientContext fc, ZParams params) {
        // 得到目标输出路径
        String outputPath = params.val_check(0);

        // 判断一下
        boolean isPathAsDir = outputPath.endsWith("/");

        // 检查目标是否存在
        WnObj target = Wn.getObj(sys, outputPath);

        // 判断输出的目标是否为目录
        boolean is_output_dir = isPathAsDir
                                || (null != target && target.isDIR());

        if (isPathAsDir && !is_output_dir) {
            throw Er.create("e.cmd.httpc.output.PathIsDirButFile", outputPath);
        }

        // 创建目标对象
        if (null == target) {
            WnRace outRace = is_output_dir ? WnRace.DIR : WnRace.FILE;
            String aph = Wn.normalizeFullPath(outputPath, sys);
            target = sys.io.createIfNoExists(null, aph, outRace);
        }

        // # 指定目标输出路径，这里有两种情况
        try {
            WnHttpResponse resp = fc.getRespose();
            // 解析文件名
            String cds = resp.getHeaders().getString("content-disposition");
            HttpContentDisposition cd = null;
            String fileName = null;
            if (!Ws.isBlank(cds)) {
                cd = HttpContentDisposition.parse(cds);
                fileName = cd.getPreferredFilename();
            }

            // # 1. 如果目标已经存在，且为目录；或者路径是以 '/' 结尾
            // # > 则表示输出路径是一个目录，具体下载的文件，参看 HTTP Header 里的
            // # "Content-Disposition" 来指定文件名
            if (is_output_dir) {
                if (null == fileName) {
                    String urlPath = fc.context.getUrl().getPath();
                    fileName = Files.getName(urlPath);
                }
                fc.oOut = sys.io
                    .createIfNoExists(target, fileName, WnRace.FILE);
            }
            // # 2. 否则必然指定了一个文件的路径，那么 Header 里的 Content-Disposition
            // # > 指明的文件名，如果与路径名不同，则会将后缀名强制替换到下载文件名里。
            // # > 主名，则作为下载对象文件的 title 元数据
            // # 即，如果 httpc http://mysite.com/abc.txt @output ~/tmp/
            // # 其中响应的 Content-Disposition : "attachment; filename="MyPet.mp4""
            // # 那么下载的文件应该是 abc.mp4 ，并有 title:"MyPet"
            // # 如果 httpc http://mysite.com/path/to/data001 @output ~/tmp/
            // # 其中响应的 Content-Disposition : "attachment; filename="MyPet.mp4""
            // # 那么下载的文件应该是 data001.mp4 ，并有 title:"MyPet"
            else {
                if (null != fileName && !target.isSameName(fileName)) {
                    sys.io.rename(target, fileName);
                }
                fc.oOut = target;
            }

        }
        catch (IOException e) {
            throw Er.wrap(e);
        }

    }

}
