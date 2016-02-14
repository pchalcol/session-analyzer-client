package com.chaltec.web.session;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;


public class CustomHttpSessionAttributeListener implements HttpSessionAttributeListener {

    private SessionEventSender eventSender;

    public CustomHttpSessionAttributeListener() {
        eventSender = SessionEventSender.getInstance();
    }

    @Override
    public void attributeAdded(final HttpSessionBindingEvent se) {
        sendEvent(se, "attributeAdded");
    }

    @Override
    public void attributeRemoved(final HttpSessionBindingEvent se) {
        sendEvent(se, "attributeRemoved");
    }

    @Override
    public void attributeReplaced(final HttpSessionBindingEvent se) {
        sendEvent(se, "attributeReplaced");
    }

    private void sendEvent(final HttpSessionBindingEvent se, final String type) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        eventSender.sendEvent(se, type, stackTraceElements);
    }
}
