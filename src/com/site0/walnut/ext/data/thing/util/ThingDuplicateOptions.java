package com.site0.walnut.ext.data.thing.util;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.validate.WnMatch;

public class ThingDuplicateOptions {

    /**
     * 【选1】复制的份数。如果指定了 <code>toIds</code> 则无视这个参数
     */
    public int dupCount;

    /**
     * 【选1】目标记录ID,多个记录用半角逗号分隔
     */
    public List<String> toIds;

    /**
     * 指定目标记录的ID对应的键名，默认为"id"。这个参数可以更换为别的唯一键
     */
    public String toKey;

    /**
     * 一个过滤器表示要复制哪些字段
     */
    public WnMatch fieldFilter;

    /**
     * 浅层复制。表示如果记录是文件，只是复制引用。 否则会将文件也复制一份
     */
    public boolean shallow;

    /**
     * 指定所有对象都固定覆盖设置的元数据
     */
    public NutMap fixedMeta;

    /**
     * 表示复制的时候还要固定的复制哪些文件
     */
    public String[] copyFiles;

    /**
     * 一个匹配文件名的正则表达式，可以为 fvars 提供上下文变量
     */
    public Pattern fOutside;

    /**
     * 表示只有匹配上正则的，才会深层复制
     */
    public boolean fMatchOnly;

    /**
     * 一个可 Explain 的JSON对象，提供给 fnewname 渲染上下文 这个对象被 Explain 的上下文来自:
     * <ol>
     * <li>正则表达式 <code>g0...gn</code>
     * <li>一个固定的上下文变量<code>input</code>，表示文件名
     * <li>目标记录的所有元数据
     * <li>源记录所有的元数据
     * </ol>
     * 上述上下文变量优先级是从高到低的，即 1 为最高优先级
     */
    public NutMap fvars;

    /**
     * 一个字符串模板，表示复制后文件的新名称
     */
    public WnTmpl fNewname;

    /**
     * 当复制文件时，要 Copy 的字段
     */
    public WnMatch fFieldMatch;

    /**
     * 一个可 Explain 的JSON对象，提供复制文件时，要固定增加的字段，<br>
     * 渲染上下文为 -fvars
     */
    public NutMap fmeta;

    public ThingDuplicateOptions() {
        this.dupCount = 1;
        this.toIds = new LinkedList<>();
        this.toKey = "id";
        this.shallow = false;
    }

    public boolean hasToIds() {
        return null != toIds && toIds.size() > 0;
    }

}
