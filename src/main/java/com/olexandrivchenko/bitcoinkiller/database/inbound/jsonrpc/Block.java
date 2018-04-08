
package com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "hash",
    "confirmations",
    "strippedsize",
    "size",
    "weight",
    "height",
    "version",
    "versionHex",
    "merkleroot",
    "tx",
    "time",
    "mediantime",
    "nonce",
    "bits",
    "difficulty",
    "chainwork",
    "previousblockhash",
    "nextblockhash"
})
public class Block {

    @JsonProperty("hash")
    private String hash;
    @JsonProperty("confirmations")
    private Long confirmations;
    @JsonProperty("strippedsize")
    private Long strippedsize;
    @JsonProperty("size")
    private Long size;
    @JsonProperty("weight")
    private Long weight;
    @JsonProperty("height")
    private Long height;
    @JsonProperty("version")
    private Long version;
    @JsonProperty("versionHex")
    private String versionHex;
    @JsonProperty("merkleroot")
    private String merkleroot;
    @JsonProperty("tx")
    private List<Tx> tx = null;
    @JsonProperty("time")
    private Long time;
    @JsonProperty("mediantime")
    private Long mediantime;
    @JsonProperty("nonce")
    private Long nonce;
    @JsonProperty("bits")
    private String bits;
    @JsonProperty("difficulty")
    private Double difficulty;
    @JsonProperty("chainwork")
    private String chainwork;
    @JsonProperty("previousblockhash")
    private String previousblockhash;
    @JsonProperty("nextblockhash")
    private String nextblockhash;

    @JsonProperty("hash")
    public String getHash() {
        return hash;
    }

    @JsonProperty("hash")
    public void setHash(String hash) {
        this.hash = hash;
    }

    @JsonProperty("confirmations")
    public Long getConfirmations() {
        return confirmations;
    }

    @JsonProperty("confirmations")
    public void setConfirmations(Long confirmations) {
        this.confirmations = confirmations;
    }

    @JsonProperty("strippedsize")
    public Long getStrippedsize() {
        return strippedsize;
    }

    @JsonProperty("strippedsize")
    public void setStrippedsize(Long strippedsize) {
        this.strippedsize = strippedsize;
    }

    @JsonProperty("size")
    public Long getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(Long size) {
        this.size = size;
    }

    @JsonProperty("weight")
    public Long getWeight() {
        return weight;
    }

    @JsonProperty("weight")
    public void setWeight(Long weight) {
        this.weight = weight;
    }

    @JsonProperty("height")
    public Long getHeight() {
        return height;
    }

    @JsonProperty("height")
    public void setHeight(Long height) {
        this.height = height;
    }

    @JsonProperty("version")
    public Long getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(Long version) {
        this.version = version;
    }

    @JsonProperty("versionHex")
    public String getVersionHex() {
        return versionHex;
    }

    @JsonProperty("versionHex")
    public void setVersionHex(String versionHex) {
        this.versionHex = versionHex;
    }

    @JsonProperty("merkleroot")
    public String getMerkleroot() {
        return merkleroot;
    }

    @JsonProperty("merkleroot")
    public void setMerkleroot(String merkleroot) {
        this.merkleroot = merkleroot;
    }

    @JsonProperty("tx")
    public List<Tx> getTx() {
        return tx;
    }

    @JsonProperty("tx")
    public void setTx(List<Tx> tx) {
        this.tx = tx;
    }

    @JsonProperty("time")
    public Long getTime() {
        return time;
    }

    @JsonProperty("time")
    public void setTime(Long time) {
        this.time = time;
    }

    @JsonProperty("mediantime")
    public Long getMediantime() {
        return mediantime;
    }

    @JsonProperty("mediantime")
    public void setMediantime(Long mediantime) {
        this.mediantime = mediantime;
    }

    @JsonProperty("nonce")
    public Long getNonce() {
        return nonce;
    }

    @JsonProperty("nonce")
    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    @JsonProperty("bits")
    public String getBits() {
        return bits;
    }

    @JsonProperty("bits")
    public void setBits(String bits) {
        this.bits = bits;
    }

    @JsonProperty("difficulty")
    public Double getDifficulty() {
        return difficulty;
    }

    @JsonProperty("difficulty")
    public void setDifficulty(Double difficulty) {
        this.difficulty = difficulty;
    }

    @JsonProperty("chainwork")
    public String getChainwork() {
        return chainwork;
    }

    @JsonProperty("chainwork")
    public void setChainwork(String chainwork) {
        this.chainwork = chainwork;
    }

    @JsonProperty("previousblockhash")
    public String getPreviousblockhash() {
        return previousblockhash;
    }

    @JsonProperty("previousblockhash")
    public void setPreviousblockhash(String previousblockhash) {
        this.previousblockhash = previousblockhash;
    }

    @JsonProperty("nextblockhash")
    public String getNextblockhash() {
        return nextblockhash;
    }

    @JsonProperty("nextblockhash")
    public void setNextblockhash(String nextblockhash) {
        this.nextblockhash = nextblockhash;
    }

}
