package de.uniluebeck.itm.ncoap.proxy;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
import de.uniluebeck.itm.ncoap.message.options.Option;
import de.uniluebeck.itm.ncoap.message.options.OptionRegistry;
import de.uniluebeck.itm.ncoap.message.options.UintOption;
import de.uniluebeck.itm.ncoap.proxy.core.HttpPipelineFactory;
import de.uniluebeck.itm.ncoap.proxy.handler.http2coap.HttpHeader2CoapOption;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpHeaders;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 19.07.13
 * Time: 19:58
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    private static Logger log = Logger.getLogger(Main.class.getName());

    public static int HTTP_PROXY_PORT = 8081;
    public static int COAP_PROXY_PORT = 8082;

    private static void initializeLogging(){
        String pattern = "%-23d{yyyy-MM-dd HH:mm:ss,SSS} | %-32.32t | %-30.30c{1} | %-5p | %m%n";
        PatternLayout patternLayout = new PatternLayout(pattern);

        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(new ConsoleAppender(patternLayout));

        Logger.getRootLogger().setLevel(Level.OFF);
        Logger.getLogger("de.uniluebeck.itm.ncoap.proxy").setLevel(Level.DEBUG);
    }

    public static void main(String[] args){
        initializeLogging();

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

        List<Option> coapOptions = HttpHeader2CoapOption.getOptions(HttpHeaders.Names.ACCEPT,
                "application/rdf+xml;q=0.5 , text/plain; q=0.1, text/html;level=1, */*");

        if(log.isDebugEnabled()){
            log.debug("Accepted CoAP media types in order of priority:");
            for(Option option : coapOptions){
                log.debug(OptionRegistry.MediaType.getByNumber((((UintOption) option).getDecodedValue())));
            }
        }

    }
}
