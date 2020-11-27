package com.olexandrivchenko.btcaddressaggregator.database.outbound.dto;

import javax.persistence.*;

@Entity
@Table(name = "address",
        indexes = {@Index(name="address_index", unique = true, columnList = "address")})
public class Address {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(length = 90,nullable = false, updatable = false)
    private String address;
    private double amount;
    private long lastSeenBlock;

    @Column(nullable = false, updatable = false)
    private long creationBlock;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public long getCreationBlock() {
        return creationBlock;
    }

    public void setCreationBlock(long creationBlock) {
        this.creationBlock = creationBlock;
    }
}
