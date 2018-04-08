package com.olexandrivchenko.bitcoinkiller.database.main;

import com.olexandrivchenko.bitcoinkiller.database.main.dto.CommandLineOperation;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class ParametersParser {

    public CommandLineOperation getOperation(String[] args) {
        if(args.length > 0){
            Set<String> params = new HashSet<>(Arrays.asList(args));
            if(params.contains("--help") || params.contains("-h")){
                return CommandLineOperation.HELP;
            }else if(params.contains("--test")){
                return CommandLineOperation.TEST;
            }
        }
        return CommandLineOperation.HELP;
    }
}
