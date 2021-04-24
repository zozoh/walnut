package org.nutz.walnut.ext.ooml.hdl;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import org.nutz.img.Images;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.ooml.OomlContext;
import org.nutz.walnut.ext.ooml.OomlFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.ooml.OomlEntry;
import org.nutz.walnut.ooml.xlsx.XlsxMedia;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class ooml_exportsheet extends OomlFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(quiet)$");
    }

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        // 准备输出目录
        String ph = params.val(0, "sheet");
        String aph = Wn.normalizeFullPath(ph, sys);
        WnObj oDir = sys.io.createIfNoExists(null, aph, WnRace.DIR);

        // 分析参数
        boolean quiet = params.is("quiet");
        int skip = params.getInt("skip", 0);
        int limit = params.getInt("limit", 0);
        String uniq = params.getString("uniq", null);
        String name = params.getString("name", null);
        Tmpl nm = Tmpl.parse(name);

        // 准备 uniq 的查询条件
        WnQuery q = Wn.Q.pid(oDir);

        // 默认第一行作为标题行
        int headIndex = params.getInt("head", 0);

        // 将行转换为对象
        Map<String, String> header = fc.sheet.getHeaderMapping(headIndex);
        List<NutBean> beans = fc.sheet.toBeans(header, headIndex + 1);

        // 准备统计数据
        int sum = limit > 0 ? limit : beans.size();
        int i = 0;
        int count = 0;
        Stopwatch sw = Stopwatch.begin();

        if (!quiet) {
            sys.out.printlnf("It will output %d rows", sum);
        }

        // 处理数据
        for (NutBean bean : beans) {
            if (i < skip) {
                i++;
                continue;
            }
            // 是否需要读取文件内容
            Object content = bean.get(fc.mapping.getContent());

            // 得到转换后的对象
            NutBean meta = fc.translateBean(bean);

            // 得到文件名
            String fnm = null;
            if (null != nm) {
                fnm = Ws.sBlank(nm.render(meta), null);
            }
            // 如果有名字，那么先尝试获取一下
            WnObj o = null;
            if (null != fnm) {
                o = sys.io.fetch(oDir, fnm);
            }

            // 是否需要去除重复
            if (null == o && null != uniq) {
                Object uv = meta.get(uniq);
                q.setv(uniq, uv);
                o = sys.io.getOne(q);
            }

            // 还是没有对象，那么创建一个
            if (null == o) {
                o = sys.io.create(oDir, Ws.sBlank(fnm, "${id}"), WnRace.FILE);
            }

            // 更新元数据
            sys.io.appendMeta(o, meta);

            // 最后更新一下内容
            if (null != content) {
                // 图片内容
                if (content instanceof XlsxMedia) {
                    XlsxMedia media = (XlsxMedia) content;
                    OomlEntry en = fc.ooml.getEntry(media.getPath());
                    byte[] bs = en.getContent();
                    // 处理图片内容
                    if (fc.mapping.isContentAsImage()) {
                        BufferedImage im = Images.read(bs);
                        sys.io.writeImage(o, im);
                    }
                    // 直接写入内容
                    else {
                        sys.io.writeBytes(o, bs);
                    }
                }
                // 其他当作文字内容
                else {
                    sys.io.writeText(o, content.toString());
                }
            }

            // 计数
            count++;
            if (limit > 0 && count > limit) {
                break;
            }

            // 打印日志
            if (!quiet) {
                sys.out.printlnf("[%d/%d] %d. %s", count, sum, i, o.name());
            }

            // 自增下标
            i++;
        }

        // 结束计时并打印
        sw.stop();
        if (!quiet) {
            sys.out.println("=============================");
            sys.out.printlnf("exported %d rows in %s", count, sw.toString());
        }
    }

}
