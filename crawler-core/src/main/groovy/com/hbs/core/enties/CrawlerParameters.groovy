package com.hbs.core.enties

import okhttp3.OkHttpClient

/**
 * Created by zun.wei on 2022/4/27.
 *
 */
class CrawlerParameters {

    /**
     * http 请求工具类
     */
    OkHttpClient okHttpClient;

    /**
     * 重试次数
     */
    int tryTimes = 50;

    /**
     * 重试时间间隔
     */
    long reTryInterval = 1000;

    /**
     * 下一页地址，或者起始爬取地址
     */
    String nextPageUrl

}
