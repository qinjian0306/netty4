package com.smr.pc.netty.serverdemo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * ChannelInboundHandlerAdapter实现自ChannelInboundHandler
 * ChannelInboundHandler提供了不同的事件处理方法你可以重写
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        //Discard the received data silently
        //ByteBuf是一个引用计数对象实现ReferenceCounted，他就是在有对象引用的时候计数+1，无的时候计数-1，当为0对象释放内存

//        ByteBuf in=(ByteBuf)msg;
        try {
//            while(in.isReadable()){
//                System.out.println((char)in.readByte());
//                System.out.flush();
//            }

            // 收到客户端消息
            System.out.println("来自客户端: " + ctx.channel().remoteAddress() + " 的消息 : " + msg);

            // 返回客户端消息
            ctx.writeAndFlush("Received your message !\n");

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 释放资源 很种药
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 本方法用作处理异常
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 信息获取完毕后操作
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }


}
