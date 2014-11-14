package net.liveopenmarket.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContextListener;

import net.liveopenmarket.server.NettyHttpServer.OnContextChannelInitializer;

@Singleton
public class HttpChannelApplicationContext {

	private final ServletContextListener contextListener;
	private final OnContextChannelInitializer channelInitializer;
	
	@Inject
	public HttpChannelApplicationContext(final ServletContextListener contextListener, final OnContextChannelInitializer channelInitializer){
		this.contextListener = contextListener;
		this.channelInitializer = channelInitializer;
	}
	
	public ChannelInitializer<SocketChannel> getChannelInitializer(){
		return this.channelInitializer;
	}
}
