package net.liveopenmarket.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpChannelHandler extends SimpleChannelInboundHandler<HttpRequest>{

	private static final Logger logger = LoggerFactory.getLogger(HttpChannelHandler.class);
	
	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
		//logger.info("channel read is complete");
        ctx.flush();
    }
	
	@Override
	protected void messageReceived(final ChannelHandlerContext ctx, final HttpRequest request) throws Exception {
		
		logger.info("message received {}", request.toString());
		
		if (HttpHeaders.is100ContinueExpected(request)){
			ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
		}
		
        boolean keepAlive = HttpHeaders.isKeepAlive(request);

        final ByteBuf content = ctx.alloc().buffer();
        content.writeBytes(new String("default response").getBytes());
//        content.writeBytes(HelloWorldHttp2Handler.RESPONSE_BYTES.duplicate());
//
//        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
//        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
//        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
//
//        if (!keepAlive) {
//            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
//        } else {
//            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
//            ctx.writeAndFlush(response);
//        }
        final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        ctx.writeAndFlush(response);
	}
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
