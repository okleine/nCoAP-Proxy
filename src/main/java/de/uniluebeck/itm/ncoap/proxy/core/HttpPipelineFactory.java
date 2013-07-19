package de.uniluebeck.itm.ncoap.proxy.core;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 19.07.13
 * Time: 20:02
 * To change this template use File | Settings | File Templates.
 */
public class HttpPipelineFactory implements ChannelPipelineFactory{

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Override
    public ChannelPipeline getPipeline() throws Exception {

        ChannelPipeline pipeline = Channels.pipeline();

        //HTTP protocol handlers
        pipeline.addLast("HTTP Decoder", new HttpRequestDecoder());
        pipeline.addLast("HTTP Chunk Aggrgator", new HttpChunkAggregator(1048576));
        pipeline.addLast("HTTP Encoder", new HttpResponseEncoder());
        pipeline.addLast("HTTP Deflater", new HttpContentCompressor());

        //Proxy specific handler
        pipeline.addLast("Execution Handler",
            new ExecutionHandler(
                        new OrderedMemoryAwareThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, 0, 0))
        );


        log.debug("New HTTP pipeline instance created.");

        return pipeline;
    }
}
