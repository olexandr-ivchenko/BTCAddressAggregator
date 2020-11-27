package com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc;

import java.util.List;

public class JsonRpcRequest {
    private String jsonrpc = "2.0";
    private String id;
    private String method;
    private List<Object> params;

    public JsonRpcRequest() {
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }
}
