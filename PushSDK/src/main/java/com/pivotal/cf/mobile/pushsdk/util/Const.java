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

package com.pivotal.cf.mobile.pushsdk.util;

public class Const {

    public static final String TAG_NAME = "PivotalCFMSPushSDK";

    // TODO - update this to the production server when it is set up.
//    public static final String BASE_SERVER_URL = "http://ec2-54-234-124-123.compute-1.amazonaws.com:8090/";
    public static final String BASE_SERVER_URL = "http://cfms-push-service-staging.one.pepsi.cf-app.com/";
    public static final String BACKEND_REGISTRATION_REQUEST_URL = BASE_SERVER_URL + "v1/registration";
    public static final String BACKEND_MESSAGE_RECEIPT_URL = BASE_SERVER_URL + "v1/message_receipt";
}
