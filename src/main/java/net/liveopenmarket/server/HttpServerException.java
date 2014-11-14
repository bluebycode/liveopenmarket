package net.liveopenmarket.server;

public class HttpServerException extends Exception {
	public HttpServerException(final Exception e){
		super(e);
	}
}
