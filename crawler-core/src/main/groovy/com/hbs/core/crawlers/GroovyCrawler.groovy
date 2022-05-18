package com.hbs.core.crawlers

import com.alibaba.fastjson.JSON
import com.hbs.core.confs.CrawlerConfig
import com.hbs.core.constants.HttpRequestType
import com.hbs.core.enties.CrawlerParameters
import com.hbs.core.enties.RespBody
import com.hbs.core.utils.ThreadPool
import okhttp3.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.Charset
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Created by zun.wei on 2022/4/27.
 *
 */
class GroovyCrawler {

    private static final Logger log = LoggerFactory.getLogger(GroovyCrawler.class);

    /**
     * 爬虫工具类
     */
    CrawlerConfig crawlerConf;


    /**
     * 新建自定义爬虫
     * @param parameters 爬虫参数
     */
    static GroovyCrawler newCrawler(CrawlerParameters parameters) {
        CrawlerConfig crawlerConf = new CrawlerConfig();
        crawlerConf.setOkHttpClient(parameters.okHttpClient ?: CrawlerConfig.DEFAULT);
        crawlerConf.tryTimes = parameters.tryTimes
        crawlerConf.reTryInterval = parameters.reTryInterval
        crawlerConf.nextPageUrl = parameters.nextPageUrl
        GroovyCrawler crawler = new GroovyCrawler();
        crawler.crawlerConf = crawlerConf;
        return crawler;
    }

    /**
     * 新建默认爬虫
     */
    static GroovyCrawler newCrawler(String beginCrawlUrl) {
        def parameters = new CrawlerParameters()
        parameters.nextPageUrl = beginCrawlUrl
        newCrawler parameters
    }

    /**
     * get 请求
     * @param url 请求地址
     */
    GroovyCrawler doGet(String url) {
        doGet(url, [:])
    }

    /**
     * get 请求
     * @param url 请求地址
     * @param params 请求参数
     * @return this
     */
    GroovyCrawler doGet(String url, Map<String, Object> params) {
        doGet(url, params, [:])
    }

    /**
     * get 请求
     * @param url 请求地址
     * @param params 请求参数
     * @param headers 请求头
     * @return this
     */
    GroovyCrawler doGet(String url, Map<String, Object> params, Map<String, String> headers) {
        doGet(url, params, headers, crawlerConf.tryTimes)
    }

    /**
     * get 请求
     * @param url 请求地址
     * @param params 请求参数
     * @param headers 请求头
     * @param tryTimes 重试次数
     * @return this
     */
    GroovyCrawler doGet(String url, Map<String, Object> params, Map<String, String> headers, Integer tryTimes) {
        execute([url           : url,
                 requestType   : HttpRequestType.GET,
                 requestParams : params,
                 requestHeaders: headers,
                 tryTimes      : tryTimes
        ])
    }

    /**
     * post 表单请求
     * @param url 请求地址值
     * @return this
     */
    GroovyCrawler doPostForm(String url) {
        doPostForm(url, [:])
    }

    /**
     * post 表单请求
     * @param url 请求地址
     * @param params 请求参数
     * @return this
     */
    GroovyCrawler doPostForm(String url, Map<String, Object> params) {
        doPostForm(url, params, [:])
    }

    /**
     * post 表单请求
     * @param url 请求地址
     * @param params 请求参数
     * @param headers 请求头
     * @return this
     */
    GroovyCrawler doPostForm(String url, Map<String, Object> params, Map<String, String> headers) {
        doPostForm(url, params, headers, crawlerConf.tryTimes)
    }

    /**
     * post 表单请求
     * @param url 请求地址
     * @param params 请求参数
     * @param headers 请求头
     * @param tryTimes 重试次数
     * @return this
     */
    GroovyCrawler doPostForm(String url, Map<String, Object> params, Map<String, String> headers, Integer tryTimes) {
        execute([url           : url,
                 requestType   : HttpRequestType.POST_FORM,
                 requestParams : params,
                 requestHeaders: headers,
                 tryTimes      : tryTimes
        ])
    }

    /**
     *  post json body请求
     * @param url 请求地址
     * @return this
     */
    GroovyCrawler doPostJson(String url) {
        doPostJson(url, [:])
    }

    /**
     *  post json body请求
     * @param url 请求地址
     * @param params 请求参数
     * @return this
     */
    GroovyCrawler doPostJson(String url, def params) {
        doPostJson(url, params, [:])
    }

    /**
     *  post json body请求
     * @param url 请求地址
     * @param params 请求参数
     * @param headers 请求头
     * @return this
     */
    GroovyCrawler doPostJson(String url, def params, Map<String, String> headers) {
        doPostJson(url, params, headers, crawlerConf.tryTimes)
    }

    /**
     *  post json body请求
     * @param url 请求地址
     * @param params 请求参数
     * @param headers 请求头
     * @param tryTimes 重试次数
     * @return this
     */
    GroovyCrawler doPostJson(String url, def params, Map<String, String> headers, Integer tryTimes) {
        execute([url           : url,
                 requestType   : HttpRequestType.POST_JSON,
                 requestParams : params,
                 requestHeaders: headers,
                 tryTimes      : tryTimes
        ])
    }

    /**
     * 网络请求
     * @param params 请求参数；
     * // 请求类型
     * HttpRequestType requestType
     * // 请求地址
     * String url
     * // 请求参数
     * def requestParams
     * // 请求头
     * requestHeaders
     * // 重试次数
     * Integer tryTimes
     * @return
     */
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
                this.crawlerConf.getRespBodys().put(url, respBody);

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


    /**
     * 删除一个请求响应体
     */
    GroovyCrawler removeRespBody(String url) {
        this.crawlerConf.getRespBodys().remove(url)
        return this;
    }

    /**
     * 删除一个 web document 文档缓存
     */
    GroovyCrawler removeDocument(String url) {
        this.crawlerConf.getDocuments().remove(url)
        return this;
    }


    /**
     * 构建 web document，指定请求的的顺序下标(从0开始)
     *
     * @param requestIndex 请求顺序下标(从0开始)
     */
    GroovyCrawler makeWebDocument(String url) {
        Map<String, RespBody> respBodys = this.crawlerConf.getRespBodys();
        RespBody respBody = respBodys.get(url);
        String baseUri = respBody.getBaseUri();
        String result = getResult(url);
        Document document = Jsoup.parse(result, baseUri);
        this.crawlerConf.getDocuments().put(url, document);
        return this;
    }

    /**
     * 获取web document文档
     * @param docIndex 文档下标
     * @param documentConsumer 文档消费函数
     */
    GroovyCrawler document(String url, Consumer<Document> documentConsumer) {
        Map<String, Document> documents = this.crawlerConf.getDocuments();
        documentConsumer.accept(documents.get(url));
        return this;
    }


    /**
     * 获取web document文档
     * @param docIndex 文档下标
     * @param documentCrawlerBiConsumer 文档消费函数
     */
    GroovyCrawler document(String url, BiConsumer<Document, GroovyCrawler> documentCrawlerBiConsumer) {
        Map<String, Document> documents = this.crawlerConf.getDocuments();
        documentCrawlerBiConsumer.accept(documents.get(url), this);
        return this;
    }

    /**
     * 对 web document 进行 css选择器选择
     *
     * @param docIndex web document 文档下标(从0开始)
     * @param cssQuery css 选择器
     * @param eleConsumer elements 消费函数
     */
    GroovyCrawler docSelect(String url, String cssQuery, BiConsumer<Elements, GroovyCrawler> eleConsumer) {
        Map<String, Document> documents = this.crawlerConf.getDocuments();
        Document document = documents.get(url);
        Elements elements = document.select(cssQuery);
        eleConsumer.accept(elements, this);
        return this;
    }


    /**
     * 对 web document 的 elements 进行选择
     *
     * @param elements web document 的 elements
     * @param cssQuery css 选择器
     * @param eleConsumer 新的elements 消费函数
     */
    GroovyCrawler eleSelect(Elements elements, String cssQuery, BiConsumer<Elements, GroovyCrawler> eleConsumer) {
        Elements elements1 = elements.select(cssQuery);
        eleConsumer.accept(elements1, this);
        return this;
    }

    /**
     *  获取元素中文字中包含指定字符串的元素列表
     * @param elements Elements
     * @param containText 指定字符串
     * @return 元素列表
     */
    List<Element> element(Elements elements, String... containText) {
        List<Element> eles = new ArrayList<>();
        if (Objects.isNull(elements)) {
            return eles;
        }
        for (Element element : elements) {
            String text = element.text();

            boolean hasText = true;
            for (String ct : containText) {
                boolean b = StringUtils.contains(text, ct);
                if (!b) {
                    hasText = false;
                    break;
                }
            }
            if (hasText) {
                eles.add(element);
            }
        }
        return eles;
    }

    /**
     * 获取元素中文字中包含指定字符串的元素列表的第一个
     * @param elements Elements
     * @param containText 指定字符串
     * @return 第一个元素，如果不存在则返回null
     */
    Element firstElement(Elements elements, String... containText) {
        List<Element> eles = this.element(elements, containText);
        return eles.isEmpty() ? null : eles.get(0);
    }

    /**
     * 获取 web document 的 elements attr 属性值
     *
     * @param elements web document 的 elements
     * @param attributeKey 属性名称
     * @param attrConsumer 属性值消费函数
     */
    GroovyCrawler eleAttr(Elements elements, String attributeKey, BiConsumer<String, GroovyCrawler> attrConsumer) {
        String attr = elements.attr(attributeKey);
        attrConsumer.accept(attr, this);
        return this;
    }

    /**
     * 设置下一页请求的 url
     *
     * @param nextPageUrl 下一页url
     */
    GroovyCrawler setNextPageUrl(String nextPageUrl) {
        this.crawlerConf.setNextPageUrl(nextPageUrl);
        return this;
    }

    /**
     * 获取下一页请求url
     */
    String getNextPage() {
        String nextPageUrl = this.crawlerConf.getNextPageUrl();
        this.crawlerConf.setNextPageUrl(null);
        return nextPageUrl;
    }

    /**
     * 是否有下一页
     */
    boolean hasNextPage() {
        String nextPageUrl = this.crawlerConf.getNextPageUrl();
        return StringUtils.isNotBlank(nextPageUrl);
    }

    /**
     * 获取响应体值（字符串）
     *
     * @param requestIndex 请求顺序下标(从0开始)
     */
    String getResult(String url) {
        Map<String, RespBody> respBodys = this.crawlerConf.getRespBodys();
        RespBody respBody = respBodys.get(url);
        byte[] bodyDatas = respBody.getBodyDatas();
        Charset charset = respBody.getCharset();
        return new String(bodyDatas, charset);
    }

    /**
     * 获取响应体值（字符串）
     * 并且释放响应体的缓存
     *
     * @param requestIndex 请求顺序下标(从0开始)
     */
    String getResultAndReleaseRespBody(String url) {
        String result = getResult(url);
        this.removeRespBody(url);
        return result;
    }

    /**
     * 获取响应体值 字节数组
     *
     * @param requestIndex 请求顺序下标(从0开始)
     */
    byte[] getBytesResult(String url) {
        Map<String, RespBody> respBodys = this.crawlerConf.getRespBodys();
        RespBody respBody = respBodys.get(url);
        return respBody.getBodyDatas();
    }

    /**
     * 获取响应体值 字节数组
     * 并且释放响应体的缓存
     *
     * @param requestIndex 请求顺序下标(从0开始)
     */
    byte[] getBytesResultAndReleaseRespBody(String url) {
        byte[] bytesResult = getBytesResult(url);
        this.removeRespBody(url);
        return bytesResult;
    }


    /**
     * 获取响应体值（泛型对象），通过 JSON.parseObject(result, clazz)
     *
     * @param requestIndex 请求顺序下标(从0开始)
     * @param clazz 指定类型的 class
     * @param <T >            泛型
     */
    def <T> T getResult(String url, Class<T> clazz) {
        String result = getResult(url);
        return JSON.parseObject(result, clazz);
    }

    /**
     * 获取响应体值（泛型对象），通过 JSON.parseObject(result, clazz)
     * 并且释放响应体的缓存
     *
     * @param requestIndex 请求顺序下标(从0开始)
     * @param clazz 指定类型的 class
     * @param <T >            泛型
     */
    def <T> T getResultAndReleaseRespBody(String url, Class<T> clazz) {
        T result = getResult(url, clazz);
        this.removeRespBody(url);
        return result;
    }

    /**
     * 将字节数据写入文件中
     * @param fileName 文件全限定名
     * @param bytes 写入文件的数据
     */
    void saveFile(String fileName, byte[] bytes) {
        FileUtils.writeByteArrayToFile(new File(fileName), bytes)
    }

    /**
     * 根据下一页地址循环爬取
     * @param crawler 爬虫
     * @param crawlLogic 爬虫逻辑
     */
    static void loopCrawl(GroovyCrawler crawler, BiConsumer<GroovyCrawler, String> crawlLogic) {
        do {
            String nextPage = crawler.getNextPage();
            if (StringUtils.isNotBlank(nextPage)) {
                crawlLogic.accept(crawler, nextPage)
            }
        } while (crawler.hasNextPage())
    }


}
