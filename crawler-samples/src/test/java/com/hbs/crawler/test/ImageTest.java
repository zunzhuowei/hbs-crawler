package com.hbs.crawler.test;

import com.hbs.core.crawlers.GroovyCrawler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * Created by zun.wei on 2022/5/15.
 */
public class ImageTest {

    public static void main(String[] args) {
        //GroovyCrawler crawler = GroovyCrawler.newCrawler("https://www.pexels.com/zh-cn/search/%E5%9F%8E%E5%B8%82/");
        GroovyCrawler crawler = GroovyCrawler.newCrawler("https://www.douyin.com/user/MS4wLjABAAAAtphR3obQL39ZlbSkjf9qPD0HHKlk7_XG-3W8_fAFsuDq2XpPBLS3BWUhFo8n5nva");
        GroovyCrawler.loopCrawl(crawler, crawlLogic());
    }

    //
    static BiConsumer<GroovyCrawler, String> crawlLogic() {
        return (crawler, nextPageUrl) -> {
            crawler.doGet(nextPageUrl)
                    .makeLastWebDocument()
                    .document((elements, crawler1) -> {
                        System.out.println("elements = " + elements);
                    });


            String[] ss = nextPageUrl.split("/");
            String idStr = ss[4];
            long id = Long.parseLong(idStr) + 1;
            String nextPageUrl2 = StringUtils.replace(nextPageUrl, idStr, Long.toString(id));

            byte[] respBody = crawler.doGet(nextPageUrl, new HashMap<>())
                    //.makeLastWebDocument()
                    //.document(document -> {
                    //    final String string = document.toString();
                    //    System.out.println("string = " + string);
                    //})
                    .getBytesResultAndReleaseRespBody();

            if (respBody.length > 500) {
                try {
                    FileUtils.writeByteArrayToFile(new File("imgs/" + idStr +".jpeg"), respBody);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (id > 1995825) {
                return;
            }
            crawler.setNextPageUrl(nextPageUrl2);
        };
    }

}
