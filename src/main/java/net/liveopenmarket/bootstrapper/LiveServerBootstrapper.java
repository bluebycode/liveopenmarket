package net.liveopenmarket.bootstrapper;

import io.netty.channel.ChannelHandler;

import javax.servlet.ServletContextListener;

import net.liveopenmarket.server.HttpChannelHandler;
import net.liveopenmarket.server.HttpChannelHandler.TestService;
import net.liveopenmarket.server.NettyHttpServer.OnContextChannelInitializer;
import net.liveopenmarket.server.HttpChannelApplicationContext;
import net.liveopenmarket.server.NettyHttpServer;
import net.liveopenmarket.server.ServerConfiguration;
import net.liveopenmarket.servlet.guice.GuiceServletModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.servlet.GuiceServletContextListener;

public final class LiveServerBootstrapper extends GuiceServletContextListener {

	private Injector injector;
	
	public static interface ChannelHandlerFactory {
		ChannelHandler createHttpHandler(TestService service);
	}
	
	@Override
	protected Injector getInjector() {
		return injector;
	}
	
	public static class AppGuiceContextListener extends GuiceServletContextListener {

		protected ServletContextListener getContext(){
			return this;
		}
		
		@Override
		protected Injector getInjector() {
			final Injector injector = Guice.createInjector(
					new GuiceServletModule(),
					new AbstractModule() {
						@Override
						protected void configure() {
							bind(HttpChannelHandler.TestService.class)
								.toInstance(new HttpChannelHandler.TestService("singletonTest"));
							install(new FactoryModuleBuilder()
								.implement(ChannelHandler.class, HttpChannelHandler.class)
						    	.build(ChannelHandlerFactory.class));
						}
					},
					new AbstractModule() {
						@Override
						protected void configure() {
							bind(OnContextChannelInitializer.class);
							bind(HttpChannelApplicationContext.class);
							bind(ServletContextListener.class).toInstance(getContext());
							bind(ServerConfiguration.class).toProvider(new Provider<ServerConfiguration>(){
								@Override
								public ServerConfiguration get() {
									return new ServerConfiguration();
								}
							});
						}
			});
			return injector;
		}
	}

	public static void main(String[] args) throws Exception {
		
		final AppGuiceContextListener ctx = new AppGuiceContextListener();
		final NettyHttpServer server = ctx.getInjector().getInstance(NettyHttpServer.class);
		server.startAsync();
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				server.stopAsync();
			}
		});
		
	}

}
