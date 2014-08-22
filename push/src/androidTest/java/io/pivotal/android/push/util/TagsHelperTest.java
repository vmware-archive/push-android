/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */

package io.pivotal.android.push.util;

import android.test.AndroidTestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TagsHelperTest extends AndroidTestCase {

    private TagsHelper tagsHelper;

    public void testNullInput() {
        tagsHelper = new TagsHelper(null, null);
        assertTrue(tagsHelper.getSubscribeTags().isEmpty());
        assertTrue(tagsHelper.getUnsubscribeTags().isEmpty());
    }

    public void testNullSavedTags() {
        tagsHelper = new TagsHelper(null, Collections.EMPTY_SET);
        assertTrue(tagsHelper.getSubscribeTags().isEmpty());
        assertTrue(tagsHelper.getUnsubscribeTags().isEmpty());
    }

    public void testNullNewTags() {
        tagsHelper = new TagsHelper(Collections.EMPTY_SET, null);
        assertTrue(tagsHelper.getSubscribeTags().isEmpty());
        assertTrue(tagsHelper.getUnsubscribeTags().isEmpty());
    }

    public void testEmptyLists() {
        tagsHelper = new TagsHelper(Collections.EMPTY_SET, Collections.EMPTY_SET);
        assertTrue(tagsHelper.getSubscribeTags().isEmpty());
        assertTrue(tagsHelper.getUnsubscribeTags().isEmpty());
    }

    public void testEmptySavedTagsListShouldResultInAPopulatedSubscribeList() {
        tagsHelper = new TagsHelper(Collections.EMPTY_SET, makeSet("DON'T", "YOU", "HATE", "PANTS"));
        assertEquals(makeSet("DON'T", "YOU", "HATE", "PANTS"), tagsHelper.getSubscribeTags());
        assertTrue(tagsHelper.getUnsubscribeTags().isEmpty());
    }

    public void testNullSavedTagsListShouldResultInAPopulatedSubscribeList() {
        tagsHelper = new TagsHelper(null, makeSet("DON'T", "YOU", "HATE", "PANTS"));
        assertEquals(makeSet("DON'T", "YOU", "HATE", "PANTS"), tagsHelper.getSubscribeTags());
        assertTrue(tagsHelper.getUnsubscribeTags().isEmpty());
    }

    public void testEmptyNewTagsListShouldResultInAPopulatedUnsubscribeList() {
        tagsHelper = new TagsHelper(makeSet("WOOZLE", "WOZZLE"), Collections.EMPTY_SET);
        assertTrue(tagsHelper.getSubscribeTags().isEmpty());
        assertEquals(makeSet("WOOZLE", "WOZZLE"), tagsHelper.getUnsubscribeTags());
    }

    public void testNullNewTagsListShouldResultInAPopulatedUnsubscribeList() {
        tagsHelper = new TagsHelper(makeSet("WOOZLE", "WOZZLE"), null);
        assertTrue(tagsHelper.getSubscribeTags().isEmpty());
        assertEquals(makeSet("WOOZLE", "WOZZLE"), tagsHelper.getUnsubscribeTags());
    }

    public void testMutuallyExclusivePopulatedLists() {
        tagsHelper = new TagsHelper(makeSet("SAVED1", "SAVED2"), makeSet("NEW1", "NEW2"));
        assertEquals(makeSet("NEW1", "NEW2"), tagsHelper.getSubscribeTags());
        assertEquals(makeSet("SAVED1", "SAVED2"), tagsHelper.getUnsubscribeTags());
    }

    public void testOverlappingLists() {
        tagsHelper = new TagsHelper(makeSet("SAVED1", "SAVED2", "KEEP1", "KEEP2"), makeSet("NEW1", "NEW2", "KEEP1", "KEEP2"));
        assertEquals(makeSet("NEW1", "NEW2"), tagsHelper.getSubscribeTags());
        assertEquals(makeSet("SAVED1", "SAVED2"), tagsHelper.getUnsubscribeTags());
    }

    public void testIdenticalLists() {
        tagsHelper = new TagsHelper(makeSet("KEEP1", "KEEP2"), makeSet("KEEP1", "KEEP2"));
        assertTrue(tagsHelper.getSubscribeTags().isEmpty());
        assertTrue(tagsHelper.getUnsubscribeTags().isEmpty());
    }

    private Set<String> makeSet(String... tags) {
        return new HashSet<String>(Arrays.asList(tags));
    }
}
