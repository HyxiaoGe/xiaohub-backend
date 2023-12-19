package com.hyxiao;

import com.hyxiao.core.NettyServer;

/**
 * Hello world!
 *
 */
public class NettyApplication
{
    public static void main( String[] args )
    {
        NettyServer server = new NettyServer();
        try {
            server.start();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
