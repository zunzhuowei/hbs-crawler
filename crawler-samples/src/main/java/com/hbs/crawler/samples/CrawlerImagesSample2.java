package com.hbs.crawler.samples;

import com.hbs.core.crawlers.GroovyCrawler;
import com.hbs.core.utils.ThreadPool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Created by zun.wei on 2022/4/28.
 */
public class CrawlerImagesSample2 {


    public static void main(String[] args) {
        GroovyCrawler crawler = GroovyCrawler.newCrawler("https://images.pexels.com/photos/995820/pexels-photo-995820.jpeg?fm=jpg");
        GroovyCrawler.loopCrawl(crawler, crawlLogic());
    }

    static BiFunction<GroovyCrawler, String, GroovyCrawler> crawlLogic() {
        return (crawler, nextPageUrl) -> {
            String[] ss = nextPageUrl.split("/");
            String idStr = ss[4];
            long id = Long.parseLong(idStr) + 1;
            String nextPageUrl2 = StringUtils.replace(nextPageUrl, idStr, Long.toString(id));

            ThreadPool.THREAD_POOL_EXECUTOR.execute(() -> {
                byte[] respBody = crawler.doGet(nextPageUrl).getBytesResultAndReleaseRespBody();
                if (respBody.length > 500) {
                    try {
                        FileUtils.writeByteArrayToFile(new File("imgs/" + idStr + ".jpeg"), respBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            if (id > 1995825) {
                return null;
            }
            return GroovyCrawler.newCrawler(nextPageUrl2);
        };
    }

}
