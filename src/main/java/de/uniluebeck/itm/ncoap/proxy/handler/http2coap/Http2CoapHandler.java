package de.uniluebeck.itm.ncoap.proxy.handler.http2coap;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 19.07.13
 * Time: 20:19
 * To change this template use File | Settings | File Templates.
 */
public class Http2CoapHandler extends SimpleChannelUpstreamHandler {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent me){
        if(!(me.getMessage() instanceof HttpRequest)){
            ctx.sendUpstream(me);
            return;
        }
    }
}
