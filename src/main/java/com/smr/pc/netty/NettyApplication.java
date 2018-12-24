package com.smr.pc.netty;

import com.smr.pc.netty.websocket.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);

        // 启动netty服务
        new NettyServer(8888).start();
    }

}

