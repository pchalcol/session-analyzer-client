package com.chaltec.web.session;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionContext;

public class StandardSession implements HttpSession {

    private static Map<?, ?> EMPTY_MAP = new HashMap<>();

    private HttpSession session;
    private SessionEventSender eventSender;

    public StandardSession(HttpSession session) {
        this.session = session;
        eventSender = SessionEventSender.getInstance();
    }

    @Override
    public long getCreationTime() {
        return session.getCreationTime();
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public long getLastAccessedTime() {
        return session.getLastAccessedTime();
    }

    @Override
    public ServletContext getServletContext() {
        return session.getServletContext();
    }

    @Override
    public void setMaxInactiveInterval(final int interval) {
        session.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval() {
        return session.getMaxInactiveInterval();
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return session.getSessionContext();
    }

    @Override
    public Object getAttribute(final String name) {
        Object value = session.getAttribute(name);

        HttpSessionBindingEvent se = new HttpSessionBindingEvent(this, name, /*value*/EMPTY_MAP);
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        eventSender.sendEvent(se, "attributeAccessed", stackTraceElements);

        return value;
    }

    @Override
    public Object getValue(final String name) {
        return session.getValue(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return session.getAttributeNames();
    }

    @Override
    public String[] getValueNames() {
        return session.getValueNames();
    }

    @Override
    public void setAttribute(final String name, final Object value) {
        session.setAttribute(name, value);
    }

    @Override
    public void putValue(final String name, final Object value) {
        session.putValue(name, value);
    }

    @Override
    public void removeAttribute(final String name) {
        session.removeAttribute(name);
    }

    @Override
    public void removeValue(final String name) {
        session.removeValue(name);
    }

    @Override
    public void invalidate() {
        session.invalidate();
    }

    @Override
    public boolean isNew() {
        return session.isNew();
    }

}
