package com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc;

public class GenericResponse<T> {
    private T result;
    private String error;
    private String id;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
