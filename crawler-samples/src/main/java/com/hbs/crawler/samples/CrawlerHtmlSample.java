package com.hbs.crawler.samples;

import com.hbs.core.crawlers.GroovyCrawler;
import org.jsoup.nodes.Element;

import java.util.function.BiConsumer;

/**
 * Created by zun.wei on 2022/4/28.
 */
public class CrawlerHtmlSample {

    public static void main(String[] args) {
        GroovyCrawler newCrawler = GroovyCrawler.newCrawler("https://xxx.ddd.com/sample");
        GroovyCrawler.loopCrawl(newCrawler, crawlLogic());
    }

    static BiConsumer<GroovyCrawler, String> crawlLogic() {
        return (newCrawler, nextPageUrl) -> {
            newCrawler.doGet(nextPageUrl)
                    .makeLastWebDocument()
                    .docSelect("#page-container > div.content > div.row.vod-type-list > a.item-video-container",
                            ((elements, crawler) -> {
                                for (Element element : elements) {
                                    String url = element.attr("abs:href");
                                    String titleNameSelector = "div > div.title";
                                    String titleName = element.select(titleNameSelector).text();
                                    //titleName = CrawlerUtils.removeSpecialSymbols(titleName);
                                    System.out.println("url = " + url);
                                    System.out.println("titleName = " + titleName);

                                    String m3u8Selector = "#page-container > div.content > div:nth-child(12) > div > " +
                                            "div.video-info.pc-video-info.col-md-5.col-xs-12.m-b-10 > div.addr.m-hide > div:nth-child(1) > div.text > input";
                                    crawler.doGet(url)
                                            .makeLastWebDocument()
                                            .docSelect(m3u8Selector, (eles, c1) -> {
                                                String index_m3u8 = eles.attr("value");
                                                System.out.println("index_m3u8 = " + index_m3u8);

                                            })
                                            .removeLastDocument()
                                            .removeLastRespBody();

                                }

                                // 下一页选择
                                String nextPageSelector = "#page-container > div.content > div.kscont > div > ol > li > span > a.next";
                                crawler.docSelect(nextPageSelector, ((elements1, crawler1) -> {
                                    //获取下一页链接
                                    crawler1.eleAttr(elements1, "abs:href", (attr, crawler2) -> {
                                        //crawler2.doGet(attr);
                                        crawler2.setNextPageUrl(attr);
                                        System.out.println("attr = " + attr);
                                    });
                                }));

                            }))
                    .removeLastDocument()
                    .removeLastRespBody();
        };
    }

}
