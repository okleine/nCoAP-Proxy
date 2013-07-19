package de.uniluebeck.itm.ncoap.proxy.handler.http2coap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.ncoap.message.options.InvalidOptionException;
import de.uniluebeck.itm.ncoap.message.options.Option;
import de.uniluebeck.itm.ncoap.message.options.OptionRegistry.OptionName;
import de.uniluebeck.itm.ncoap.message.options.OptionRegistry.MediaType;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 19.07.13
 * Time: 20:36
 * To change this template use File | Settings | File Templates.
 */
public class HttpHeader2CoapOption {

    private static Logger log = LoggerFactory.getLogger(HttpHeader2CoapOption.class.getName());

    private static BiMap<String, MediaType> mediaTypes = HashBiMap.create();

    static{
        mediaTypes.put("text/plain", MediaType.TEXT_PLAIN_UTF8);
        mediaTypes.put("application/xml", MediaType.APP_XML);
        mediaTypes.put("application/rdf+xml", MediaType.APP_RDF_XML);
        mediaTypes.put("application/shdt", MediaType.APP_SHDT);
        mediaTypes.put("application/n3", MediaType.APP_N3);
    }

    public static List<Option> getOptions(String headerName, String headerValue) throws InvalidOptionException {

        ArrayList<Option> result = new ArrayList<Option>();

        if(headerName.equals(HttpHeaders.Names.ACCEPT)){
            Multimap<Double, String> acceptedMediaTypes = getAcceptedMediaTypes(headerValue);

            for(Double priority : acceptedMediaTypes.keySet()){
                for(String httpMediaType : acceptedMediaTypes.get(priority)){
                    MediaType coapMediaType = mediaTypes.get(httpMediaType);
                    result.add(Option.createUintOption(OptionName.ACCEPT, coapMediaType.number));
                }
            }

            //return Option.createUintOption(OptionName.ACCEPT, (long) mediaType.number);
        }

        return null;
    }

    /**
     * Returns a {@link com.google.common.collect.Multimap}  with priorities as key and the name of the
     * accepted HTTP media type as value. The {@link com.google.common.collect.Multimap#keySet()} is guaranteed to
     * contain the keys ordered according to their priorities with the highest priority first.
     *
     * @param headerValue the value of an HTTP Accept-Header
     *
     * @return a {@link com.google.common.collect.Multimap}  with priorities as key and the name of the
     * accepted HTTP media type as value.
     */
    public static Multimap<Double, String> getAcceptedMediaTypes(String headerValue){

        Multimap<Double, String> result = HashMultimap.create();

        for(String acceptedMediaType : headerValue.split(",")){
            String[] parts = acceptedMediaType.split(";");

            double priority = 1.0;

            if(parts.length > 1){
                String priorityString =
                        parts[parts.length - 1].substring(parts[parts.length - 1].indexOf("=") + 1);

                priority = Double.parseDouble(priorityString);
            }

            String httpMediaType;
            if(parts.length > 2)
                httpMediaType = parts[0].replace("\\s", "") + ";" + parts[1].replace("\\s", "");
            else
                httpMediaType = parts[0].replace("\\s", "");

            //HTTP media subtype is wildcard
            if(httpMediaType.substring(httpMediaType.indexOf("/") + 1) == "*"){
                log.debug("Process wildcard in ACCEPT header ({}).", httpMediaType);

                for(String knownHttpMediaType : mediaTypes.keySet()){
                    if(knownHttpMediaType.startsWith((httpMediaType.substring(0, httpMediaType.indexOf("/") + 1)))){
                        result.put(priority, knownHttpMediaType);
                        log.debug("Added media type {} with priority {}.", httpMediaType);
                    }
                }
            }
            else{
                result.put(priority, httpMediaType);
                log.debug("Added media type {} with priority {}.", httpMediaType);
            }
        }

        return result;
    }
}
