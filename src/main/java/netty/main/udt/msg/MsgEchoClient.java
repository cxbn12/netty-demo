package netty.main.udt.msg;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import netty.handler.udt.msg.MsgEchoClientHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UDT Message Flow client
 * Sends one message when a connection is open and echoes back any received data
 * to the server. Simply put, the echo client initiates the ping-pong traffic
 * between the echo client and server by sending the first message to the
 * server.
 * use UDT in UDP-like message delivery mode
 * warn: netty 4.1.12 UDT transport is no longer 
 * maintained and will be removed
 * see #netty.main.udt.package-info
 * @author donald
 * 2017年7月1日
 * 下午4:34:20
 */
public final class MsgEchoClient {
    private static final Logger log = LoggerFactory.getLogger(MsgEchoClient.class);
    public static final String ip = System.getProperty("host", "10.16.7.107");
    static final int port = Integer.parseInt(System.getProperty("port", "10020"));
    public static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    @SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {

        // Configure the client.
        final ThreadFactory connectFactory = new DefaultThreadFactory("connect");
        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(1,
                connectFactory, NioUdtProvider.MESSAGE_PROVIDER);
        try {
            final Bootstrap boot = new Bootstrap();
            boot.group(connectGroup)
                    .channelFactory(NioUdtProvider.MESSAGE_CONNECTOR)
                    .handler(new ChannelInitializer<UdtChannel>() {
                        @Override
                        public void initChannel(final UdtChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(
                                    new LoggingHandler(LogLevel.INFO),
                                    new MsgEchoClientHandler());
                        }
                    });
            InetSocketAddress inetSocketAddress = new InetSocketAddress(ip,port);
            log.info("=========UDT Client is start=========");
            // Start the client.
            final ChannelFuture f = boot.connect(inetSocketAddress).sync();
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down the event loop to terminate all threads.
            connectGroup.shutdownGracefully();
        }
    }
}
