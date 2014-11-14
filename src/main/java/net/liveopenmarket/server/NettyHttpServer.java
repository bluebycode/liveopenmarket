package net.liveopenmarket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.liveopenmarket.bootstrapper.LiveServerBootstrapper.ChannelHandlerFactory;
import net.liveopenmarket.server.HttpChannelHandler.TestService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Provider;

public class NettyHttpServer extends AbstractIdleService {

	private static final Logger logger = LoggerFactory.getLogger(NettyHttpServer.class);

	private EventLoopGroup acceptors, workers;
	private final ServerBootstrap server;
	private final Provider<ServerConfiguration> configuration;
	private final HttpChannelApplicationContext ctx;

	@Inject
	public NettyHttpServer(final Provider<ServerConfiguration> configuration, HttpChannelApplicationContext ctx) {
		this.server = new ServerBootstrap();
		this.configuration = configuration;
		this.ctx = ctx;
	}

	ThreadFactory getAcceptorsFactory() {
		return new DefaultThreadFactory("acceptor");
	}
	
	ThreadFactory getWorkersFactory() {
		return new DefaultThreadFactory("worker");
	}
	
	@Singleton
	public static class OnContextChannelInitializer extends ChannelInitializer<SocketChannel> {
		
		@Inject
		private TestService service;
		
		@Inject
		private ChannelHandlerFactory channelHandlerFactory;
		
		public OnContextChannelInitializer(){
			logger.info("initializer"+this);
		}
		
		@Override
		protected void initChannel(final SocketChannel channel) throws Exception {						
			final ChannelPipeline pipeline = channel.pipeline();
			pipeline.addLast("decoder", new HttpRequestDecoder());
			pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
			pipeline.addLast("encoder", new HttpResponseEncoder());
			pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
			pipeline.addLast("handler", channelHandlerFactory.createHttpHandler(service));
		}
	}
	
	@Override
	protected void startUp() throws Exception {
		logger.info("Starting server...");

		this.acceptors = new NioEventLoopGroup(0, getAcceptorsFactory());
		this.workers = new NioEventLoopGroup(0, getWorkersFactory());

		ChannelFuture serverChannel = null;
		try {
			server.group(acceptors, workers)
					.channel(NioServerSocketChannel.class)
					.childHandler(ctx.getChannelInitializer())
//					.childHandler(new ChannelInitializer<SocketChannel>() {
//						@Override
//						protected void initChannel(final SocketChannel channel) throws Exception {						
//							final ChannelPipeline pipeline = channel.pipeline();
//							pipeline.addLast("decoder", new HttpRequestDecoder());
//							pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
//							pipeline.addLast("encoder", new HttpResponseEncoder());
//							// enable compression?
//							// pipeline.addLast("deflated", new HttpContentCompressor(0.9));
//							pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
//							pipeline.addLast("handler", new HttpChannelHandler());
//						}
//					})
					.childOption(ChannelOption.TCP_NODELAY,true)
					.childOption(ChannelOption.SO_KEEPALIVE,true);
			
			final ServerConfiguration conf = configuration.get();
			final InetSocketAddress address = new InetSocketAddress(conf.host, conf.port);
			serverChannel = server.bind(address).sync();
			
			logger.info("Listening under address {}", address);
			serverChannel.channel().closeFuture().sync();
			
		} catch (final InterruptedException e) {
			logger.error("Server connection was interrupted {}", e.getMessage(), e);
			throw new HttpServerException(e);
		} finally {
			// nothing...
		}
	}

	@Override
	protected void shutDown() throws Exception {
		logger.info("Stopping server...");
		if (acceptors != null) {
			acceptors.shutdownGracefully().sync();
		}
		if (workers != null) {
			workers.shutdownGracefully().sync();
		}
	}

}
