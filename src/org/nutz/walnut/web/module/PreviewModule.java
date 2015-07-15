package org.nutz.walnut.web.module;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Param;

/**
 * 为obj对象提供预览功能
 *
 * 为图片提供缩略图
 * 
 * 为视频提供预览视频(分辨率缩小)
 * 
 * 为纯文本文件提供前100行
 * 
 * 为word文档提供前2页
 * 
 * @author pw
 *
 */
@IocBean
@At("/preview")
public class PreviewModule extends AbstractWnModule {

    @At("/thumbnail")
    public Object getPreviewThumbnail(@Param("obj") String obj, @Param("size") int size) {
        // 找到obj对象

        // 查找对应的缩略图对象

        // 没有的话返回默认缩略图
        return getDefaultPreviewThumbnail("", size);
    }

    @At("/default/thumbnail")
    private Object getDefaultPreviewThumbnail(@Param("tp") String tp, @Param("size") int size) {
        return null;
    }

    @At("/video")
    public Object getPreviewVideo(@Param("obj") String obj) {
        return null;
    }

}