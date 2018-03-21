package org.nutz.walnut.ext.mediax.apis;

import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.mediax.MxAPIKey;
import org.nutz.walnut.ext.mediax.bean.MxAccount;
import org.nutz.walnut.ext.mediax.bean.MxCrawl;
import org.nutz.walnut.ext.mediax.bean.MxPost;
import org.nutz.walnut.ext.mediax.bean.MxReCrawl;
import org.nutz.walnut.ext.mediax.bean.MxRePost;
import org.nutz.walnut.ext.mediax.util.Mxs;

@MxAPIKey("icp.chinaz.com")
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
        // 准备请求头
        Header header = Mxs.genHeader(null);

        // 爬取 HTML
        String url = cr.uri.toString();
        Response resp = Http.get(url, header, 10000, 10000);
        String html = resp.getContent();

        // 解析 HTML
        Document doc = Jsoup.parse(html);

        // 准备输出结果
        List<MxReCrawl> list = new LinkedList<>();

        // 寻找主要表格
        Element eleTable = doc.getElementsByClass("Tool-batchTable").first();

        // 得到表头
        Element eleTHead = eleTable.getElementsByTag("thead").first();
        Elements eleThs = eleTHead.getElementsByTag("th");
        String[] keys = new String[eleThs.size()];
        int i = 0;
        for (Element eleTh : eleThs) {
            keys[i++] = Strings.trim(eleTh.text());
        }

        // 得到表体，循环
        Element eleTBody = eleTable.getElementsByTag("tbody").first();
        Elements eleRows = eleTBody.children();
        for (Element eleRow : eleRows) {
            Elements eleTds = eleRow.children();
            i = 0;
            MxReCrawl rec = new MxReCrawl();
            for (Element eleTd : eleTds) {
                if (i > keys.length)
                    break;
                String val = Strings.trim(eleTd.text());
                String key = keys[i++];
                rec.put(key, val);
            }
            list.add(rec);
        }

        // 返回结果
        return list;
    }

}
