package com.gopivotal.pushlib;

import android.test.AndroidTestCase;

public class GcmRegistrarTest extends AndroidTestCase {

    private static final String TEST_SENDER_ID = "SomeSenderId";

    public void testNullContext() {
        try {
            GcmRegistrar registrar = new GcmRegistrar(null, TEST_SENDER_ID);
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullSenderId() {
        try {
            GcmRegistrar registrar = new GcmRegistrar(getContext(), null);
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }
}
