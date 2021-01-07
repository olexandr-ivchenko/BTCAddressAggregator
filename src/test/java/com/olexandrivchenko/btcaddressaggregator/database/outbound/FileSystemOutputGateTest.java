package com.olexandrivchenko.btcaddressaggregator.database.outbound;

import com.olexandrivchenko.btcaddressaggregator.database.configuration.FileSystemOutputGateImplTestConfig;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.Address;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.DbUpdateLog;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.impl.AddressCsvRenderer;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.impl.FileSystemOutputGateImpl;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {FileSystemOutputGateImpl.class,
        AddressCsvRenderer.class,
        FileSystemOutputGateImplTestConfig.class})
public class FileSystemOutputGateTest {

    @Autowired
    private FileSystemOutputGateImpl gate;

    @Autowired
    private AddressCsvRenderer renderer;

    @Autowired
    private String baseOutputFolder;

    @BeforeEach
    public void cleanTempDir() throws IOException {
        FileUtils.cleanDirectory(new File(baseOutputFolder));
    }

    @Test
    public void testRunUpdate_success() throws IOException {
        DbUpdateLog job = new DbUpdateLog();
        job.setStartBlock(0L);
        job.setEndBlock(1000L);
        Map<String, Address> addressesMap = generateAddressMap(100);
        gate.runUpdate(addressesMap, job);
        //read and validate

        String jobFolderName = job.getStartBlock() + "-" + job.getEndBlock();
        BufferedReader reader = new BufferedReader(new FileReader(Paths.get(baseOutputFolder, jobFolderName, jobFolderName + ".csv").toFile()));
        String line;
        while ((line = reader.readLine()) != null) {
            Address fsAddr = renderer.decodeAddress(line);
            Address origAddr = addressesMap.get(fsAddr.getAddress());
            addressesMap.remove(fsAddr.getAddress());
            if (!fsAddr.getAddress().equals(origAddr.getAddress()) ||
                    fsAddr.getCreationBlock() != origAddr.getCreationBlock() ||
                    fsAddr.getLastSeenBlock() != origAddr.getLastSeenBlock() ||
                    fsAddr.getAmount() != origAddr.getAmount()) {
                fail("Written address differs from saved one");
            }
        }
        assertEquals(0, addressesMap.size(),
                "After deleting all records found in FS from generated address map - map should be empty");
        reader.close();
    }

    @Test
    public void testGetJobToProcess(){
        //get first job
        DbUpdateLog firstJob = gate.getJobToProcess(1000, false);
        assertEquals(0, firstJob.getStartBlock());
        assertEquals(999, firstJob.getEndBlock());

        //get new job
        DbUpdateLog job2 = gate.getJobToProcess(1000, false);
        assertEquals(1000, job2.getStartBlock());
        assertEquals(1999, job2.getEndBlock());

        //get new job
        DbUpdateLog job3 = gate.getJobToProcess(500, false);
        assertEquals(2000, job3.getStartBlock());
        assertEquals(2499, job3.getEndBlock());

    }

    private Map<String, Address> generateAddressMap(int num) {
        Map<String, Address> result = new HashMap<>();
        for (int i = 0; i < num; i++) {
            Address a = new Address();
            a.setAddress("1TestAddressNumber" + i);
            a.setAmount(i * 1.1);
            a.setCreationBlock(i);
            a.setLastSeenBlock(i);
            result.put(a.getAddress(), a);
        }
        return result;
    }

}
