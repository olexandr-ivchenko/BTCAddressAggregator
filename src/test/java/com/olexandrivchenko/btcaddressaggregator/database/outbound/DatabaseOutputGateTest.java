package com.olexandrivchenko.btcaddressaggregator.database.outbound;

import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.DbUpdateLog;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.impl.DatabaseOutputGateImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@ContextConfiguration(classes = {DatabaseOutputGateImpl.class})
@EnableAutoConfiguration
public class DatabaseOutputGateTest {

    @Autowired
    OutputGate out;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testGetFirstJobToProcess(){
        DbUpdateLog jobToProcess = out.getJobToProcess(100, true);
        assertEquals(Long.valueOf(0L), jobToProcess.getStartBlock());
        assertEquals(Long.valueOf(99L), jobToProcess.getEndBlock());
        DbUpdateLog dbUpdateLog = entityManager.find(DbUpdateLog.class, jobToProcess.getId());
        assertFalse(dbUpdateLog.isProcessed());
    }

    @Test
    public void testJobSaving(){
        DbUpdateLog jobToProcess = out.getJobToProcess(100, true);
        jobToProcess.setProcessed(true);
        out.runUpdate(new HashMap<>(), jobToProcess);
        DbUpdateLog dbUpdateLog = entityManager.find(DbUpdateLog.class, jobToProcess.getId());
        assertTrue(dbUpdateLog.isProcessed());
    }

    /**
     * Test for issue #3
     * See https://github.com/olexandr-ivchenko/BTCAddressAggregator/issues/3
     */
    @Test
    public void testSkipLastUnfinishedJob(){
        DbUpdateLog jobToProcess = out.getJobToProcess(100, true);
        assertEquals(Long.valueOf(0), jobToProcess.getStartBlock());
        jobToProcess = out.getJobToProcess(100, true);
        assertEquals(Long.valueOf(0), jobToProcess.getStartBlock());
    }
}
