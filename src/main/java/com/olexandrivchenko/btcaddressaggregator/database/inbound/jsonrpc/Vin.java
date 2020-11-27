
package com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "coinbase",
    "sequence",
    "txid",
    "vout",
    "scriptSig"
})
public class Vin {

    @JsonProperty("coinbase")
    private String coinbase;
    @JsonProperty("sequence")
    private Long sequence;
    @JsonProperty("txid")
    private String txid;
    @JsonProperty("vout")
    private Integer vout;
    @JsonProperty("scriptSig")
    private ScriptSig scriptSig;

    @JsonProperty("coinbase")
    public String getCoinbase() {
        return coinbase;
    }

    @JsonProperty("coinbase")
    public void setCoinbase(String coinbase) {
        this.coinbase = coinbase;
    }

    @JsonProperty("sequence")
    public Long getSequence() {
        return sequence;
    }

    @JsonProperty("sequence")
    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    @JsonProperty("txid")
    public String getTxid() {
        return txid;
    }

    @JsonProperty("txid")
    public void setTxid(String txid) {
        this.txid = txid;
    }

    @JsonProperty("vout")
    public Integer getVout() {
        return vout;
    }

    @JsonProperty("vout")
    public void setVout(Integer vout) {
        this.vout = vout;
    }

    @JsonProperty("scriptSig")
    public ScriptSig getScriptSig() {
        return scriptSig;
    }

    @JsonProperty("scriptSig")
    public void setScriptSig(ScriptSig scriptSig) {
        this.scriptSig = scriptSig;
    }

}
