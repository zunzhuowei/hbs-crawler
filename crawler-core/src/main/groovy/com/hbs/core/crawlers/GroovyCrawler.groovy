package com.hbs.core.crawlers

import com.alibaba.fastjson.JSON
import com.hbs.core.confs.CrawlerConfig
import com.hbs.core.constants.HttpRequestType
import com.hbs.core.enties.CrawlerParameters
import com.hbs.core.enties.RespBody
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by zun.wei on 2022/4/27.
 *
 */
class GroovyCrawler {

    private static final Logger log = LoggerFactory.getLogger(Crawler.class);

    /**
     * 爬虫工具类
     */
    CrawlerConfig crawlerConf;

    static GroovyCrawler newCrawler(CrawlerParameters parameters) {
        CrawlerConfig crawlerConf = new CrawlerConfig();
        crawlerConf.setOkHttpClient(parameters.okHttpClient ?: CrawlerConfig.DEFAULT);
        crawlerConf.tryTimes = parameters.tryTimes
        crawlerConf.reTryInterval = parameters.reTryInterval
        GroovyCrawler crawler = new GroovyCrawler();
        crawler.crawlerConf = crawlerConf;
        return crawler;
    }

    static GroovyCrawler newCrawler() {
        newCrawler(new CrawlerParameters())
    }


    GroovyCrawler execute(def params) {
        // 请求类型
        HttpRequestType requestType = params.requestType as HttpRequestType
        // 请求地址
        String url = params.url as String
        // 请求参数
        def requestParams = params.requestParams
        // 请求头
        Map<String, String> requestHeaders = params.requestHeaders as Map<String, String>
        // 重试次数
        Integer tryTimes = params.tryTimes ?: 0

        try {
            Call call = null
            // GET
            if (requestType == HttpRequestType.GET) {
                Request.Builder builder = new Request.Builder();
                HttpUrl.Builder urlBuilder = HttpUrl.get(url).newBuilder();
                requestParams = requestParams as Map
                boolean b = Objects.isNull(requestParams) || requestParams.isEmpty();
                if (!b) {
                    Map<String, String> getParamsMap = new HashMap<>()
                    requestParams.each { getParamsMap.put(it.key.toString(), it.value.toString()) }
                    for (Map.Entry<String, String> entry : getParamsMap.entrySet()) {
                        urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                    }
                }

                fillHeaders(requestHeaders, builder)

                HttpUrl httpUrl = urlBuilder.build();
                Request request = builder.url(httpUrl).build();
                log.info("doGet url call before --::{}", httpUrl);
                url = httpUrl;
                call = this.crawlerConf.getOkHttpClient().newCall(request);
            }

            //POST FORM
            if (requestType == HttpRequestType.POST_FORM) {
                FormBody.Builder formBodyBuilder = new FormBody.Builder();
                requestParams = requestParams as Map
                boolean b = Objects.isNull(requestParams) || requestParams.isEmpty();
                if (!b) {
                    Map<String, String> getParamsMap = new HashMap<>()
                    requestParams.each { getParamsMap.put(it.key.toString(), it.value.toString()) }
                    for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                        formBodyBuilder.add(entry.getKey(), entry.getValue());
                    }
                }

                FormBody formBody = formBodyBuilder.build();
                Request.Builder builder = new Request.Builder()
                fillHeaders(requestHeaders, builder)
                final Request request = builder.url(url)
                        .post(formBody)
                        .build();

                log.info("doPost url call before --::{}", url);
                call = this.crawlerConf.getOkHttpClient().newCall(request);
            }

            // POST JSON
            if (requestType == HttpRequestType.POST_JSON) {
                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        JSON.toJSONString(requestParams));

                Request.Builder builder = new Request.Builder()
                fillHeaders(requestHeaders, builder)
                final Request request = builder.url(url)
                        .post(requestBody)
                        .build();
                log.info("doPost url call before --::{}", url);
                call = this.crawlerConf.getOkHttpClient().newCall(request);
            }

            Response execute = call.execute();
            ResponseBody body = execute.body();
            if (Objects.isNull(body)) {
                log.warn("Crawler ${requestType.toString()} body is null!");
                return this;
            }
            try (InputStream inputStream = body.byteStream()) {
                MediaType mediaType = body.contentType();
                long contentLength = body.contentLength();
                byte[] bytes = IOUtils.toByteArray(inputStream);

                URL requestUrl = new URL(url);
                String scheme = requestUrl.getProtocol();
                String host = requestUrl.getHost() + requestUrl.getPath();
                final RespBody respBody = new RespBody();
                respBody.setBodyDatas(bytes);
                respBody.setCharset(mediaType);
                respBody.setContentLength(contentLength);
                respBody.setBaseUri(scheme + "://" + host);
                respBody.setRequestUrl(url);
                this.crawlerConf.getRespBodys().add(respBody);

            }
        } catch (IOException e) {
            //e.printStackTrace();
            String message = e.getMessage();
            log.error("Crawler ${requestType.toString()} Exception message,tryTimes ---::{},{}", message, tryTimes);
            if (tryTimes > 0) {
                long reTryInterval = this.crawlerConf.getReTryInterval();
                try {
                    Thread.sleep(reTryInterval);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                params.tryTimes--
                return execute(params);
            }
        }
        return this;
    }

    private void fillHeaders(Map<String, String> requestHeaders, Request.Builder builder) {
        if (Objects.nonNull(requestHeaders) && !requestHeaders.isEmpty()) {
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                builder.header(entry.key, entry.value)
            }
        }
    }

}
