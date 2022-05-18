package com.hbs.crawler.samples;

import com.hbs.core.crawlers.GroovyCrawler;

/**
 * Created by zun.wei on 2022/4/28.
 */
public class CrawlerMp4Sample {


    public static void main(String[] args) {
        final GroovyCrawler groovyCrawler = GroovyCrawler.newCrawler("https://player.vimeo.com/external/488052191.hd.mp4?s=e389a83d2015857f6ba22071d062502865b1d9d1&profile_id=175&oauth2_token_id=57447761&download=1");
        GroovyCrawler.loopCrawl(groovyCrawler, (crawler, url) -> {
            final byte[] respBody = crawler.doGet(url).getBytesResultAndReleaseRespBody(url);
            crawler.saveFile("test.mp4", respBody);
        });
    }


}
