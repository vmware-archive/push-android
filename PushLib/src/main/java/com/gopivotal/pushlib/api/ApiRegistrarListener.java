package com.gopivotal.pushlib.api;

public interface ApiRegistrarListener {
    void onRegistrationSuccess();
    void onRegistrationFailed(String reason);
}
