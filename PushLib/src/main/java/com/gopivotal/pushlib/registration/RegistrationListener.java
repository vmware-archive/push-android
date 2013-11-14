package com.gopivotal.pushlib.registration;

public interface RegistrationListener {
    void onRegistrationComplete();
    void onRegistrationFailed(String reason);
}
