/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */

package io.pivotal.android.push.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TagsHelper {

    public static final Set<String> EMPTY_SET = Collections.emptySet();
    private final Set<String> subscribeTags;
    private final Set<String> unsubscribeTags;

    public TagsHelper(Set<String> savedTags, Set<String> newTags) {

        if ((isNullOrEmpty(savedTags) && isNullOrEmpty(newTags)) || (newTags != null && newTags.equals(savedTags))) {

            subscribeTags = EMPTY_SET;
            unsubscribeTags = EMPTY_SET;

        } else if (isNullOrEmpty(savedTags)) {

            subscribeTags = new HashSet<>(newTags);
            unsubscribeTags = EMPTY_SET;

        } else if (isNullOrEmpty(newTags)) {

            subscribeTags = EMPTY_SET;
            unsubscribeTags = new HashSet<>(savedTags);

        } else {

            subscribeTags = new HashSet<>(newTags);
            unsubscribeTags = new HashSet<>(savedTags);
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
