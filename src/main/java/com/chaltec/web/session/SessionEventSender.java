package com.chaltec.web.session;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSessionBindingEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SessionEventSender {

    protected final static Logger log = LoggerFactory.getLogger(SessionEventSender.class);

    private static SessionEventSender instance;
    private ThreadPoolTaskExecutor taskExecutor;
    private Gson gson;
    private RestTemplate restTemplate;

    private final String url = "http://localhost:9292/session/data";
    private final String classToExclude = "session.class.name.to.exclude"; // Change accordingly

    public synchronized static SessionEventSender getInstance() {
        if (null == instance) instance = new SessionEventSender();
        return instance;
    }

    private SessionEventSender() {

        gson = new GsonBuilder()
                .setExclusionStrategies(new JsonExclusionStrategy())
                /*.serializeSpecialFloatingPointValues()*/
                .create();

        GsonHttpMessageConverter converter = new GsonHttpMessageConverter(gson);

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(converter);

        SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        //httpRequestFactory.setProxy(new Proxy(Type.HTTP, new InetSocketAddress(9595)));
        //restTemplate = new RestTemplate(httpRequestFactory);

        restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(converters);

        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(30);
        taskExecutor.setQueueCapacity(2000);
        taskExecutor.initialize();
    }

    public void sendEvent(HttpSessionBindingEvent se, String eventType, StackTraceElement[] stackTraceElements) {


        // Stacktrace
        List<StackTraceElement> stackTraceToSent = new ArrayList<>();

        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement.getClassName().contains(classToExclude)) continue;
            stackTraceToSent.add(stackTraceElement);
        }

        taskExecutor.execute(new EventSenderTask(
                        se,
                        eventType,
                        stackTraceToSent.toArray(new StackTraceElement[stackTraceToSent.size()]))
        );
    }

    /**
     * Runnable d'envoi de l'evenement.
     */
    private class EventSenderTask implements Runnable {
        private HttpSessionBindingEvent sessionBindingEvent;
        private String eventType;
        private StackTraceElement[] stackTraceElements;

        public EventSenderTask(final HttpSessionBindingEvent se,
                               final String eventType,
                               final StackTraceElement[] stackTraceElements) {
            sessionBindingEvent = se;
            this.eventType = eventType;
            this.stackTraceElements = stackTraceElements;
        }

        @Override
        public void run() {
            final SessionEvent event = SessionEvent.createSessionEvent(sessionBindingEvent, eventType, stackTraceElements);

            try {
                restTemplate.postForObject(url, event, String.class);

            } catch (Throwable t) {
                onErrorLogThenSendThinEvent(event, t);
            }
        }

        /**
         * Fallback.
         * @param event
         */
        private void onErrorLogThenSendThinEvent(final SessionEvent event, Throwable t) {

            log.error(String.format("Fallback : marshalling attribut %s ko : %s",
                    event.getAttributeName(),
                    t.getMessage()));

            String str = String.format("Sending event with session id: %s, type: %s, attributeName: %s, attributeShallowHeap: %d," +
                                    " attributeRetainedHeap: %d, sessionRetainedHeap: %d, counter: %d",
                            event.getSessionId(),
                            event.getAttributeType(),
                            event.getAttributeName(),
                            event.getAttributeShallowHeap(),
                            event.getAttributeRetainedHeap(),
                            event.getSessionRetainedHeap(),
                            event.getIndex()
                    );

            log.debug(str);

            Map<String, String> m = new HashMap<>();
            m.put("sessionId", event.getSessionId());
            m.put("eventType", event.getEventType());
            m.put("attributeName", event.getAttributeName());
            m.put("attributeValue", event.getAttributeType());
            m.put("attributeShallowHeap", String.valueOf(event.getAttributeShallowHeap()));
            m.put("attributeRetainedHeap", String.valueOf(event.getAttributeRetainedHeap()));
            m.put("sessionRetainedHeap", String.valueOf(event.getSessionRetainedHeap()));
            m.put("index", String.valueOf(event.getIndex()));

            restTemplate.postForObject(url, m, String.class);
        }
    }
}
