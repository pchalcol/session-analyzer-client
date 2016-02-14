/*
 * Copyright (c) 2012, vsc-technologies - www.voyages-sncf.com
 * All rights reserved.
 * 
 * Les presents codes sources sont proteges par le droit d'auteur et 
 * sont la propriete exclusive de VSC Technologies.
 * Toute representation, reproduction, utilisation, exploitation, modification, 
 * adaptation de ces codes sources sont strictement interdits en dehors 
 * des autorisations formulees expressement par VSC Technologies, 
 * sous peine de poursuites penales. 
 * 
 * Usage of this software, in source or binary form, partly or in full, and of
 * any application developed with this software, is restricted to the
 * customer.s employees in accordance with the terms of the agreement signed
 * with VSC-technologies.
 */
package com.chaltec.web.session;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpSessionBindingEvent;

import org.github.jamm.MemoryMeter;

/**
 */
class SessionEvent implements Serializable {

    private static AtomicInteger counter = new AtomicInteger();
    private String sessionId;
    private String eventType;
    private String attributeName;
    private Object attributeValue;
    private String attributeType;
    /**
     * Taille de l'objet en octets.
     */
    private long attributeShallowHeap;
    /**
     * Taille de l'objet en octets + la taille des objets référencés.
     */
    private long attributeRetainedHeap;
    /**
     * Taille de la session en octets.
     */
    private long sessionRetainedHeap;

    private StackTraceElement[] stackTraceElements;
    private int index;

    private SessionEvent(final String sessionId,
                        final String eventType,
                        final String attributeName,
                        final Object attributeValue,
                        final String attributeType,
                        final long attributeShallowHeap,
                        final long attributeRetainedHeap,
                        final long sessionRetainedHeap,
                        final StackTraceElement[] stackTraceElements) {
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.attributeType = attributeType;
        this.attributeShallowHeap = attributeShallowHeap;
        this.attributeRetainedHeap = attributeRetainedHeap;
        this.sessionRetainedHeap = sessionRetainedHeap;
        this.index = counter.incrementAndGet();
        this.stackTraceElements = stackTraceElements;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public Object getAttributeValue() {
        return attributeValue;
    }

    long getAttributeShallowHeap() {
        return attributeShallowHeap;
    }

    long getAttributeRetainedHeap() {
        return attributeRetainedHeap;
    }

    long getSessionRetainedHeap() {
        return sessionRetainedHeap;
    }

    public StackTraceElement[] getStackTraceElements() {
        return stackTraceElements;
    }

    public int getIndex() {
        return index;
    }

    public static SessionEvent createSessionEvent(final HttpSessionBindingEvent se,
            final String eventType,
            final StackTraceElement[] stackTraceElements) {

        Object attributeValue = se.getValue();
        String attributeName = se.getName();
        String sessionId = se.getSession() != null ? se.getSession().getId() : "";

        // Memory analysis
        //final long attributeShallowHeap = MemoryCounterAgent.sizeOf(attributeValue);
        //final long attributeRetainedHeap = MemoryCounterAgent.deepSizeOf(attributeValue);

        MemoryMeter meter = new MemoryMeter();
        final long attributeRetainedHeap = meter.measureDeep(attributeValue);
        //meter.measure(object);
        meter.countChildren(attributeValue);

/*
        final List<Object> sessionAttributes = new ArrayList<>();
        final Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            sessionAttributes.add(session.getAttribute(attributeNames.nextElement()));
        }
        // on calcule la taille de tous les attributs moins la taille de la liste
        final long sessionRetainedHeap = MemoryCounterAgent.deepSizeOf(sessionAttributes) - MemoryCounterAgent.sizeOf(sessionAttributes);
*/

        return new SessionEvent(sessionId,
                eventType,
                attributeName,
                attributeValue,
                attributeValue.getClass().getName(),
                /*attributeShallowHeap*/0,
                attributeRetainedHeap,
                0,
                stackTraceElements);
    }
}
