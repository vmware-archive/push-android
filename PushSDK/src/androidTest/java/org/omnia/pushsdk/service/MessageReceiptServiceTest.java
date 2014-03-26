package org.omnia.pushsdk.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.test.ServiceTestCase;

import org.omnia.pushsdk.prefs.FakeMessageReceiptsProvider;

import java.util.concurrent.Semaphore;

public class MessageReceiptServiceTest extends ServiceTestCase<MessageReceiptService> {

    private int testResultCode = MessageReceiptService.NO_RESULT;
    private TestResultReceiver testResultReceiver;
    private Intent intent;

    // Captures result codes from the service itself
    public class TestResultReceiver extends ResultReceiver {

        public TestResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            testResultCode = resultCode;
        }
    }

    public MessageReceiptServiceTest() {
        super(MessageReceiptService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MessageReceiptService.semaphore = new Semaphore(0);
        MessageReceiptService.messageReceiptsProvider = new FakeMessageReceiptsProvider(null);
        testResultReceiver = new TestResultReceiver(null);
        intent = getServiceIntent();
    }

    public void testReceiveNullIntent() throws InterruptedException {
        startService(null);
        MessageReceiptService.semaphore.acquire();
        assertEquals(MessageReceiptService.NO_RESULT, testResultCode);
        assertEquals(0, GcmService.messageReceiptsProvider.numberOfMessageReceipts());
    }

    private Intent getServiceIntent() {
        final Intent intent = new Intent(getContext(), GcmService.class);
        intent.putExtra(MessageReceiptService.KEY_RESULT_RECEIVER, testResultReceiver);
        return intent;
    }
}
