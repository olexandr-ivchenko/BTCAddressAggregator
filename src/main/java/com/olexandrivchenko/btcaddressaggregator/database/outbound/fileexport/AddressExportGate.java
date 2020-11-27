package com.olexandrivchenko.btcaddressaggregator.database.outbound.fileexport;

import com.olexandrivchenko.btcaddressaggregator.database.outbound.AsyncOutputGateWrapper;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.dto.Address;
import com.olexandrivchenko.btcaddressaggregator.database.outbound.repository.AddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.stream.Stream;

@Service
public class AddressExportGate {
    private final static Logger log = LoggerFactory.getLogger(AddressExportGate.class);

    private final AddressRepository addressRepo;
    private final AsyncOutputGateWrapper asyncOutputGateWrapper;

    @PersistenceContext
    EntityManager entityManager;

    public AddressExportGate(AddressRepository addressRepo,
                             AsyncOutputGateWrapper asyncOutputGateWrapper) {
        this.addressRepo = addressRepo;
        this.asyncOutputGateWrapper = asyncOutputGateWrapper;
    }

    @Transactional(readOnly = true)
    public void exportDatabaseToFile() throws FileNotFoundException {
        log.info("Starting export");
        long blocks = asyncOutputGateWrapper.getNewJobStartPoint() - 1;
        PrintWriter out = new PrintWriter("export-" + blocks + ".log");
        out.println(blocks);
        Stream<Address> resultStream = addressRepo.streamAllAddress();
        log.info("Got result stream");
        resultStream.forEach(addr -> {
            out.println(addressToLine(addr));
            entityManager.clear();
        });
        out.close();
        log.info("Export done");
    }

    private String addressToLine(Address a) {
        return a.getAddress();
    }

}
