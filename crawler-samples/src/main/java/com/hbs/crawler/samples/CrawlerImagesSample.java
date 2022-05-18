package com.hbs.crawler.samples;

import com.hbs.core.crawlers.GroovyCrawler;
import com.hbs.core.enties.CrawlerParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by zun.wei on 2022/4/28.
 */
public class CrawlerImagesSample {


    public static void main(String[] args) {
        //GroovyCrawler crawler = GroovyCrawler.newCrawler("https://www.pexels.com/zh-cn/");
        GroovyCrawler crawler = GroovyCrawler.newCrawler("https://images.pexels.com/photos/995820/pexels-photo-995820.jpeg?fm=jpg");
        //String url = "https://images.pexels.com/photos/11780519/pexels-photo-11780519.jpeg?crop=entropy&cs=srgb&dl=pexels-albina-white-11780519.jpg&fit=crop&fm=jpg&h=1080&w=1920";
        //GroovyCrawler crawler = GroovyCrawler.newCrawler(url);
        GroovyCrawler.loopCrawl(crawler, crawlLogic());
    }

//
    static BiConsumer<GroovyCrawler, String> crawlLogic() {
        return (crawler, nextPageUrl) -> {

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
                    .getBytesResultAndReleaseRespBody(nextPageUrl);

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
