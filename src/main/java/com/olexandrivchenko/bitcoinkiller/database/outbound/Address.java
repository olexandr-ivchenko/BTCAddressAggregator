package com.olexandrivchenko.bitcoinkiller.database.outbound;

public class Address {

    private String address;
    private double amount;
    private long lastSeenBlock;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getLastSeenBlock() {
        return lastSeenBlock;
    }

    public void setLastSeenBlock(long lastSeenBlock) {
        this.lastSeenBlock = lastSeenBlock;
    }
}
