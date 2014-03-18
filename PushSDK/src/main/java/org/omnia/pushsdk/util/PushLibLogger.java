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

package org.omnia.pushsdk.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Locale;

/**
 * Used by the Push Library to log messages to the device log.  An optional 'listener'
 * can be registered so an application can watch all the message traffic.
 *
 * If the "debuggable" flag is "false" in the application manifest file then only warning
 * or error messages will be printed to the device log.
 */
public class PushLibLogger {

    private static final String UI_THREAD = "UI";
    private static final String BACKGROUND_THREAD = "BG";

    private static boolean isDebuggable = false;
    private static PushLibLogger loggerInstance;
    private static String tagName = "PushLibLogger";
    private static boolean isSetup = false;
    private static Listener listener;
    private static Object lock = new Object();
    private static Handler mainHandler;

    public static interface Listener {
        void onLogMessage(String message);
    }

    public static void setup(Context context, String tagName) {
        PushLibLogger.isDebuggable = DebugUtil.getInstance(context).isDebuggable();
        PushLibLogger.tagName = tagName;
        PushLibLogger.isSetup = true;
    }

    private PushLibLogger() {
    }

    public static boolean isSetup() {
        return PushLibLogger.isSetup;
    }

    public static void i(String message) {
        final String formattedString = formatMessage(message, new Object[] {});
        if (isDebuggable) {
            Log.i(tagName, formattedString);
        }
        sendMessageToListener(formattedString);
    }

    public static void w(String message, Throwable tr) {
        final String formattedString = formatMessage(message, new Object[] {}) + ": " + Log.getStackTraceString(tr);
        if (isDebuggable) {
            Log.w(tagName, formattedString);
        }
        sendMessageToListener(formattedString);
    }

    public static void w(Throwable tr) {
        final String formattedString = formatMessage("", new Object[] {}) + Log.getStackTraceString(tr);
        if (isDebuggable) {
            Log.w(tagName, formattedString);
        }
        sendMessageToListener(formattedString);
    }

    public static void w(String message) {
        final String formattedString = formatMessage(message, new Object[] {});
        if (isDebuggable) {
            Log.w(tagName, formattedString);
        }
        sendMessageToListener(formattedString);
    }

    public static void v(String message) {
        final String formattedString = formatMessage(message, new Object[] {});
        if (isDebuggable) {
            Log.v(tagName, formattedString);
        }
        sendMessageToListener(formattedString);
    }

    public static void d(String message) {
        final String formattedString = formatMessage(message, new Object[] {});
        if (isDebuggable) {
            Log.d(tagName, formattedString);
        }
        sendMessageToListener(formattedString);
    }

    public static void d(String message, Throwable tr) {
        final String formattedString = formatMessage(message, new Object[] {}) + ": " + Log.getStackTraceString(tr);
        if (isDebuggable) {
            Log.d(tagName, formattedString);
        }
        sendMessageToListener(formattedString);
    }

    public static void d(Throwable tr) {
        final String formattedString = formatMessage("", new Object[] {}) + Log.getStackTraceString(tr);
        if (isDebuggable) {
            Log.d(tagName, formattedString);
        }
        sendMessageToListener(formattedString);
    }

    public static void fd(String message, Object... objects) {
        final String formattedString = formatMessage(message, objects);
        if (isDebuggable) {
            Log.d(tagName, formattedString);
        }
    }

    public static void e(String message) {
        final String formattedString = formatMessage(message, new Object[] {});
        sendMessageToListener(formattedString);
        Log.e(tagName, formattedString);
    }

    public static void ex(String message, Throwable tr) {
        final String formattedString = formatMessage(message, new Object[] {}) + ": " + Log.getStackTraceString(tr);
        sendMessageToListener(formattedString);
        Log.e(tagName, formattedString);
    }

    public static void ex(Throwable tr) {
        final String formattedString = formatMessage("", new Object[] {}) + Log.getStackTraceString(tr);
        sendMessageToListener(formattedString);
        Log.e(tagName, formattedString);
    }

    private static String formatMessage(String message, Object... objects) {

        final StackTraceElement s = getCallingStackTraceElement();
        String formattedMessage = String.format(Locale.getDefault(), "[%s:%s:%d:tid%d] ", s.getClassName(), s.getMethodName(), s.getLineNumber(), Thread.currentThread().getId());

        if (objects.length > 0)
            formattedMessage += String.format(message, objects);
        else
            formattedMessage += message;

        return addThreadInfo(formattedMessage);
    }

    private static StackTraceElement getCallingStackTraceElement() {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        final int indexForFirstElementInLoggerClass = getFirstElementIndexForLoggerClass(stackTraceElements);
        return getFirstElementInCallingClass(stackTraceElements, indexForFirstElementInLoggerClass);
    }

    private static int getFirstElementIndexForLoggerClass(final StackTraceElement[] stackTraceElements) {
        for (int i = 0; i < stackTraceElements.length; i += 1) {
            final StackTraceElement s = stackTraceElements[i];
            if (stackTraceElementIsForLoggerClass(s)) {
                return i;
            }
        }
        throw new IllegalArgumentException("No PushLibLogger class reference found");
    }

    private static StackTraceElement getFirstElementInCallingClass(StackTraceElement[] stackTraceElements, int indexForFirstElementInLoggerClass) {
        for (int i = indexForFirstElementInLoggerClass; i < stackTraceElements.length; i += 1) {
            final StackTraceElement s = stackTraceElements[i];
            if (!stackTraceElementIsForLoggerClass(s)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No calling class reference found");
    }

    private static String getLoggerClassName() {
        return getInstance().getClass().getName();
    }

    private static boolean stackTraceElementIsForLoggerClass(StackTraceElement s) {
        final String loggerClassName = getLoggerClassName();
        return s.getClassName().equals(loggerClassName);
    }

    private static String addThreadInfo(String string) {
        if (isUiThread()) {
            return "*" + UI_THREAD + "* " + string;
        }
        return "*" + BACKGROUND_THREAD + "* " + string;
    }

    private static boolean isUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private static PushLibLogger getInstance() {
        if (loggerInstance == null)
            loggerInstance = new PushLibLogger();
        return loggerInstance;
    }

    public static void setListener(Listener listener) {
        synchronized (lock) {
            PushLibLogger.listener = listener;
            if (PushLibLogger.listener != null && PushLibLogger.mainHandler == null) {
                PushLibLogger.mainHandler = new Handler(Looper.getMainLooper());
            }
        }
    }

    public static void sendMessageToListener(final String message) {
        synchronized (lock) {
            if (listener != null && mainHandler != null) {
                final Listener localListener = listener;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        localListener.onLogMessage(message.replaceFirst("^.*\\]\\s*", ""));
                    }
                });
            }
        }
    }
}
