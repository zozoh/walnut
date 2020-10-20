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
import org.nutz.walnut.validate.WnMatch;
import org.nutz.walnut.validate.impl.AutoStrMatch;

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

    private WnMatch wm;

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
        wm = Strings.isBlank(fnm) ? null : new AutoStrMatch(fnm);

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
                            wm = Strings.isBlank(fnm) ? null : new AutoStrMatch(fnm);
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
                        String fnm = field.getFileName();

                        // 匹配文件
                        if (null == wm || wm.match(fnm)) {
                            // 首先判断文件是否存在
                            WnObj oM = io.fetch(oDir, fnm);

                            // 不存在，创建一个
                            if (null == oM) {
                                oM = io.create(oDir, fnm, WnRace.FILE);
                            }
                            // 如果存在 ...
                            else {
                                // 是否生成一个新的
                                if (!Strings.isBlank(dupp)) {
                                    oM = Things.createFileNoDup(io, oDir, fnm, dupp);
                                }
                                // 不能生成一个新的，并且还不能覆盖就抛错
                                else if (!overwrite) {
                                    throw Er.create("e.thing.file.exists", myFilePath(fnm));
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
