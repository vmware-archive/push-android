package com.pivotal.cf.mobile.pushsdk.model;

public class EventType {

    public static final String PUSH_RECEIVED = "event_push_received";
    public static final String INITIALIZED = "event_initialized";
    public static final String ACTIVE = "event_active";
    public static final String INACTIVE = "event_inactive";
    public static final String FOREGROUNDED = "event_foregrounded";
    public static final String BACKGROUNDED = "event_backgrounded";
    public static final String REGISTERED = "event_registered";

    // TODO - should this be an enum?  or a parameter class?  how to make more type safe?
}
