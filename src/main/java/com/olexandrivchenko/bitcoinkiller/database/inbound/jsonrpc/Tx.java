
package com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "txid",
    "hash",
    "version",
    "size",
    "vsize",
    "locktime",
    "vin",
    "vout",
    "hex"
})
public class Tx {

    @JsonProperty("txid")
    private String txid;
    @JsonProperty("hash")
    private String hash;
    @JsonProperty("version")
    private Long version;
    @JsonProperty("size")
    private Long size;
    @JsonProperty("vsize")
    private Long vsize;
    @JsonProperty("locktime")
    private Long locktime;
    @JsonProperty("vin")
    private List<Vin> vin = null;
    @JsonProperty("vout")
    private List<Vout> vout = null;
    @JsonProperty("hex")
    private String hex;

    @JsonProperty("txid")
    public String getTxid() {
        return txid;
    }

    @JsonProperty("txid")
    public void setTxid(String txid) {
        this.txid = txid;
    }

    @JsonProperty("hash")
    public String getHash() {
        return hash;
    }

    @JsonProperty("hash")
    public void setHash(String hash) {
        this.hash = hash;
    }

    @JsonProperty("version")
    public Long getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(Long version) {
        this.version = version;
    }

    @JsonProperty("size")
    public Long getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(Long size) {
        this.size = size;
    }

    @JsonProperty("vsize")
    public Long getVsize() {
        return vsize;
    }

    @JsonProperty("vsize")
    public void setVsize(Long vsize) {
        this.vsize = vsize;
    }

    @JsonProperty("locktime")
    public Long getLocktime() {
        return locktime;
    }

    @JsonProperty("locktime")
    public void setLocktime(Long locktime) {
        this.locktime = locktime;
    }

    @JsonProperty("vin")
    public List<Vin> getVin() {
        return vin;
    }

    @JsonProperty("vin")
    public void setVin(List<Vin> vin) {
        this.vin = vin;
    }

    @JsonProperty("vout")
    public List<Vout> getVout() {
        return vout;
    }

    @JsonProperty("vout")
    public void setVout(List<Vout> vout) {
        this.vout = vout;
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
