/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.android.push.gcm;

import android.test.AndroidTestCase;

import java.util.concurrent.Semaphore;

public class GcmUnregistrationApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_GCM_DEVICE_REGISTRATION_ID = "SomeGcmDeviceUnregistrationId";

    private Semaphore semaphore;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        semaphore = new Semaphore(0);
    }

    public void testNullContext() {
        try {
            new GcmUnregistrationApiRequestImpl(null, new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID));
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullGcmProvider() {
        try {
            new GcmUnregistrationApiRequestImpl(getContext(), null);
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullListener() {
        try {
            final GcmUnregistrationApiRequest request = new GcmUnregistrationApiRequestImpl(getContext(), new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID));
            request.startUnregistration(null);
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testSuccessfulInitialUnregistration() throws InterruptedException {
        final FakeGcmProvider gcm = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID, true, false);
        final GcmUnregistrationApiRequestImpl request = new GcmUnregistrationApiRequestImpl(getContext(), gcm);
        final GcmUnregistrationListener listener = getGcmUnregistrationListener(true);
        request.startUnregistration(listener);
        semaphore.acquire();
        assertTrue(gcm.wasUnregisterCalled());
    }

    public void testFailedUnregistration() throws InterruptedException {
        final FakeGcmProvider gcm = new FakeGcmProvider(null, true, true);
        final GcmUnregistrationApiRequestImpl registrar = new GcmUnregistrationApiRequestImpl(getContext(), gcm);
        final GcmUnregistrationListener listener = getGcmUnregistrationListener(false);
        registrar.startUnregistration(listener);
        semaphore.acquire();
        assertTrue(gcm.wasUnregisterCalled());
    }

    private GcmUnregistrationListener getGcmUnregistrationListener(final boolean isSuccessfulUnregistration) {
        return new GcmUnregistrationListener() {
            @Override
            public void onGcmUnregistrationComplete() {
                assertTrue(isSuccessfulUnregistration);
                semaphore.release();
            }

            @Override
            public void onGcmUnregistrationFailed(String reason) {
                assertFalse(isSuccessfulUnregistration);
                semaphore.release();
            }
        };
    }
}
