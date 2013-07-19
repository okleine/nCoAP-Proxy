package de.uniluebeck.itm.ncoap.proxy;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
import de.uniluebeck.itm.ncoap.proxy.core.HttpPipelineFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 19.07.13
 * Time: 19:58
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    public static int HTTP_PROXY_PORT = 8081;
    public static int COAP_PROXY_PORT = 8082;

    public static void main(String[] args){
        //Create HTTP channel
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()
        ));

        HttpPipelineFactory pipelineFactory = new HttpPipelineFactory();
        bootstrap.setPipelineFactory(pipelineFactory);

        bootstrap.bind(new InetSocketAddress(HTTP_PROXY_PORT));

        //Create CoAP Applications
        CoapServerApplication coapServerApplication = new CoapServerApplication(COAP_PROXY_PORT);

        CoapClientApplication coapClientApplication = new CoapClientApplication();
    }
}
