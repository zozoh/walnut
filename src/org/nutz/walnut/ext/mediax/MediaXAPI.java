package org.nutz.walnut.ext.mediax;

import java.util.Date;
import java.util.List;

import org.nutz.walnut.ext.mediax.bean.MxCrawl;
import org.nutz.walnut.ext.mediax.bean.MxPost;
import org.nutz.walnut.ext.mediax.bean.MxReCrawl;
import org.nutz.walnut.ext.mediax.bean.MxRePost;

/**
 * 封装了对某个媒体平台的所有操作。
 * <p>
 * 它需要被预先设置好连接信息。
 * <p>
 * <b>!!!本接口的实现类通常都是线程不安全的。因为它们通常要保存一些临时状态</b> <br>
 * 因此，请不要在多个线程间共享一个实例
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface MediaXAPI {

    MxRePost post(MxPost obj);

    List<MxReCrawl> crawl(MxCrawl cr);

    List<MxReCrawl> crawl(String uri, int limit);

    List<MxReCrawl> crawl(String uri, Date last);

    List<MxReCrawl> crawl(String uri, Date last, int limit);

}
