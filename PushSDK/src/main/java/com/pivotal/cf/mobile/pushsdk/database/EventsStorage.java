package com.pivotal.cf.mobile.pushsdk.database;

import android.net.Uri;

import com.pivotal.cf.mobile.pushsdk.model.events.Event;

import java.util.List;

public interface EventsStorage {

    /**
     * Saves the given event object to the backing store.
     * @param event  the {@link com.pivotal.cf.mobile.pushsdk.model.events.Event} object to save
     * @return the {@link android.net.Uri} of the newly created {@link com.pivotal.cf.mobile.pushsdk.model.events.Event} if created successfully.  Otherwise null.
     */
    public Uri saveEvent(Event event);

    /**
     * Gets the list of Event URIs from the backing store.
     *
     * @return           the list of {@link Uri} objects for all Events currently in the backing store of the given {@link EventType}.
     */
    public List<Uri> getEventUris();

    /**
     * Gets the list of {@link com.pivotal.cf.mobile.pushsdk.model.events.Event} URIs from the backing store that
     * satisfy the given status
     *
     * @param status     a {@link com.pivotal.cf.mobile.pushsdk.model.events.Event.Status} value to query
     * @return  the list of {@link com.pivotal.cf.mobile.pushsdk.model.events.Event} {@link Uri} objects currently in the backing store with the given {@link Event.Status} of the given {@link EventType}.
     */
    public List<Uri> getEventUrisWithStatus(int status);

    /**
     * Gets the {@link com.pivotal.cf.mobile.pushsdk.model.events.Event} with the given {@link Uri} from the backing store.
     * @param uri  the {@link android.net.Uri} of the {@link com.pivotal.cf.mobile.pushsdk.model.events.Event} object to read
     * @return  the {@link com.pivotal.cf.mobile.pushsdk.model.events.Event} object
     */
    public Event readEvent(Uri uri);

    /**
     * Deletes the given {@link com.pivotal.cf.mobile.pushsdk.model.events.Event} (with the given {@link Uri}s from the backing store
     * @param eventUris  the list of {@link android.net.Uri}s of {@link com.pivotal.cf.mobile.pushsdk.model.events.Event} object to delete from the backing store.
     */
    public void deleteEvents(List<Uri> eventUris);

    /**
     * Returns the number of {@link com.pivotal.cf.mobile.pushsdk.model.events.Event} currently in the backing store.
     */
    public int getNumberOfEvents();

    /**
     * Deletes all {@link com.pivotal.cf.mobile.pushsdk.model.events.Event}s from the backing store
     */
    public void reset();

    /**
     * Sets the status of the {@link com.pivotal.cf.mobile.pushsdk.model.events.Event} with the given {@link Uri}.
     * @param eventUri
     * @param status
     */
    public void setEventStatus(Uri eventUri, int status);
}
