/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */

package io.pivotal.android.push.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TagsHelper {

    private final Set<String> subscribeTags;
    private final Set<String> unsubscribeTags;

    public TagsHelper(Set<String> savedTags, Set<String> newTags) {

        if ((isNullOrEmpty(savedTags) && isNullOrEmpty(newTags)) || (newTags != null && newTags.equals(savedTags))) {

            subscribeTags = Collections.EMPTY_SET;
            unsubscribeTags = Collections.EMPTY_SET;

        } else if (isNullOrEmpty(savedTags)) {

            subscribeTags = new HashSet<String>(newTags);
            unsubscribeTags = Collections.EMPTY_SET;

        } else if (isNullOrEmpty(newTags)) {

            subscribeTags = Collections.EMPTY_SET;
            unsubscribeTags = new HashSet<String>(savedTags);

        } else {

            subscribeTags = new HashSet<String>(newTags);
            unsubscribeTags = new HashSet<String>(savedTags);
            subscribeTags.removeAll(savedTags);
            unsubscribeTags.removeAll(newTags);

        }
    }

    private boolean isNullOrEmpty(Set<String> s) {
        return s == null || s.isEmpty();
    }

    public Set<String> getSubscribeTags() {
        return Collections.unmodifiableSet(subscribeTags);
    }

    public Set<String> getUnsubscribeTags() {
        return Collections.unmodifiableSet(unsubscribeTags);
    }
}
