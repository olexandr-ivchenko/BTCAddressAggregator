package com.olexandrivchenko.bitcoinkiller.database.outbound.dto;

import javax.persistence.*;

@Entity
@Table(name = "dbupdatelog")
public class DbUpdateLog {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private Long startBlock;

    @Column(nullable = false)
    private Long endBlock;

    @Column
    private boolean processed;

    public Long getStartBlock() {
        return startBlock;
    }

    public void setStartBlock(Long startBlock) {
        this.startBlock = startBlock;
    }

    public Long getEndBlock() {
        return endBlock;
    }

    public void setEndBlock(Long endBlock) {
        this.endBlock = endBlock;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
