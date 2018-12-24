package com.smr.pc.netty.websocket;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 全局ChannelGroup类
 *
 * @author QJ
 * @date 2018/12/21
 *
 */
public class Global {

    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static ChannelGroup spotwsGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static ChannelGroup futurewsGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static ChannelGroup mktwsGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

}
