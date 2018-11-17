package org.nutz.walnut.util;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback2;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.web.Webs.Err;

/**
 * <b>!!!注意</b> 本类线程不安全，不要在多个线程共享实例
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnObjCopying {

    private WnIo io;

    /**
     * 递归
     */
    private boolean recur;

    /**
     * 每次 copy 的回调
     */
    private Callback2<WnObj, WnObj> callback;
    /**
     * 属性过滤器，如果为空表示不 copy 属性
     */
    private String propFilter;
    /**
     * 如果目标存在，先删除再创建（仅限于文件）
     */
    private boolean dropBeforeCopy;

    private WnObj oSrcObj;
    private WnObj oDstObj;

    /**
     * 强制设定新文件/目录的所属者，null 表示为当前账号
     */
    private String own;
    /**
     * 强制设定新文件/目录的所属组，null 表示为当前账号
     */
    private String grp;

    /**
     * @param io
     *            数据接口
     */
    public WnObjCopying(WnIo io) {
        this.io = io;
    }

    /**
     * @param o
     *            源对象（可以是目录或者文件）
     * @param targetPath
     *            目标路径,如果是个目录且以'/'结尾，则在其内生成源对象
     */
    public WnObj exec(WnObj o, String targetPath) {
        this.oSrcObj = o;
        // 检查源，如果是目录，则必须标识 -r
        if (oSrcObj.isDIR() && !this.recur) {
            throw Err.create("e.io.copy.src_is_dir", oSrcObj);
        }

        // 得到目标
        this.oDstObj = io.fetch(null, targetPath);

        // 目标不存在:
        // 应该先检查一下其父是否存在，如果不存在看看是不是 -r
        // 总之要创建一个目标出来
        if (null == oDstObj) {
            // 必须存在父
            if (!this.recur) {
                WnObj oP = io.check(null, Files.getParent(targetPath));
                oDstObj = io.createIfNoExists(oP, Files.getName(targetPath), oSrcObj.race());
            }
            // 否则自由创建
            else {
                oDstObj = io.createIfNoExists(null, targetPath, oSrcObj.race());
            }
            // 执行 Copy 就好了
            __recur_copy_obj(oSrcObj, oDstObj);
        }
        // 否则，不能是自己 copy 给自己就好
        else {
            // 自己 copy 自己，不能够啊
            if (oDstObj.isSameId(oSrcObj)) {
                throw Er.create("e.io.copy.self", oSrcObj);
            }
            // 目标是一个文件夹
            if (oDstObj.isDIR()) {
                WnObj o_dst2 = oDstObj;
                // 在里面创建与源同名的目标
                if (targetPath.endsWith("/")) {
                    o_dst2 = __do_create(oSrcObj, oDstObj);
                }
                // 执行 Copy
                __recur_copy_obj(oSrcObj, o_dst2);
                // 记录返回结果
                oDstObj = o_dst2;
            }
            // 目标是一个文件
            else if (oDstObj.isFILE()) {
                // 源必须是一个文件
                if (!oSrcObj.isFILE()) {
                    throw Er.create("e.io.copy.file2dir",
                                    oSrcObj.path() + " ->> " + oDstObj.path());
                }
                // 执行 Copy
                __recur_copy_obj(oSrcObj, oDstObj);
            }
            // 靠！什么鬼！
            else {
                throw Lang.impossible();
            }
        }
        // 返回目标
        return oDstObj;
    }

    private void __copy_meta(WnObj oSrc, WnObj oDst) {
        if (!Strings.isBlank(propFilter)) {
            NutBean meta = oSrc.pickBy(propFilter);
            // 重设所有者
            if (!Strings.isBlank(this.own))
                meta.setv("c", this.own);

            // 重设组
            if (!Strings.isBlank(this.grp))
                meta.setv("g", this.grp);
            
            // 确保不 copy 缩略图
            meta.remove("thumb");

            io.appendMeta(oDst, meta);
        }
    }

    private WnObj __do_create(WnObj o_src, WnObj o_dst) {
        if (this.dropBeforeCopy && o_src.isFILE()) {
            return io.createIfExists(o_dst, o_src.name(), o_src.race());
        }
        return io.createIfNoExists(o_dst, o_src.name(), o_src.race());
    }

    private void __recur_copy_obj(WnObj o_src, WnObj o_dst) {
        // 复制元数据
        __copy_meta(o_src, o_dst);

        // 回调
        if (null != callback)
            callback.invoke(o_dst, this.oDstObj);

        // 如果是文件夹，递归
        if (o_src.isDIR() && o_dst.isDIR()) {
            Wn.Io.eachChildren(io, o_src, new Each<WnObj>() {
                public void invoke(int index, WnObj o, int length) {
                    WnObj oTa = __do_create(o, o_dst);

                    __recur_copy_obj(o, oTa);
                }
            });
        }
        // 如果是文件，内容 copy
        else if (o_src.isFILE() && o_dst.isFILE()) {
            Wn.Io.copyFile(io, o_src, o_dst);
        }
        // 靠，不可能
        else {
            throw Lang.impossible();
        }

    }

    public boolean isRecur() {
        return recur;
    }

    public void setRecur(boolean recur) {
        this.recur = recur;
    }

    public void setOwn(String own) {
        this.own = own;
    }

    public void setGrp(String grp) {
        this.grp = grp;
    }

    public boolean isDropBeforeCopy() {
        return dropBeforeCopy;
    }

    public void setDropBeforeCopy(boolean dropBeforeCopy) {
        this.dropBeforeCopy = dropBeforeCopy;
    }

    public void setCallback(Callback2<WnObj, WnObj> callback) {
        this.callback = callback;
    }

    public String getPropFilter() {
        return propFilter;
    }

    public void setPropFilter(String propFilter) {
        this.propFilter = propFilter;
    }

    public void setPropDefaultFilter() {
        this.propFilter = "!^(id|pid|race|ph|c|m|g|md|nm|d[0-9]|ct|lm|data|sha1|len|thumb|videoc_dir)$";
    }

    public void setPropOwnerFilter() {
        this.propFilter = "!^(id|pid|race|ph|nm|d[0-9]|ct|lm|data|sha1|len|thumb|videoc_dir)$";
    }

}
