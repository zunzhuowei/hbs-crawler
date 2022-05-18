package com.hbs.crawler.samples;

import com.hbs.core.crawlers.GroovyCrawler;
import com.hbs.core.utils.ThreadPool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Created by zun.wei on 2022/4/28.
 */
public class CrawlerImagesSample2 {


    public static void main(String[] args) {
        GroovyCrawler crawler = GroovyCrawler.newCrawler("https://images.pexels.com/photos/998638/pexels-photo-998638.jpeg?fm=jpg");
        GroovyCrawler.loopCrawl(crawler, crawlLogic());
    }

    static BiConsumer<GroovyCrawler, String> crawlLogic() {
        return (crawler, nextPageUrl) -> {
            String[] ss = nextPageUrl.split("/");
            String idStr = ss[4];
            long id = Long.parseLong(idStr) + 1;
            String nextPageUrl2 = StringUtils.replace(nextPageUrl, idStr, Long.toString(id));

            ThreadPool.execute(() -> {
                byte[] respBody = crawler.doGet(nextPageUrl).getBytesResultAndReleaseRespBody(nextPageUrl);
                if (respBody.length > 500) {
                    try {
                        FileUtils.writeByteArrayToFile(new File("H:\\disk\\网络图片\\" + idStr + ".jpeg"), respBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            if (id > 199582500) {
                return;
            }
            crawler.setNextPageUrl(nextPageUrl2);
        };
    }

}
