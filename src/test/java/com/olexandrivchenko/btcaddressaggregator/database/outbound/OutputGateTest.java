package com.olexandrivchenko.btcaddressaggregator.database.outbound;

import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.DbUpdateLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {OutputGate.class})
public class OutputGateTest {

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
        assertEquals(false, dbUpdateLog.isProcessed());
    }

    @Test
    public void testJobSaving(){
        DbUpdateLog jobToProcess = out.getJobToProcess(100, true);
        jobToProcess.setProcessed(true);
        out.runUpdate(new HashMap<>(), jobToProcess);
        DbUpdateLog dbUpdateLog = entityManager.find(DbUpdateLog.class, jobToProcess.getId());
        assertEquals(true, dbUpdateLog.isProcessed());
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
