package com.gopivotal.pushlib.gcm;

import java.io.IOException;

public interface GcmProvider {
    String register(String... senderIds) throws IOException;
}
