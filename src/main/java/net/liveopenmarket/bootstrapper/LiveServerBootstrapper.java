package net.liveopenmarket.bootstrapper;

import net.liveopenmarket.server.NettyHttpServer;
import net.liveopenmarket.server.ServerConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.servlet.GuiceServletContextListener;

public final class LiveServerBootstrapper extends GuiceServletContextListener {

	private Injector injector;
	
	@Override
	protected Injector getInjector() {
		return injector;
	}

	public static void main(String[] args) throws Exception {
		
		final Injector injector = Guice.createInjector(new AbstractModule(){
			@Override
			protected void configure() {
				bind(ServerConfiguration.class).toProvider(new Provider<ServerConfiguration>(){
					@Override
					public ServerConfiguration get() {
						return new ServerConfiguration();
					}
				});
			}
		});
		
		final NettyHttpServer server = injector.getInstance(NettyHttpServer.class);
		server.startAsync();
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				server.stopAsync();
			}
		});
		
	}

}
