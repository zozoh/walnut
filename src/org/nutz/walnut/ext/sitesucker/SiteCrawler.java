package org.nutz.walnut.ext.sitesucker;

import java.util.Set;
import java.util.regex.Pattern;

import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class SiteCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
                                                           + "|png|mp3|mp4|zip|gz))$");

    private WnSystem system;
    private ZParams params;
    private String targetUrl;

    public SiteCrawler(WnSystem system, ZParams params) {
        this.system = system;
        this.params = params;
        this.targetUrl = params.get("url");
        if (!targetUrl.startsWith("http://") || !targetUrl.startsWith("https://")) {
            this.targetUrl = "http://" + this.targetUrl;
        }
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches() && href.startsWith(targetUrl);
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        system.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            System.out.println("Text length: " + text.length());
            System.out.println("Html length: " + html.length());
            System.out.println("Number of outgoing links: " + links.size());
        }
    }
}
