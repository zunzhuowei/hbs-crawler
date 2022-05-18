package com.hbs.crawler.test;

import com.hbs.core.crawlers.GroovyCrawler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * Created by zun.wei on 2022/5/17.
 */
public class DouyinPlayerListParse {


    public static void main(String[] args) throws IOException {
        final File file = new File("C:\\Users\\hui\\Desktop\\新建文本文档 (3).txt");
        final String string = IOUtils.toString(new FileInputStream(file), StandardCharsets.UTF_8);
        final Document parse = Jsoup.parse(string);
        final Elements elements = parse.select("a");
        System.out.println("a = " + elements);
        String basePath = "H:\\disk\\情感视频素材\\不想努力了\\";
        elements.forEach(element -> {
            try {
                final String href = element.attr("href");
                final String desc = element.getElementsByTag("p").text();
                final String https = element.getElementsByTag("img")
                        .attr("src");
                String dirName = basePath + href.split("/")[4] + "\\";

                FileUtils.write(new File(dirName + "href.txt"), "https:" + href, "UTF-8");
                FileUtils.write(new File(dirName + "desc.txt"), desc, "UTF-8");

                GroovyCrawler crawler = GroovyCrawler.newCrawler("https:" + https);
                GroovyCrawler.loopCrawl(crawler, crawlLogic(dirName + "img"));
                //elements.get(0).getElementsByTag("p").text()
                //elements.get(0).getElementsByTag("img").attr("src") + "https:"
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    static BiConsumer<GroovyCrawler, String> crawlLogic(String imgPath) {
        return (crawler, nextPageUrl) -> {
            byte[] respBody = crawler.doGet(nextPageUrl, new HashMap<>())
                    .getBytesResultAndReleaseRespBody(nextPageUrl);
            try {
                FileUtils.writeByteArrayToFile(new File(imgPath + ".jpeg"), respBody);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

}
