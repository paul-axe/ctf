package pwn.challenge;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

public class Sender {

    public class Handler extends SimpleChannelInboundHandler<String> {
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		}
    }

    private String path;
    public Sender(String username){
        path = Utils.getUserSocketPath(username);
    }

    public void send(byte[] data) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        EpollEventLoopGroup group = new EpollEventLoopGroup();
        try {
            bootstrap
                .group(group)
                .channel(EpollDomainSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                         @Override
                         protected void initChannel(Channel ch) throws Exception {
                             ChannelPipeline pipeline = ch.pipeline();
                             pipeline.addLast("encoder", new ByteArrayEncoder());
                             pipeline.addLast("handler", new Handler());
                         }
                });

            final Channel channel = bootstrap.connect(
                    new DomainSocketAddress(this.path)
                    ).sync().channel();

            channel.write(data);
            channel.flush();
            channel.disconnect().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}

