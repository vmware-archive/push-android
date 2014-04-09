package org.omnia.pushsdk.database;

import android.content.Context;
import android.net.Uri;

import org.omnia.pushsdk.model.EventBase;

import java.util.List;

public interface EventsStorage {

    public enum EventType {
        ALL,
        MESSAGE_RECEIPT
    }

    /**
     * Saves the given event object to the backing store.
     * @param context  an Android context
     * @param event  the {@link EventBase} object to save
     * @param eventType TODO
     * @return the {@link android.net.Uri} of the newly created {@link EventBase} if created successfully.  Otherwise null.
     */
    public Uri saveEvent(Context context, EventBase event, EventType eventType);

    /**
     * Gets the list of Event URIs from the backing store.
     *
     * @param context    an Android context
     * @param eventType  the event type to request
     * @return           the list of {@link Uri} objects for all Events currently in the backing store of the given {@link EventType}.
     */
    public List<Uri> getEventUris(Context context, EventType eventType);

    /**
     * Gets the list of {@link EventBase} URIs from the backing store that
     * satisfy the given status
     *
     * @param context    an Android context
     * @param eventType  the event type to request
     * @param status     a {@link EventBase.Status} value to query
     * @return  the list of {@link EventBase} {@link Uri} objects currently in the backing store with the given {@link Event.Status} of the given {@link EventType}.
     */
    public List<Uri> getEventUrisWithStatus(Context context, EventType eventType, int status);

    /**
     * Gets the {@link EventBase} with the given {@link Uri} from the backing store.
     * @param context  an Android context
     * @param uri  the {@link Uri} of the {@link EventBase} object to read
     * @return  the {@link EventBase} object
     */
    public EventBase readEvent(Context context, Uri uri);

    /**
     * Deletes the given {@link EventBase} (with the given {@link Uri}s from the backing store
     * @param context  an Android context
     * @param eventUris  the list of {@link Uri}s of {@link EventBase} object to delete from the backing store.
     * @param eventType TODO
     */
    public void deleteEvents(Context context, List<Uri> eventUris, EventType eventType);

    /**
     * Returns the number of {@link EventBase} currently in the backing store.
     * @param context
     * @param eventType TODO
     */
    public int getNumberOfEvents(Context context, EventType eventType);

    /**
     * Deletes all {@link EventBase}s from the backing store
     * @param context
     * @param eventType TODO
     */
    public void reset(Context context, EventType eventType);

    /**
     * Sets the status of the {@link EventBase} with the given {@link Uri}.
     * @param context
     * @param eventUri
     * @param status
     */
    public void setEventStatus(Context context, Uri eventUri, int status);
}
