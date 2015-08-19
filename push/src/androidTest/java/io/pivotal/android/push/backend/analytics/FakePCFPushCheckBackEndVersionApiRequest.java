package io.pivotal.android.push.backend.analytics;

import java.util.Arrays;
import java.util.List;

import io.pivotal.android.push.version.Version;

public class FakePCFPushCheckBackEndVersionApiRequest implements PCFPushCheckBackEndVersionApiRequest {

    public enum RequestResult {
        SUCCESS,
        OLD,
        RETRYABLE_FAILURE,
        FATAL_FAILURE
    }

    public static class Request {

        public final RequestResult requestResult;
        public final Version version;

        public Request(RequestResult requestResult) {
            this.requestResult = requestResult;
            this.version = null;
        }

        public Request(RequestResult requestResult, Version version) {
            this.requestResult = requestResult;
            this.version = version;
        }
    }

    private final FakePCFPushCheckBackEndVersionApiRequest originatingRequest;
    private List<Request> requests;
    private int numberOfRequestsMade = 0;

    public FakePCFPushCheckBackEndVersionApiRequest(FakePCFPushCheckBackEndVersionApiRequest originatingRequest) {
        this.originatingRequest = originatingRequest;
    }

    public FakePCFPushCheckBackEndVersionApiRequest() {
        this.originatingRequest = null;
    }

    @Override
    public void startCheckBackEndVersion(PCFPushCheckBackEndVersionListener listener) {

        final Request request = requests.get(numberOfRequestsMade);

        numberOfRequestsMade += 1;
        if (originatingRequest != null) {
            originatingRequest.numberOfRequestsMade += 1;
        }

        switch(request.requestResult) {

            case SUCCESS:
                listener.onCheckBackEndVersionSuccess(request.version);
                break;

            case OLD:
                listener.onCheckBackEndVersionIsOldVersion();
                break;

            case RETRYABLE_FAILURE:
                listener.onCheckBackEndVersionRetryableFailure("The fake request retryably failed fakely.");
                break;

            case FATAL_FAILURE:
            default:
                listener.onCheckBackEndVersionFatalFailure("The fake request fatally failed fakely");
                break;
        }
    }

    @Override
    public PCFPushCheckBackEndVersionApiRequest copy() {
        final FakePCFPushCheckBackEndVersionApiRequest newRequest = new FakePCFPushCheckBackEndVersionApiRequest(this);
        newRequest.requests = requests;
        newRequest.numberOfRequestsMade = numberOfRequestsMade;
        return newRequest;
    }

    public int getNumberOfRequestsMade() {
        return numberOfRequestsMade;
    }

    public void setRequests(Request... results) {
        this.requests = Arrays.asList(results);
    }
}
