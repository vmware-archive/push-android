package com.pivotal.cf.mobile.pushsdk.model;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

// TODO - remove this class somehow!
public class MessageReceiptEvent  {

    public static BaseEvent getMessageReceiptEvent(String variantUuid, String messageUuid, String deviceId) {
        final String eventId = UUID.randomUUID().toString();
        final Date time = new Date();
        return getMessageReceiptEvent(eventId, variantUuid, messageUuid, deviceId, time);
    }

    public static BaseEvent getMessageReceiptEvent(String variantUuid, String messageUuid, String deviceId, Date time) {
        final String eventId = UUID.randomUUID().toString();
        return getMessageReceiptEvent(eventId, variantUuid, messageUuid, deviceId, time);
    }

    public static BaseEvent getMessageReceiptEvent(String eventId, String variantUuid, String messageUuid, String deviceId, Date time) {
        final BaseEvent event = new BaseEvent();
        event.setEventType(EventType.PUSH_RECEIVED);
        event.setEventId(eventId);
        event.setVariantUuid(variantUuid);
        event.setTime(time);
        event.setDeviceId(deviceId);
        event.setStatus(BaseEvent.Status.NOT_POSTED);
        final HashMap<String, String> data = new HashMap<String, String>();
        data.put(EventPushReceivedData.MESSAGE_UUID, messageUuid);
        event.setData(data);
        return event;
    }

}
