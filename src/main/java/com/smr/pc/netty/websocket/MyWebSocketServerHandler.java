package com.smr.pc.netty.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 自定义Handler处理类
 *
 * @author QJ
 * @date 2018/12/21
 *
 */
@ChannelHandler.Sharable
public class MyWebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = Logger.getLogger(WebSocketServerHandshaker.class.getName());
    private WebSocketServerHandshaker handshaker;

    private static ConcurrentHashMap<String,Channel> channelsMap = new ConcurrentHashMap<String,Channel>();

    /**
     *  接受到客户端消息,处理业务逻辑
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 传统的HTTP接入
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, ((FullHttpRequest) msg));
        }

        // WebSocket接入
        else if (msg instanceof WebSocketFrame) {
            handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 添加
        Global.group.add(ctx.channel());
        System.out.println(ctx.channel().id() + " : 客户端与服务端连接开启");
    }

    /**
     * 断开连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 移除
        Global.group.remove(ctx.channel());
        System.out.println(ctx.channel().id() + " : 客户端与服务端连接关闭");
    }

    /**
     * 读完成后的操作
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 异常事件
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("发生异常");
        cause.printStackTrace();
        ctx.close();
    }


    /**-------------------------------------用户请求处理-------------------------------------**/


    /**
     * 处理Socket请求
     *
     * @param ctx
     * @param frame
     */
    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame
                    .retain());
            System.out.println("关闭socket");
        }
        // 判断是否ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(
                    new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 本例程仅支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            System.out.println("本例程仅支持文本消息，不支持二进制消息");
            throw new UnsupportedOperationException(String.format(
                    "%s frame types not supported", frame.getClass().getName()));
        }

        // routing 消息来源哪个socket

        System.out.println("Routing ============ ");
        if("spotws".equals(ctx.attr(AttributeKey.valueOf("route")).get())){


            // spotws的处理


            System.out.println("Routing spotws .. ");
        }else if("futurews".equals(ctx.attr(AttributeKey.valueOf("route")).get())){
            System.out.println("Routing futurews .. ");


            // futurews的处理


        }
        System.out.println("Routing ============ ");







        // 返回应答消息
        String request = ((TextWebSocketFrame) frame).text();
        System.out.println("服务端收到：" + request);
        if (logger.isLoggable(Level.FINE)) {
            logger
                    .fine(String.format("%s received %s", ctx.channel(),
                            request));
        }
        TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()
                + ctx.channel().id() + "：" + request);
        // 群发
//        Global.group.writeAndFlush(tws);
        // 返回【谁发的发给谁】
        ctx.channel().writeAndFlush(tws);
    }

    /**
     * 处理Http请求，完成WebSocket握手
     *
     * @param ctx
     * @param req
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {

        System.out.println("HTTP握手...");

        if (!req.getDecoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }


        // ws分发处理 routing
        HttpMethod method=req.getMethod();
        String uri=req.getUri();
        if(method==HttpMethod.GET&&uri.contains("spotws")){
            //....处理    重点在这里，对于URL的不同，给ChannelHandlerContext设置一个Attribut
            ctx.attr(AttributeKey.valueOf("route")).set("spotws");
        }else if(method==HttpMethod.GET&&uri.contains("futurews")){
            //...处理
            ctx.attr(AttributeKey.valueOf("route")).set("futurews");
        }




        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                "ws://localhost:8888/", null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {// 无法处理的websocket版本
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {// 向客户端发送websocket握手,完成握手
            // 记录管道处理上下文，便于服务器推送数据到客户端
            handshaker.handshake(ctx.channel(), req);


//            String uri = req.getUri();
//
//            String token = uri.substring(uri.lastIndexOf("/") + 1);
//
//            channelsMap.put(token,ctx.channel());




        }
    }

    /**
     * Http返回
     *
     * @param ctx
     * @param req
     * @param res
     */
    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(),
                    CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static boolean isKeepAlive(FullHttpRequest req) {
        return false;
    }
}
