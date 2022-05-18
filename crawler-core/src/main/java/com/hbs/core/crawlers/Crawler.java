//package com.hbs.core.crawlers;
//
//import com.alibaba.fastjson.JSON;
//import com.hbs.core.confs.CrawlerConfig;
//import com.hbs.core.enties.RespBody;
//import okhttp3.*;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.nio.charset.Charset;
//import java.util.*;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
///**
// * Created by zun.wei on 2021/1/24.
// */
//public class Crawler {
//
//    private static final Logger log = LoggerFactory.getLogger(Crawler.class);
//
//    /**
//     * 爬虫工具类
//     */
//    private CrawlerConfig crawlerConf;
//
//    public void setCrawlerConfig(CrawlerConfig crawlerConf) {
//
//        this.crawlerConf = crawlerConf;
//    }
//
//    private CrawlerConfig getCrawlerConfig() {
//        return this.crawlerConf;
//    }
//
//
//    /**
//     * 新建爬虫
//     */
//    public static Crawler newCrawler() {
//        return Crawler.newCrawler(CrawlerConfig.DEFAULT);
//    }
//
//    /**
//     * 新建爬虫
//     */
//    public static Crawler newCrawler(OkHttpClient okHttpClient) {
//        CrawlerConfig crawlerConf = new CrawlerConfig();
//        crawlerConf.setOkHttpClient(okHttpClient);
//        Crawler crawler = new Crawler();
//        crawler.setCrawlerConfig(crawlerConf);
//        return crawler;
//    }
//
//    /**
//     * 新建爬虫
//     *
//     * @param clientFunction clientFunction
//     */
//    public static Crawler newCrawler(Function<OkHttpClient.Builder, OkHttpClient> clientFunction) {
//        OkHttpClient.Builder builder = CrawlerConfig.getNewOkHttpClientBuilder();
//        OkHttpClient okHttpClient = clientFunction.apply(builder);
//        return Crawler.newCrawler(okHttpClient);
//    }
//
//    /**
//     * get 请求
//     *
//     * @param url 请求链接
//     */
//    public Crawler doGet(String url) {
//        return doGet(url, new HashMap<>(0));
//    }
//
//    /**
//     * get 请求
//     *
//     * @param url       请求链接
//     * @param respBytes 相应返回的字节数组处理函数
//     */
//    public Crawler doGet(String url, BiConsumer<byte[], Crawler> respBytes) {
//        return doGet(url, null, respBytes);
//    }
//
//    /**
//     * get 请求
//     *
//     * @param url    请求链接
//     * @param params 请求参数
//     */
//    public Crawler doGet(String url, Map<String, String> params) {
//        int tryTimes = this.crawlerConf.getTryTimes();
//        return doGet(url, params, tryTimes, null);
//    }
//
//    /**
//     * get 请求
//     *
//     * @param url       请求链接
//     * @param params    请求参数
//     * @param respBytes 相应返回的字节数组处理函数
//     */
//    public Crawler doGet(String url, Map<String, String> params, BiConsumer<byte[], Crawler> respBytes) {
//        int tryTimes = this.crawlerConf.getTryTimes();
//        return doGet(url, params, tryTimes, respBytes);
//    }
//
//    /**
//     * get 请求
//     *
//     * @param url       请求链接
//     * @param params    请求参数
//     * @param tryTimes  重试次数
//     * @param respBytes 相应返回的字节数组处理函数
//     */
//    public Crawler doGet(String url, Map<String, String> params, int tryTimes, BiConsumer<byte[], Crawler> respBytes) {
//        try {
//            Request.Builder builder = new Request.Builder();
//            HttpUrl.Builder urlBuilder = HttpUrl.get(url).newBuilder();
//            boolean b = Objects.isNull(params) || params.isEmpty();
//            if (!b) {
//                for (Map.Entry<String, String> entry : params.entrySet()) {
//                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
//                }
//            }
//
//            HttpUrl httpUrl = urlBuilder.build();
//            Request request = builder.url(httpUrl).build();
//            log.info("doGet url call before --::{}", url);
//            final Call call = this.crawlerConf.getOkHttpClient().newCall(request);
//            Response execute = call.execute();
//            ResponseBody body = execute.body();
//            if (Objects.isNull(body)) {
//                log.warn("Crawler doGet body is null!");
//                return this;
//            }
//            try (InputStream inputStream = body.byteStream()) {
//                MediaType mediaType = body.contentType();
//                long contentLength = body.contentLength();
//                byte[] bytes = IOUtils.toByteArray(inputStream);
//                if (Objects.nonNull(respBytes)) {
//                    respBytes.accept(bytes, this);
//                    return this;
//                }
//                String scheme = httpUrl.scheme();
//                String host = httpUrl.host();
//                URL url1 = httpUrl.url();
//                final RespBody respBody = new RespBody();
//                respBody.setBodyDatas(bytes);
//                respBody.setCharset(mediaType);
//                respBody.setContentLength(contentLength);
//                respBody.setBaseUri(scheme + "://" + host);
//                respBody.setRequestUrl(url1.toString());
//                this.crawlerConf.getRespBodys().add(respBody);
//
//            }
//        } catch (IOException e) {
//            //e.printStackTrace();
//            String message = e.getMessage();
//            log.error("Crawler doGet Exception message,tryTimes ---::{},{}", message, tryTimes);
//            if (tryTimes > 0) {
//                long reTryInterval = this.crawlerConf.getReTryInterval();
//                try {
//                    Thread.sleep(reTryInterval);
//                } catch (InterruptedException interruptedException) {
//                    interruptedException.printStackTrace();
//                }
//                return doGet(url, params, --tryTimes, respBytes);
//            }
//        }
//        return this;
//    }
//
//    /**
//     * post 请求
//     *
//     * @param url 请求链接
//     */
//    public Crawler doPost(String url) {
//        return doPost(url, new HashMap<>(0));
//    }
//
//    /**
//     * post 请求
//     *
//     * @param url       请求链接
//     * @param respBytes 相应返回的字节数组处理函数
//     */
//    public Crawler doPost(String url, BiConsumer<byte[], Crawler> respBytes) {
//        return doPost(url, null, respBytes);
//    }
//
//    /**
//     * post 请求
//     *
//     * @param url       请求链接
//     * @param formDatas form 表单参数
//     */
//    public Crawler doPost(String url, Map<String, String> formDatas) {
//        int tryTimes = this.crawlerConf.getTryTimes();
//        return doPost(url, formDatas, tryTimes, null);
//    }
//
//    /**
//     * post 请求
//     *
//     * @param url       请求链接
//     * @param formDatas form 表单参数
//     * @param respBytes 相应返回的字节数组处理函数
//     */
//    public Crawler doPost(String url, Map<String, String> formDatas, BiConsumer<byte[], Crawler> respBytes) {
//        int tryTimes = this.crawlerConf.getTryTimes();
//        return doPost(url, formDatas, tryTimes, respBytes);
//    }
//
//    /**
//     * post 请求
//     *
//     * @param url       请求链接
//     * @param formDatas form 表单参数
//     * @param tryTimes  失败重试次数
//     * @param respBytes 相应返回的字节数组处理函数
//     */
//    public Crawler doPost(String url, Map<String, String> formDatas, int tryTimes, BiConsumer<byte[], Crawler> respBytes) {
//        try {
//            FormBody.Builder formBodyBuilder = new FormBody.Builder();
//            boolean b = Objects.isNull(formDatas) || formDatas.isEmpty();
//            if (!b) {
//                for (Map.Entry<String, String> entry : formDatas.entrySet()) {
//                    formBodyBuilder.add(entry.getKey(), entry.getValue());
//                }
//            }
//            FormBody formBody = formBodyBuilder.build();
//            final Request request = new Request.Builder()
//                    .url(url)
//                    .post(formBody)
//                    .build();
//            log.info("doPost url call before --::{}", url);
//            final Call call = this.crawlerConf.getOkHttpClient().newCall(request);
//            Response execute = call.execute();
//            ResponseBody body = execute.body();
//            if (Objects.isNull(body)) {
//                log.warn("Crawler doPost body is null!");
//                return this;
//            }
//            try (InputStream inputStream = body.byteStream()) {
//                MediaType mediaType = body.contentType();
//                long contentLength = body.contentLength();
//                byte[] bytes = IOUtils.toByteArray(inputStream);
//                if (Objects.nonNull(respBytes)) {
//                    respBytes.accept(bytes, this);
//                    return this;
//                }
//                final RespBody respBody = new RespBody();
//                respBody.setBodyDatas(bytes);
//                respBody.setCharset(mediaType);
//                respBody.setContentLength(contentLength);
//                respBody.setBaseUri(url);
//                respBody.setRequestUrl(url);
//                this.crawlerConf.getRespBodys()
//                        .add(respBody);
//
//            }
//        } catch (IOException e) {
//            //e.printStackTrace();
//            String message = e.getMessage();
//            log.error("Crawler doPost Exception message,tryTimes ---::{},{}", message, tryTimes);
//            if (tryTimes > 0) {
//                long reTryInterval = this.crawlerConf.getReTryInterval();
//                try {
//                    Thread.sleep(reTryInterval);
//                } catch (InterruptedException interruptedException) {
//                    interruptedException.printStackTrace();
//                }
//                return doPost(url, formDatas, --tryTimes, respBytes);
//            }
//        }
//        return this;
//    }
//
//    /**
//     * post 请求
//     *
//     * @param url               请求链接
//     * @param requestBodyEntity 请求体实体类
//     * @param <T>               请求体类型
//     */
//    public <T> Crawler doPost(String url, T requestBodyEntity) {
//        int tryTimes = this.crawlerConf.getTryTimes();
//        return doPost(url, requestBodyEntity, tryTimes, null);
//    }
//
//    /**
//     * post 请求
//     *
//     * @param url               请求链接
//     * @param requestBodyEntity 请求体实体类
//     * @param respBytes         相应返回的字节数组处理函数
//     * @param <T>               请求体类型
//     * @return
//     */
//    public <T> Crawler doPost(String url, T requestBodyEntity, BiConsumer<byte[], Crawler> respBytes) {
//        int tryTimes = this.crawlerConf.getTryTimes();
//        return doPost(url, requestBodyEntity, tryTimes, respBytes);
//    }
//
//    /**
//     * post 请求
//     *
//     * @param url               请求链接
//     * @param requestBodyEntity 请求体实体类
//     * @param tryTimes          失败重试次数
//     * @param <T>               请求体类型
//     * @param respBytes         相应返回的字节数组处理函数
//     */
//    public <T> Crawler doPost(String url, T requestBodyEntity, int tryTimes, BiConsumer<byte[], Crawler> respBytes) {
//        try {
//            RequestBody requestBody = RequestBody.create
//                    (MediaType.parse("application/json; charset=utf-8"),
//                            JSON.toJSONString(requestBodyEntity));
//
//            final Request request = new Request.Builder()
//                    .url(url)
//                    .post(requestBody)
//                    .build();
//
//            log.info("doPost url call before --::{}", url);
//            final Call call = this.crawlerConf.getOkHttpClient().newCall(request);
//            Response execute = call.execute();
//            ResponseBody body = execute.body();
//            if (Objects.isNull(body)) {
//                log.warn("Crawler doPost requestBodyEntity body is null!");
//                return this;
//            }
//            try (InputStream inputStream = body.byteStream()) {
//                MediaType mediaType = body.contentType();
//                long contentLength = body.contentLength();
//                byte[] bytes = IOUtils.toByteArray(inputStream);
//                if (Objects.nonNull(respBytes)) {
//                    respBytes.accept(bytes, this);
//                    return this;
//                }
//                final RespBody respBody = new RespBody();
//                respBody.setBodyDatas(bytes);
//                respBody.setCharset(mediaType);
//                respBody.setContentLength(contentLength);
//                respBody.setBaseUri(url);
//                respBody.setRequestUrl(url);
//
//                this.crawlerConf.getRespBodys().add(respBody);
//
//            }
//        } catch (IOException e) {
//            //e.printStackTrace();
//            String message = e.getMessage();
//            log.error("Crawler doPost requestBodyEntity Exception message,tryTimes ---::{},{}", message, tryTimes);
//            if (tryTimes > 0) {
//                long reTryInterval = this.crawlerConf.getReTryInterval();
//                try {
//                    Thread.sleep(reTryInterval);
//                } catch (InterruptedException interruptedException) {
//                    interruptedException.printStackTrace();
//                }
//                return doPost(url, requestBodyEntity, --tryTimes, respBytes);
//            }
//        }
//        return this;
//    }
//
//    /**
//     * 删除第一个请求响应体
//     */
//    public Crawler removeFirstRespBody() {
//        this.crawlerConf.getRespBodys().remove(0);
//        return this;
//    }
//
//    /**
//     * 删除最后一个请求响应体
//     */
//    public Crawler removeLastRespBody() {
//        this.crawlerConf.getRespBodys().remove(this.crawlerConf.getRespBodys().size() - 1);
//        return this;
//    }
//
//    /**
//     * 删除请求体
//     *
//     * @param requestIndex 请求顺序下标(从0开始)
//     */
//    public Crawler removeRespBody(int requestIndex) {
//        this.crawlerConf.getRespBodys().remove(requestIndex);
//        return this;
//    }
//
//    /**
//     * 删除第一个 web document 文档缓存
//     */
//    public Crawler removeFirstDocument() {
//        this.crawlerConf.getDocuments().remove(0);
//        return this;
//    }
//
//    /**
//     * 删除最后一个 web document 文档缓存
//     */
//    public Crawler removeLastDocument() {
//        this.crawlerConf.getDocuments().remove(this.crawlerConf.getDocuments().size() - 1);
//        return this;
//    }
//
//    /**
//     * 删除一个 web document 文档缓存
//     *
//     * @param requestIndex 请求顺序下标(从0开始)
//     */
//    public Crawler removeDocument(int requestIndex) {
//        this.crawlerConf.getDocuments().remove(requestIndex);
//        return this;
//    }
//
//    /**
//     * 构建 web document (最早一次请求获取到的文档数据转换)
//     */
//    public Crawler makeFirstWebDocument() {
//        return makeWebDocument(0);
//    }
//
//    /**
//     * 构建 web document (最近一次请求获取到的文档数据转换)
//     */
//    public Crawler makeLastWebDocument() {
//        List<RespBody> respBodys = this.crawlerConf.getRespBodys();
//        int requestIndex = respBodys.size() - 1;
//        return makeWebDocument(requestIndex);
//    }
//
//    /**
//     * 构建 web document，指定请求的的顺序下标(从0开始)
//     *
//     * @param requestIndex 请求顺序下标(从0开始)
//     */
//    public Crawler makeWebDocument(int requestIndex) {
//        List<RespBody> respBodys = this.crawlerConf.getRespBodys();
//        RespBody respBody = respBodys.get(requestIndex);
//        String baseUri = respBody.getBaseUri();
//        String result = getResult(requestIndex);
//        Document document = Jsoup.parse(result, baseUri);
//        this.crawlerConf.getDocuments().add(document);
//        return this;
//    }
//
//    /**
//     * 获取web document文档
//     *
//     * @param docIndex         文档下标
//     * @param documentConsumer 文档消费函数
//     */
//    public Crawler document(int docIndex, Consumer<Document> documentConsumer) {
//        List<Document> documents = this.crawlerConf.getDocuments();
//        documentConsumer.accept(documents.get(docIndex));
//        return this;
//    }
//
//    /**
//     * 获取web document文档
//     *
//     * @param documentConsumer 文档消费函数
//     */
//    public Crawler document(Consumer<Document> documentConsumer) {
//        List<Document> documents = this.crawlerConf.getDocuments();
//        int docIndex = documents.size() - 1;
//        return document(docIndex, documentConsumer);
//    }
//
//    /**
//     * 获取web document文档
//     *
//     * @param docIndex                  文档下标
//     * @param documentCrawlerBiConsumer 文档消费函数
//     */
//    public Crawler document(int docIndex, BiConsumer<Document, Crawler> documentCrawlerBiConsumer) {
//        List<Document> documents = this.crawlerConf.getDocuments();
//        documentCrawlerBiConsumer.accept(documents.get(docIndex), this);
//        return this;
//    }
//
//    /**
//     * 获取web document文档
//     *
//     * @param documentCrawlerBiConsumer 文档消费函数
//     */
//    public Crawler document(BiConsumer<Document, Crawler> documentCrawlerBiConsumer) {
//        List<Document> documents = this.crawlerConf.getDocuments();
//        int docIndex = documents.size() - 1;
//        return document(docIndex, documentCrawlerBiConsumer);
//    }
//
//    /**
//     * 对 web document 进行 css选择器选择
//     *
//     * @param docIndex    web document 文档下标(从0开始)
//     * @param cssQuery    css 选择器
//     * @param eleConsumer elements 消费函数
//     */
//    public Crawler docSelect(int docIndex, String cssQuery, BiConsumer<Elements, Crawler> eleConsumer) {
//        List<Document> documents = this.crawlerConf.getDocuments();
//        Document document = documents.get(docIndex);
//        Elements elements = document.select(cssQuery);
//        eleConsumer.accept(elements, this);
//        return this;
//    }
//
//    /**
//     * 对 web document 进行 css选择器选择 （选择最后一个文档）
//     *
//     * @param cssQuery    css 选择器
//     * @param eleConsumer elements 消费函数
//     */
//    public Crawler docSelect(String cssQuery, BiConsumer<Elements, Crawler> eleConsumer) {
//        List<Document> documents = this.crawlerConf.getDocuments();
//        int docIndex = documents.size() - 1;
//        return docSelect(docIndex, cssQuery, eleConsumer);
//    }
//
//    /**
//     * 对 web document 的 elements 进行选择
//     *
//     * @param elements    web document 的 elements
//     * @param cssQuery    css 选择器
//     * @param eleConsumer 新的elements 消费函数
//     */
//    public Crawler eleSelect(Elements elements, String cssQuery, BiConsumer<Elements, Crawler> eleConsumer) {
//        Elements elements1 = elements.select(cssQuery);
//        eleConsumer.accept(elements1, this);
//        return this;
//    }
//
//    /**
//     * 获取元素中文字中包含指定字符串的元素列表
//     *
//     * @param elements    Elements
//     * @param containText 指定字符串
//     * @return 元素列表
//     */
//    public List<Element> element(Elements elements, String... containText) {
//        List<Element> eles = new ArrayList<>();
//        if (Objects.isNull(elements)) {
//            return eles;
//        }
//        for (Element element : elements) {
//            String text = element.text();
//
//            boolean hasText = true;
//            for (String ct : containText) {
//                boolean b = StringUtils.contains(text, ct);
//                if (!b) {
//                    hasText = false;
//                    break;
//                }
//            }
//            if (hasText) {
//                eles.add(element);
//            }
//        }
//        return eles;
//    }
//
//    /**
//     * 获取元素中文字中包含指定字符串的元素列表的第一个
//     *
//     * @param elements    Elements
//     * @param containText 指定字符串
//     * @return 第一个元素，如果不存在则返回null
//     */
//    public Element firstElement(Elements elements, String... containText) {
//        List<Element> eles = this.element(elements, containText);
//        return eles.isEmpty() ? null : eles.get(0);
//    }
//
//    /**
//     * 获取 web document 的 elements attr 属性值
//     *
//     * @param elements     web document 的 elements
//     * @param attributeKey 属性名称
//     * @param attrConsumer 属性值消费函数
//     */
//    public Crawler eleAttr(Elements elements, String attributeKey, BiConsumer<String, Crawler> attrConsumer) {
//        String attr = elements.attr(attributeKey);
//        attrConsumer.accept(attr, this);
//        return this;
//    }
//
//    /**
//     * 设置下一页请求的 url
//     *
//     * @param nextPageUrl 下一页url
//     */
//    public Crawler setNextPageUrl(String nextPageUrl) {
//        this.crawlerConf.setNextPageUrl(nextPageUrl);
//        return this;
//    }
//
//    /**
//     * 获取下一页请求url
//     */
//    public String getNextPage() {
//        String nextPageUrl = this.crawlerConf.getNextPageUrl();
//        this.crawlerConf.setNextPageUrl(null);
//        return nextPageUrl;
//    }
//
//    /**
//     * 是否有下一页
//     */
//    public boolean hasNextPage() {
//        String nextPageUrl = this.crawlerConf.getNextPageUrl();
//        return StringUtils.isNotBlank(nextPageUrl);
//    }
//
//    /**
//     * 获取响应体值（字符串），（最后一次请求结果的响应体）
//     */
//    public String getResult() {
//        List<RespBody> respBodys = this.crawlerConf.getRespBodys();
//        int requestIndex = respBodys.size() - 1;
//        return getResult(requestIndex);
//    }
//
//    /**
//     * 获取响应体值（字符串），（最后一次请求结果的响应体）
//     * 并且释放最后一次响应体的缓存
//     */
//    public String getResultAndReleaseRespBody() {
//        String result = getResult();
//        this.removeLastRespBody();
//        return result;
//    }
//
//    /**
//     * 获取响应体值（字符串）
//     *
//     * @param requestIndex 请求顺序下标(从0开始)
//     */
//    public String getResult(int requestIndex) {
//        List<RespBody> respBodys = this.crawlerConf.getRespBodys();
//        RespBody respBody = respBodys.get(requestIndex);
//        byte[] bodyDatas = respBody.getBodyDatas();
//        Charset charset = respBody.getCharset();
//        return new String(bodyDatas, charset);
//    }
//
//    /**
//     * 获取响应体值（字符串）
//     * 并且释放响应体的缓存
//     *
//     * @param requestIndex 请求顺序下标(从0开始)
//     */
//    public String getResultAndReleaseRespBody(int requestIndex) {
//        String result = getResult(requestIndex);
//        this.removeRespBody(requestIndex);
//        return result;
//    }
//
//    /**
//     * 获取响应体值 字节数组
//     */
//    public byte[] getBytesResult() {
//        List<RespBody> respBodys = this.crawlerConf.getRespBodys();
//        int requestIndex = respBodys.size() - 1;
//        return getBytesResult(requestIndex);
//    }
//
//    /**
//     * 获取响应体值 字节数组
//     * 并且释放响应体的缓存
//     */
//    public byte[] getBytesResultAndReleaseRespBody() {
//        byte[] bytesResult = getBytesResult();
//        this.removeLastRespBody();
//        return bytesResult;
//    }
//
//    /**
//     * 获取响应体值 字节数组
//     *
//     * @param requestIndex 请求顺序下标(从0开始)
//     */
//    public byte[] getBytesResult(int requestIndex) {
//        List<RespBody> respBodys = this.crawlerConf.getRespBodys();
//        RespBody respBody = respBodys.get(requestIndex);
//        return respBody.getBodyDatas();
//    }
//
//    /**
//     * 获取响应体值 字节数组
//     * 并且释放响应体的缓存
//     *
//     * @param requestIndex 请求顺序下标(从0开始)
//     */
//    public byte[] getBytesResultAndReleaseRespBody(int requestIndex) {
//        byte[] bytesResult = getBytesResult(requestIndex);
//        this.removeRespBody(requestIndex);
//        return bytesResult;
//    }
//
//    /**
//     * 获取响应体值（泛型对象），通过 JSON.parseObject(result, clazz)
//     *
//     * @param clazz 指定类型的 class
//     * @param <T>   泛型
//     */
//    public <T> T getResult(Class<T> clazz) {
//        List<RespBody> respBodys = this.crawlerConf.getRespBodys();
//        int requestIndex = respBodys.size() - 1;
//        return getResult(requestIndex, clazz);
//    }
//
//    /**
//     * 获取响应体值（泛型对象），通过 JSON.parseObject(result, clazz)
//     * 并且释放响应体的缓存
//     *
//     * @param clazz 指定类型的 class
//     * @param <T>   泛型
//     */
//    public <T> T getResultAndReleaseRespBody(Class<T> clazz) {
//        T result = getResult(clazz);
//        this.removeLastRespBody();
//        return result;
//    }
//
//    /**
//     * 获取响应体值（泛型对象），通过 JSON.parseObject(result, clazz)
//     *
//     * @param requestIndex 请求顺序下标(从0开始)
//     * @param clazz        指定类型的 class
//     * @param <T>          泛型
//     */
//    public <T> T getResult(int requestIndex, Class<T> clazz) {
//        String result = getResult(requestIndex);
//        return JSON.parseObject(result, clazz);
//    }
//
//    /**
//     * 获取响应体值（泛型对象），通过 JSON.parseObject(result, clazz)
//     * 并且释放响应体的缓存
//     *
//     * @param requestIndex 请求顺序下标(从0开始)
//     * @param clazz        指定类型的 class
//     * @param <T>          泛型
//     */
//    public <T> T getResultAndReleaseRespBody(int requestIndex, Class<T> clazz) {
//        T result = getResult(requestIndex, clazz);
//        this.removeRespBody(requestIndex);
//        return result;
//    }
//
//}
