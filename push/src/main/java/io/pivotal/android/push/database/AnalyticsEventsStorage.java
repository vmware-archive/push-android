package io.pivotal.android.push.database;

import android.net.Uri;

import java.util.List;

import io.pivotal.android.push.model.analytics.AnalyticsEvent;

public interface AnalyticsEventsStorage {

    /**
     * Saves the given event object to the backing store.
     * @param event  the {@link AnalyticsEvent} object to save
     * @return the {@link android.net.Uri} of the newly created {@link AnalyticsEvent} if created successfully.  Otherwise null.
     */
    public Uri saveEvent(AnalyticsEvent event);

    /**
     * Gets the list of Event URIs from the backing store.
     *
     * @return           the list of {@link Uri} objects for all Events currently in the backing store of the given eventType.
     */
    public List<Uri> getEventUris();

    /**
     * Gets the list of {@link AnalyticsEvent} URIs from the backing store that
     * satisfy the given status
     *
     * @param status     a {@link AnalyticsEvent.Status} value to query
     * @return  the list of {@link AnalyticsEvent} {@link Uri} objects currently in the backing store with the given {@link AnalyticsEvent.Status} of the given eventType.
     */
    public List<Uri> getEventUrisWithStatus(int status);

    /**
     * Gets the list of {@link AnalyticsEvent} URIs from the backing store with
     * the given type.
     *
     * @param eventType   The eventType to be searched for
     * @return  the list of {@link AnalyticsEvent} {@link Uri} objects currently in the backing store with the given eventType.
     */
    public List<Uri> getEventUrisWithType(String eventType);

    /**
     * Gets the {@link AnalyticsEvent} with the given {@link Uri} from the backing store.
     * @param uri  the {@link android.net.Uri} of the {@link AnalyticsEvent} object to read
     * @return  the {@link AnalyticsEvent} object
     */
    public AnalyticsEvent readEvent(Uri uri);

    /**
     * Deletes the given {@link AnalyticsEvent} (with the given {@link Uri}s from the backing store
     * @param eventUris  the list of {@link android.net.Uri}s of {@link AnalyticsEvent} object to delete from the backing store.
     */
    public void deleteEvents(List<Uri> eventUris);

    /**
     * Gets the number of {@link AnalyticsEvent} currently in the backing store.
     * @return the number of {@link AnalyticsEvent} currently in the backing store.
     */
    public int getNumberOfEvents();

    /**
     * Deletes all {@link AnalyticsEvent}s from the backing store
     */
    public void reset();

    /**
     * Sets the status of the {@link AnalyticsEvent} with the given {@link Uri}.
     * @param eventUri the {@link android.net.Uri} of the {@link AnalyticsEvent} to set the status
     * @param status the {@link AnalyticsEvent.Status} value to set
     */
    public void setEventStatus(Uri eventUri, int status);
}
