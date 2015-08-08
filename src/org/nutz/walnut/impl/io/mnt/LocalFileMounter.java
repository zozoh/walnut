package org.nutz.walnut.impl.io.mnt;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.impl.io.WnMounter;
import org.nutz.walnut.util.Wn;

public class LocalFileMounter implements WnMounter {

    private static final Pattern regex_id_mnt2 = Pattern.compile("^([\\d\\w]+)://(.+)$");

    @Override
    public WnObj get(MimeMap mimes, WnObj mo, String[] paths, int fromIndex, int toIndex) {
        // 获得挂载点对应的本地目录
        // data 的形式应该是 file://path/to/file
        String mnt = mo.mount();
        Matcher m = regex_id_mnt2.matcher(mnt);
        if (!m.find()) {
            throw Er.create("e.io.mnt.local.invalid", mnt);
        }
        String mntPath = m.group(2);
        File d = new File(Disks.normalize(mntPath));
        if (!d.exists()) {
            throw Er.create("e.io.mnt.local.noexists", mnt);
        }

        // 从父对象开始循环
        WnObj p = mo;
        WnObj o = p;
        for (int i = fromIndex; i < toIndex; i++) {
            // 获得文件对象
            String nm = paths[i];
            File f = Files.getFile(d, nm);

            if (!f.exists())
                return null;

            // 生成虚拟对象
            String rph = Disks.getRelativePath(d, f);
            o = new WnBean();
            o.id(String.format("%s:file://%s", mo.id(), rph));

            o.name(f.getName());
            Wn.set_type(mimes, o, null);
            if (f.isFile())
                o.race(WnRace.FILE);
            else
                o.race(WnRace.DIR);

            o.setParent(p);

            o.sha1("-no-sha1-");
            o.data("file://" + f.getAbsolutePath());
            o.len(f.length());

            o.createTime(f.lastModified());
            o.lastModified(f.lastModified());

            o.mode(0750);
            o.creator(mo.creator()).group(mo.creator()).mender(mo.creator());

            // 作为下一圈的父
            p = o;
            d = f;
        }
        // 返回
        return o;
    }

}
