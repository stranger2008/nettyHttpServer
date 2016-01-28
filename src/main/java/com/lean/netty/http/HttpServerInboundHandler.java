package com.lean.netty.http;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.EXPIRES;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class HttpServerInboundHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HttpServerInboundHandler.class);

    private static final Pattern SEND_TASK_FOR_METHOD_GET_PATTERN = Pattern
            .compile("/dmap-sf/query(?:\\?.*)?");

    private static final Pattern SEND_TASK_FOR_METHOD_POST_PATTERN = Pattern
            .compile("/dmap-sf/sendMsg(?:\\?.*)?");

    private HttpRequest request;
    private boolean isGet;
    private boolean isPost;

    /**
     * POST: http://localhost:8844/dmap-sf/sendMsg?hello=df&world=women body: we
     * are togather
     *
     * GET: http://localhost:8844/dmap-sf/query?hello=df&world=women
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;

            String uri = request.getUri();
            HttpMethod method = request.getMethod();

            isGet = method.equals(HttpMethod.GET);
            isPost = method.equals(HttpMethod.POST);

            System.out.println(String.format("Uri:%s method %s", uri, method));

            if (SEND_TASK_FOR_METHOD_GET_PATTERN.matcher(uri).matches()
                    && isGet) {
                System.out.println("doing something here.");
                String param = "hello";
                String str = getParamerByNameFromGET(param);
                System.out.println(param + ":" + str);
            }
            if (SEND_TASK_FOR_METHOD_POST_PATTERN.matcher(uri).matches()
                    && isPost) {
                System.out.println("doing something here.");
            } else {
                String responseString = JSON.toJSONString(AjaxResult.FAILURE
                        .setMessage(String.format(
                                "Cann't find the url:%s for method:%s", uri,
                                method.name())));

                writeHttpResponse(responseString, ctx, NOT_FOUND);
                return;
            }

        }

        if (!isGet) {
            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;

                ByteBuf buf = content.content();
                String bodyString = buf.toString(Charsets.UTF_8);

                System.out.println("body: " + bodyString);

                String l = getParamerByNameFromPOST("hello", bodyString);
                System.out.println(l);

                buf.release();
                String responseString = JSON.toJSONString(AjaxResult.SUCCESS);
                writeHttpResponse(responseString, ctx, OK);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error(cause.getMessage());
        ctx.close();
    }

    private String getParamerByNameFromGET(String name) {
        QueryStringDecoder decoderQuery = new QueryStringDecoder(
                request.getUri());
        return getParameterByName(name, decoderQuery);
    }

    private String getParamerByNameFromPOST(String name, String body) {
        QueryStringDecoder decoderQuery = new QueryStringDecoder("some?" + body);
        return getParameterByName(name, decoderQuery);
    }

    /**
     * <pre>
     * @param name
     * @param decoderQuery
     * @return
     * </pre>
     */
    private String getParameterByName(String name,
                                      QueryStringDecoder decoderQuery) {
        Map<String, List<String>> uriAttributes = decoderQuery.parameters();
        for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
            String key = attr.getKey();
            for (String attrVal : attr.getValue()) {
                if (key.equals(name)) {
                    return attrVal;
                }
            }
        }
        return null;
    }

    private void writeHttpResponse(String responseString, ChannelHandlerContext ctx, HttpResponseStatus status)
            throws UnsupportedEncodingException {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.wrappedBuffer(responseString.getBytes(Charsets.UTF_8)));
        response.headers().set(CONTENT_TYPE, "text/json");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(EXPIRES, 0);
        if (HttpHeaders.isKeepAlive(request)) {
            response.headers().set(CONNECTION, Values.KEEP_ALIVE);
        }
        ctx.write(response);
        ctx.flush();
    }

}
