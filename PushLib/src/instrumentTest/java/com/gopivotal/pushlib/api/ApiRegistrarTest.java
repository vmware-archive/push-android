package com.gopivotal.pushlib.api;

import android.test.AndroidTestCase;

public class ApiRegistrarTest extends AndroidTestCase {

    public void testRequiresApiProvider() {
        try {
            new ApiRegistrar(null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }
}
