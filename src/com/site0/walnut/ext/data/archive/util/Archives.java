package com.site0.walnut.ext.data.archive.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.archive.api.ArchiveExtracting;
import com.site0.walnut.ext.data.archive.impl.TarArchiveExtracting;
import com.site0.walnut.ext.data.archive.impl.TgzArchiveExtracting;
import com.site0.walnut.ext.data.archive.impl.ZipArchiveExtracting;
import com.site0.walnut.util.stream.MarkableInputStream;

public abstract class Archives {

    public static ArchiveExtracting extract(String type,
                                            InputStream ins,
                                            Charset charset) {
        // ZIP
        if ("zip".equals(type)) {
            return new ZipArchiveExtracting(ins, charset);
        }
        // TAR
        else if ("tar".equals(type)) {
            return new TarArchiveExtracting(ins, charset);
        }
        // TGZ | tar.gz
        else if ("tar.gz".equals(type) || "tgz".equals(type)) {
            // 有时候是这样的，服务器有个文件是 gzip，这个文件名叫 abc.tar.gz 用户右键下载，
            // 因为就是静态文件下载， HTTP响应头部标识了内容为 gzip 压缩，
            // 某些聪明浏览器就自动解压了，那么在用户的磁盘上就有了 abc.tar.gz 文件
            // 但是它的内容已经是解压过的 .tar 文件，让后他再上传到 Walnut，调用解压就会发生
            //  > java.util.zip.ZipException: Not in GZIP format
            // 但是作为普通用户，似乎有没有什么很直觉的手段来阻止这种情况发生，
            // 因为在这种情况下, 我就需要看看这个文件的二进制到底是不是 gzip
            InputStream ins2 = MarkableInputStream.WRAP(ins);
            try {
                String mime = MimeSpy.getMimeType(ins2);
                // 的确是 gzip
                if ("application/gzip".equals(mime)) {
                    return new TgzArchiveExtracting(ins2, charset);
                }
                // 那么就当做是 Tar
                return new TarArchiveExtracting(ins2, charset);
            }
            catch (IOException e) {
                throw Er.wrap(e);
            }
        }

        throw Er.create("e.io.archive.extract.UnsupportType", type);
    }

    public static Instant T(FileTime ft) {
        if (null == ft) {
            return null;
        }
        return ft.toInstant();
    }

    public static Instant TD(Date d) {
        if (null == d) {
            return null;
        }
        return Instant.ofEpochMilli(d.getTime());
    }

}
