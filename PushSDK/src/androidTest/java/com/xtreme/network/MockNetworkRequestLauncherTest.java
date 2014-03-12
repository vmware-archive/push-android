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

package com.xtreme.network;

import java.io.IOException;

import android.test.AndroidTestCase;

import com.xtreme.commons.testing.DelayedLoop;

public class MockNetworkRequestLauncherTest extends AndroidTestCase {

	private static final long TEN_SECOND_TIMEOUT = 10000;
	private static final long DELAY = 500;
	private boolean wasMethodCalled;
	private MockNetworkRequestLauncher launcher;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		launcher = new MockNetworkRequestLauncher();
	}

	public void testSuccessfulRequestNoDelay() throws Exception {

		final NetworkRequest request = new NetworkRequest("mock request");
		final NetworkResponse response = launcher.getNetworkResponse("TEST RESPONSE", 200);
		wasMethodCalled = false;

		final MockNetworkRequestListener listener = new MockNetworkRequestListener() {

			@Override
			public void onSuccess(NetworkRequest networkRequest, NetworkResponse networkRequestResponse) {
				wasMethodCalled = true;

				try {
					networkRequestResponse.getInputStream().read();
				} catch (Exception e) {
					fail();
				}
			}

			@Override
			public void onFailure(NetworkRequest networkRequest, NetworkError networkError) {
				fail();
			}
		};

		launcher.setNextRequestResolution(listener, response, 0L);
		launcher.executeRequest(request);
		assertTrue(wasMethodCalled);
	}

	public void testFailedRequestNoDelay() throws Exception {

		final NetworkRequest request = new NetworkRequest("mock request");
		final NetworkError error = new NetworkError(null, null);
		wasMethodCalled = false;

		final MockNetworkRequestListener listener = new MockNetworkRequestListener() {

			@Override
			public void onSuccess(NetworkRequest networkRequest, NetworkResponse networkRequestResponse) {
				fail();
			}

			@Override
			public void onFailure(NetworkRequest networkRequest, NetworkError networkError) {
				wasMethodCalled = true;
			}
		};

		launcher.setNextRequestResolution(listener, error, 0L);
		launcher.executeRequest(request);
		assertTrue(wasMethodCalled);
	}

	public void testSuccessfulRequestSomeDelay() throws Exception {

		final DelayedLoop delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
		final NetworkRequest request = new NetworkRequest("mock request");
		final NetworkResponse response = launcher.getNetworkResponse("TEST RESPONSE", 200);

		final MockNetworkRequestListener listener = new MockNetworkRequestListener() {

			@Override
			public void onSuccess(NetworkRequest networkRequest, NetworkResponse networkRequestResponse) {

				try {
					networkRequestResponse.getInputStream().read();
				} catch (Exception e) {
					fail();
				}

				delayedLoop.flagSuccess();
			}

			@Override
			public void onFailure(NetworkRequest networkRequest, NetworkError networkError) {
				delayedLoop.flagFailure();
			}
		};

		launcher.setNextRequestResolution(listener, response, DELAY);
		launcher.executeRequest(request);

		delayedLoop.startLoop();

		assertFalse(delayedLoop.isFailure());
		assertTrue(delayedLoop.isSuccess());
	}

	public void testFailedRequestSomeDelay() throws Exception {

		final DelayedLoop delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
		final NetworkRequest request = new NetworkRequest("mock request");
		final NetworkError error = new NetworkError(null, null);

		final MockNetworkRequestListener listener = new MockNetworkRequestListener() {

			@Override
			public void onSuccess(NetworkRequest networkRequest, NetworkResponse networkRequestResponse) {
				delayedLoop.flagFailure();
			}

			@Override
			public void onFailure(NetworkRequest networkRequest, NetworkError networkError) {
				delayedLoop.flagSuccess();
			}
		};

		launcher.setNextRequestResolution(listener, error, DELAY);

		launcher.executeRequest(request);

		delayedLoop.startLoop();

		assertFalse(delayedLoop.isFailure());
		assertTrue(delayedLoop.isSuccess());
	}

	public void testSuccessfulRequestSynchronously() throws Exception {

		final NetworkRequest request = new NetworkRequest("mock request");
		final NetworkResponse desiredResponse = new MockNetworkResponse(null, 200);

		launcher.setNextRequestResolution(null, desiredResponse, 0L);

		NetworkResponse actualResponse = launcher.executeRequestSynchronously(request);

		assertTrue(desiredResponse == actualResponse);
	}

	public void testFailedRequestSynchronously() throws Exception {

		final NetworkRequest request = new NetworkRequest("mock request");
		final IOException desiredException = new IOException("awesome exception");
		launcher.setNextRequestResolution(null, desiredException, 0L);

		boolean wasExceptionThrown = true;
		try {
			launcher.executeRequestSynchronously(request);
		} catch (IOException actualException) {
			assertTrue(desiredException == actualException);
			wasExceptionThrown = true;
		}
		assertTrue(wasExceptionThrown);
	}

	public void testSuccessfulRequestNoDelayBadResponseData() throws Exception {

		final NetworkRequest request = new NetworkRequest("mock request");
		final NetworkResponse response = launcher.getNetworkResponse(null, 200);
		wasMethodCalled = false;

		final MockNetworkRequestListener listener = new MockNetworkRequestListener() {

			@Override
			public void onSuccess(NetworkRequest networkRequest, NetworkResponse networkRequestResponse) {
				wasMethodCalled = true;
				try {
					networkRequestResponse.getInputStream().read();
					fail();
				} catch (IOException e) {

				} catch (Exception e) {
					fail();
				}
			}

			@Override
			public void onFailure(NetworkRequest networkRequest, NetworkError networkError) {
				fail();
			}
		};

		launcher.setNextRequestResolution(listener, response, 0L);
		launcher.executeRequest(request);
		assertTrue(wasMethodCalled);
	}
}
