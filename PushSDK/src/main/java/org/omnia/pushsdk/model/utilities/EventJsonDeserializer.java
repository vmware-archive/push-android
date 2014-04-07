package org.omnia.pushsdk.model.utilities;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.omnia.pushsdk.model.EventBase;

import java.lang.reflect.Type;

/**
 * Used to deserialize Events objects in an EventMessage JSON string. We need to
 * use a custom deserializer since EventBase is an abstract class and GSON doesn't
 * know how to construct its subclasses without our help. We need to look at the
 * value of the event's "type" field to determine which event subclass we need
 * to construct.
 * 
 */
public class EventJsonDeserializer implements JsonDeserializer<EventBase> {

	@Override
	public EventBase deserialize(JsonElement jsonElement, Type t, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		final String eventType = getEventType(jsonElement);
		final EventBase event = EventHelper.deserializeEvent(eventType, jsonElement, jsonDeserializationContext);
		return event;
	}

	private String getEventType(final JsonElement jsonElement) {
		return jsonElement.getAsJsonObject().getAsJsonPrimitive(EventBase.Columns.TYPE).getAsString();
	}
}
