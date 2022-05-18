package com.hbs.core.confs

import com.hbs.core.enties.RespBody
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Created by zun.wei on 2022/4/27.
 *
 */
class CrawlerConfig {


/**
 * 默认 OkHttpClient
 */
    public static final OkHttpClient DEFAULT = getNewOkHttpClientBuilder().build();


    /**
     * 获取新的 httpClient
     */
    static OkHttpClient.Builder getNewOkHttpClientBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(35, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
    }

    /**
     * http 请求工具类
     */
    OkHttpClient okHttpClient;

    /**
     * 响应体,key url;
     */
    Map<String, RespBody> respBodys = new ConcurrentHashMap<>()
    //List<RespBody> respBodys = new ArrayList<>();

    /**
     * 重试次数
     */
    int tryTimes = 50;

    /**
     * 重试时间间隔
     */
    long reTryInterval = 1000;

    /**
     * html dom 文本列表; key url
     */
    //List<Document> documents = new ArrayList<>();
    Map<String, Document> documents = new ConcurrentHashMap<>()


    /**
     * 下一页url链接
     */
    String nextPageUrl;

}
