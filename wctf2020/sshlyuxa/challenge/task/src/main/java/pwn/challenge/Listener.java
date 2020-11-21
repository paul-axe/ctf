package pwn.challenge;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.unix.DomainSocketAddress;

import java.io.File;
import java.net.SocketAddress;

public class Listener extends Thread {
    private SocketAddress socket;

    public class MessageHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.duplicate().readBytes(bytes);
            System.out.println("\n[!] Incoming message: " + Utils.bytesToHex(bytes));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

    public Listener(String username) {
        String path = Utils.getUserSocketPath(username);
        this.socket = new DomainSocketAddress(path);
    }

    public void run() {
        EventLoopGroup bossGroup = new EpollEventLoopGroup();
        EventLoopGroup workerGroup = new EpollEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(EpollServerDomainSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    public void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new MessageHandler());
                    }
                });
        try {
            ChannelFuture f = b.bind(this.socket);
            f.channel().closeFuture().sync();
        } catch (java.lang.InterruptedException e) {
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


}
