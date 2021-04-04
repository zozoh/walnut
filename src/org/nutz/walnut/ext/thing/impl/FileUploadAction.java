package org.nutz.walnut.ext.thing.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.nutz.castor.Castors;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.ThingDataAction;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.util.upload.HttpFormCallback;
import org.nutz.walnut.util.upload.HttpFormField;
import org.nutz.walnut.util.upload.HttpFormUpload;

/**
 * 处理 HTTP 上传流，返回所有符合过滤器的文件对象
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class FileUploadAction extends ThingDataAction<List<WnObj>> {

    /**
     * 文件名过滤器
     */
    public String fnm;

    /**
     * 这个在表单上传时，指明采用哪个表单字段 如果不指明，则每个表单的文件项，都会被写入，<br>
     * 采用的的文件名是原始文件名 如果表单只有一个文件，这个可以结合 -add 参数为文件改名
     */
    public String fieldName;

    // private WnMatch wm;

    /**
     * 重名模板
     */
    public String dupp;

    /**
     * 是否覆盖
     */
    public boolean overwrite;

    /**
     * 上传流
     */
    public InputStream ins;

    /**
     * 边界
     */
    public String boundary;

    @Override
    public List<WnObj> invoke() {
        WnObj oDir = this.myDir();
        List<WnObj> list = new LinkedList<>();

        // 首先准备判断条件
        // wm = Strings.isBlank(fnm) ? null : new AutoStrMatch(fnm);

        // 准备上传流解析
        HttpFormUpload upload = new HttpFormUpload(ins, boundary);

        try {
            upload.parse(new HttpFormCallback() {
                public void handle(HttpFormField field) throws IOException {
                    // 如果得到是字段
                    if (field.isText()) {
                        String fldName = field.getName();
                        // 文件名过滤器
                        if ("fnm".equals(fldName)) {
                            fnm = field.readAllString().trim();
                            // wm = Strings.isBlank(fnm) ? null : new
                            // AutoStrMatch(fnm);
                        }
                        // 是否覆盖
                        else if ("overwrite".equals(fldName) || "ow".equals(fldName)) {
                            String ow = field.readAllString();
                            if (!Strings.isBlank(ow)) {
                                overwrite = Castors.me().castTo(ow, Boolean.class);
                            }
                        }
                        // 重命名文件模板
                        else if ("dupp".equals(fldName)) {
                            String str = field.readAllString();
                            dupp = Strings.sBlank(str, dupp);
                        }
                    }
                    // 处理文件
                    else if (field.isFile()) {
                        // 得到文件名
                        String localFileName = field.getFileName();

                        // 得到这个文件真正的名称
                        String fileName = Strings.sBlank(fnm, localFileName);

                        // 匹配文件
                        if (field.isName(fieldName) /*
                                                     * || null == wm ||
                                                     * wm.match(localFileName)
                                                     */) {
                            // 首先判断文件是否存在
                            WnObj oM = io.fetch(oDir, fileName);

                            // 不存在，创建一个
                            if (null == oM) {
                                oM = io.create(oDir, fileName, WnRace.FILE);
                            }
                            // 如果存在 ...
                            else {
                                // 是否生成一个新的
                                if (!Strings.isBlank(dupp)) {
                                    oM = Things.createFileNoDup(io, oDir, fileName, dupp);
                                }
                                // 不能生成一个新的，并且还不能覆盖就抛错
                                else if (!overwrite) {
                                    throw Er.create("e.thing.file.exists", myFilePath(fileName));
                                }
                            }

                            // 修改 mime
                            if (field.hasContentType()) {
                                io.appendMeta(oM, Lang.map("mime", field.getContentType()));
                            }

                            // 写入文件
                            InputStream ins = field.getInputStream();
                            io.writeAndClose(oM, ins);

                            // 记入列表
                            list.add(oM);
                        }
                        // 跳过文件
                        else {
                            field.readAll();
                        }
                    }
                }
            });
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }

        // 指定了 dir 就更新计数
        if (this.hasDirName(fnm)) {
            String dnm = this.myFileDirName(fnm);
            Things.update_file_count(io, oT, dnm, _Q());
        }

        // 搞定
        return list;
    }

}
