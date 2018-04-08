
package com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "asm",
    "hex",
    "reqSigs",
    "type",
    "addresses"
})
public class ScriptPubKey {

    @JsonProperty("asm")
    private String asm;
    @JsonProperty("hex")
    private String hex;
    @JsonProperty("reqSigs")
    private Long reqSigs;
    @JsonProperty("type")
    private String type;
    @JsonProperty("addresses")
    private List<String> addresses = null;

    @JsonProperty("asm")
    public String getAsm() {
        return asm;
    }

    @JsonProperty("asm")
    public void setAsm(String asm) {
        this.asm = asm;
    }

    @JsonProperty("hex")
    public String getHex() {
        return hex;
    }

    @JsonProperty("hex")
    public void setHex(String hex) {
        this.hex = hex;
    }

    @JsonProperty("reqSigs")
    public Long getReqSigs() {
        return reqSigs;
    }

    @JsonProperty("reqSigs")
    public void setReqSigs(Long reqSigs) {
        this.reqSigs = reqSigs;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("addresses")
    public List<String> getAddresses() {
        return addresses;
    }

    @JsonProperty("addresses")
    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

}
