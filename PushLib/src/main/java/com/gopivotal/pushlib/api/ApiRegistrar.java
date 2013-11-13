package com.gopivotal.pushlib.api;

public class ApiRegistrar {

    private ApiProvider apiProvider;

    public ApiRegistrar(ApiProvider apiProvider) {
        verifyArguments(apiProvider);
        saveArguments(apiProvider);
    }

    private void verifyArguments(ApiProvider apiProvider) {
        if (apiProvider == null) {
            throw new IllegalArgumentException("apiProvider may not be null");
        }
    }

    private void saveArguments(ApiProvider apiProvider) {
        this.apiProvider = apiProvider;
    }

}
