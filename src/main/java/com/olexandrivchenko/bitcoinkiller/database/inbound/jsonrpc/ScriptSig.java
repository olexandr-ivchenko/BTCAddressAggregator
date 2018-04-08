
package com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "asm",
    "hex"
})
public class ScriptSig {

    @JsonProperty("asm")
    private String asm;
    @JsonProperty("hex")
    private String hex;

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

}
