package org.nutz.walnut.ext.thing.impl;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.ThingDataAction;
import org.nutz.walnut.validate.WnMatch;
import org.nutz.walnut.validate.match.AutoStrMatch;

/**
 * 处理 HTTP 上传流，返回所有符合过滤器的文件对象
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class FileUploadAction extends ThingDataAction<List<WnObj>>{

    /**
     * 文件名过滤器
     */
    public String fnm;
    
    
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
        List<WnObj> list = new LinkedList<>();
        
        // 首先准备判断条件
        WnMatch wm = new AutoStrMatch(fnm);
        
        
        
        // 搞定
        return list;
    }
    
    
    
    
}
