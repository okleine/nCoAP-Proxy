package de.uniluebeck.itm.ncoap.proxy.handler.http2coap;

import com.google.common.collect.*;
import de.uniluebeck.itm.ncoap.message.options.InvalidOptionException;
import de.uniluebeck.itm.ncoap.message.options.Option;
import de.uniluebeck.itm.ncoap.message.options.OptionRegistry.OptionName;
import de.uniluebeck.itm.ncoap.message.options.OptionRegistry.MediaType;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
        mediaTypes.put( "text/plain",           MediaType.TEXT_PLAIN_UTF8   );
        mediaTypes.put( "application/xml",      MediaType.APP_XML           );
        mediaTypes.put( "application/rdf+xml",  MediaType.APP_RDF_XML       );
        mediaTypes.put( "application/shdt",     MediaType.APP_SHDT          );
        mediaTypes.put( "application/n3",       MediaType.APP_N3            );
    }

    public static List<Option> getOptions(String headerName, String headerValue) {

        if(headerName.equals(HttpHeaders.Names.ACCEPT)){
            return getAcceptOptions(headerValue);
        }

        return new ArrayList<>();
    }

    public static List<Option> getAcceptOptions(String httpAcceptHeaderValue){
        ArrayList<Option> result = new ArrayList<>();

        Multimap<Double, String> acceptedMediaTypes = getAcceptedMediaTypes(httpAcceptHeaderValue);

        for(Double priority : acceptedMediaTypes.keySet()){
            for(String httpMediaType : acceptedMediaTypes.get(priority)){
                MediaType coapMediaType = mediaTypes.get(httpMediaType);
                if(coapMediaType != null){
                    try {
                        result.add(Option.createUintOption(OptionName.ACCEPT, coapMediaType.number));
                        log.debug("Added CoAP ACCEPT Option {}.", coapMediaType);
                    } catch (InvalidOptionException e) {
                        log.error("This should never happen.", e);
                    }
                }
                else{
                    log.warn("No appropriate CoAP media type found for {}.", httpMediaType);
                }
            }
        }

        return result;
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

        Multimap<Double, String> result = TreeMultimap.create();

        for(String acceptedMediaType : headerValue.split(",")){
            ArrayList<String> parts = new ArrayList<>(Arrays.asList(acceptedMediaType.split(";")));

            if(log.isDebugEnabled()){
                log.debug("Media type (from HTTP ACCEPT header): {}.", acceptedMediaType);
                for(int i = 0; i < parts.size(); i++){
                    log.debug("Part {}: {}", i + 1, parts.get(i));
                }
            }

            double priority = 1.0;

            if(parts.size() > 1){
                String priorityString = parts.get(parts.size() - 1).replace(" ", "");
                if(priorityString.startsWith("q=")){
                    priority = Double.parseDouble(priorityString.substring(priorityString.indexOf("=") + 1));
                    parts.remove(parts.size() - 1);
                }
            }


            String httpMediaType;
            if(parts.size() > 1)
                httpMediaType = (parts.get(0) + ";" + parts.get(1)).replace(" ", "");
            else
                httpMediaType = parts.get(0).replace(" ", "");

            log.debug("Found accepted media type {} with priority {}.", httpMediaType, priority);

            if(httpMediaType.contains("*")){
                log.warn("There is no support for wildcard types ({})", httpMediaType);
                continue;
            }

            result.put(priority * (-1), httpMediaType);
            log.debug("Added media type {} with priority {}.", httpMediaType, priority);

        }

        return result;
    }
}
