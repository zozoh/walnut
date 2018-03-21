package org.nutz.walnut.ext.mediax.apis;

import java.util.List;

import org.nutz.walnut.ext.mediax.MxHost;
import org.nutz.walnut.ext.mediax.bean.MxAccount;
import org.nutz.walnut.ext.mediax.bean.MxCrawl;
import org.nutz.walnut.ext.mediax.bean.MxPost;
import org.nutz.walnut.ext.mediax.bean.MxReCrawl;
import org.nutz.walnut.ext.mediax.bean.MxRePost;

@MxHost("icp.chinaz.com")
public class ChinaZMeidaXAPI extends NoTicketMediaXAPI {

    public ChinaZMeidaXAPI(MxAccount account) {
        super(account);
    }

    @Override
    public MxRePost post(MxPost obj) {
        return null;
    }

    @Override
    public List<MxReCrawl> crawl(MxCrawl cr) {
        return null;
    }

}
