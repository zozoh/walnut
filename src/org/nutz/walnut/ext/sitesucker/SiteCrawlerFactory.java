package org.nutz.walnut.ext.sitesucker;

import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

import edu.uci.ics.crawler4j.crawler.CrawlController;

public class SiteCrawlerFactory implements CrawlController.WebCrawlerFactory {

    private WnSystem system;
    private ZParams params;

    public SiteCrawlerFactory(WnSystem system, ZParams params) {
        this.system = system;
        this.params = params;
    }

    @Override
    public SiteCrawler newInstance() throws Exception {
        return new SiteCrawler(system, params);
    }

}
