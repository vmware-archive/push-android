package org.omnia.pushsdk.database;

import android.net.Uri;

import org.omnia.pushsdk.model.BaseEvent;

import java.util.List;

public interface EventsStorage {

    public enum EventType {
        ALL,
        MESSAGE_RECEIPT
    }

    /**
     * Saves the given event object to the backing store.
     * @param event  the {@link org.omnia.pushsdk.model.BaseEvent} object to save
     * @param eventType TODO
     * @return the {@link android.net.Uri} of the newly created {@link org.omnia.pushsdk.model.BaseEvent} if created successfully.  Otherwise null.
     */
    public Uri saveEvent(BaseEvent event, EventType eventType);

    /**
     * Gets the list of Event URIs from the backing store.
     *
     * @param eventType  the event type to request
     * @return           the list of {@link Uri} objects for all Events currently in the backing store of the given {@link EventType}.
     */
    public List<Uri> getEventUris(EventType eventType);

    /**
     * Gets the list of {@link org.omnia.pushsdk.model.BaseEvent} URIs from the backing store that
     * satisfy the given status
     *
     * @param eventType  the event type to request
     * @param status     a {@link org.omnia.pushsdk.model.BaseEvent.Status} value to query
     * @return  the list of {@link org.omnia.pushsdk.model.BaseEvent} {@link Uri} objects currently in the backing store with the given {@link Event.Status} of the given {@link EventType}.
     */
    public List<Uri> getEventUrisWithStatus(EventType eventType, int status);

    /**
     * Gets the {@link org.omnia.pushsdk.model.BaseEvent} with the given {@link Uri} from the backing store.
     * @param uri  the {@link android.net.Uri} of the {@link org.omnia.pushsdk.model.BaseEvent} object to read
     * @return  the {@link org.omnia.pushsdk.model.BaseEvent} object
     */
    public BaseEvent readEvent(Uri uri);

    /**
     * Deletes the given {@link org.omnia.pushsdk.model.BaseEvent} (with the given {@link Uri}s from the backing store
     * @param eventUris  the list of {@link android.net.Uri}s of {@link org.omnia.pushsdk.model.BaseEvent} object to delete from the backing store.
     * @param eventType TODO
     */
    public void deleteEvents(List<Uri> eventUris, EventType eventType);

    /**
     * Returns the number of {@link org.omnia.pushsdk.model.BaseEvent} currently in the backing store.
     * @param eventType TODO
     */
    public int getNumberOfEvents(EventType eventType);

    /**
     * Deletes all {@link org.omnia.pushsdk.model.BaseEvent}s from the backing store
     * @param eventType TODO
     */
    public void reset(EventType eventType);

    /**
     * Sets the status of the {@link org.omnia.pushsdk.model.BaseEvent} with the given {@link Uri}.
     * @param eventUri
     * @param status
     */
    public void setEventStatus(Uri eventUri, int status);
}
