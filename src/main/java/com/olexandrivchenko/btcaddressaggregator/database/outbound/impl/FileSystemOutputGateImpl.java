package com.olexandrivchenko.btcaddressaggregator.database.outbound.impl;

import com.olexandrivchenko.btcaddressaggregator.database.outbound.OutputGate;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.Address;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.DbUpdateLog;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("FileSystemOutputGateImpl")
public class FileSystemOutputGateImpl implements OutputGate {

    @Autowired
    private AddressCsvRenderer csvRenderer;

    @Value("${baseOutputFolder}")
    private String baseOutputFolder;

    private static final String tempFolderName = "temp";
    private static final Pattern resultFolderPattern = Pattern.compile("[0-9]{1,8}-([0-9]{1,8})");

    private Long newJobStartPoint = -1L;

    @PostConstruct
    public void initTempFolder() throws IOException {
        Path tempPath = Paths.get(baseOutputFolder, tempFolderName);
        if (Files.exists(tempPath)) {
            FileUtils.cleanDirectory(tempPath.toFile());
        }
    }

    @Override
    public DbUpdateLog getJobToProcess(int size, boolean skipUnfinished) {
        DbUpdateLog job = new DbUpdateLog();
        long newJobStartPoint = getNewJobStartPoint(skipUnfinished);
        job.setStartBlock(newJobStartPoint);
        job.setEndBlock(job.getStartBlock() + size - 1);
        this.newJobStartPoint = job.getEndBlock()+1;
        return job;
    }

    @Override
    public long getNewJobStartPoint(boolean skipUnfinished) {
        if(newJobStartPoint > 0){
            return newJobStartPoint;
        }
        File baseFolder = new File(baseOutputFolder);
        String[] list = baseFolder.list();
        long maxEndPoint = -1L;
        for(String folder : list){
            Matcher m = resultFolderPattern.matcher(folder);
            if(m.matches()){
                long endPoint = Long.parseLong(m.group(0));
                if(endPoint > maxEndPoint){
                    maxEndPoint = endPoint;
                }
            }
        }
        return maxEndPoint+1;
    }

    @Override
    public void runUpdate(Map<String, Address> addressesMap, DbUpdateLog job) {
        String jobFolderName = job.getStartBlock() + "-" + job.getEndBlock();
        try {
            //create temp folder+file and write input
            Path tmpFilePath = Paths.get(baseOutputFolder, tempFolderName, jobFolderName, jobFolderName + ".csv");
            Files.createDirectories(tmpFilePath.getParent());
            Files.createFile(tmpFilePath);
            FileWriter writer = new FileWriter(tmpFilePath.toFile());
            for (Address a : addressesMap.values()) {
                writer.write(csvRenderer.renderAddress(a));
            }
            writer.close();
            //done

            //move saved file to correct place
//            Path jobFolder = Files.createDirectories(Paths.get(baseOutputFolder, jobFolderName));
            Path jobResultFolder = Paths.get(baseOutputFolder, jobFolderName);
            Files.move(tmpFilePath.getParent(), jobResultFolder);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save file", e);
        }

    }
}
