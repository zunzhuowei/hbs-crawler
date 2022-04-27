package com.hbs.core.enties;

import okhttp3.MediaType;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Created by zun.wei on 2021/1/24.
 */
class RespBody implements Serializable {


    private static final long serialVersionUID = -9124339656834845933L;

    /**
     * 请求返回的字节数组
     */
    byte[] bodyDatas;

    /**
     * 编码格式
     */
    Charset charset = StandardCharsets.UTF_8;

    /**
     * 内容长度
     */
    long contentLength;

    /**
     * 请求的链接 不包含参数
     */
    String baseUri;

    /**
     * 请求的 url 包含完整参数
     */
    String requestUrl;

    /**
     * 设置响应体的编码
     * @param mediaType MediaType
     */
    RespBody setCharset(MediaType mediaType) {
        Charset charset;
        if (Objects.isNull(mediaType)) {
            charset = StandardCharsets.UTF_8;
        } else {
            charset = mediaType.charset();
            if (Objects.isNull(charset)) {
                charset = StandardCharsets.UTF_8;
            }
        }
        this.charset = charset;
        return this;
    }

}
