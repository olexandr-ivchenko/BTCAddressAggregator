
package com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "value",
    "n",
    "scriptPubKey"
})
public class Vout {

    @JsonProperty("value")
    private Double value;
    @JsonProperty("n")
    private Integer n;
    @JsonProperty("scriptPubKey")
    private ScriptPubKey scriptPubKey;

    @JsonProperty("value")
    public Double getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(Double value) {
        this.value = value;
    }

    @JsonProperty("n")
    public Integer getN() {
        return n;
    }

    @JsonProperty("n")
    public void setN(Integer n) {
        this.n = n;
    }

    @JsonProperty("scriptPubKey")
    public ScriptPubKey getScriptPubKey() {
        return scriptPubKey;
    }

    @JsonProperty("scriptPubKey")
    public void setScriptPubKey(ScriptPubKey scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
    }

}
