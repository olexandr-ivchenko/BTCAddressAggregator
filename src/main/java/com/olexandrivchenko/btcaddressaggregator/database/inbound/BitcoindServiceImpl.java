package com.olexandrivchenko.btcaddressaggregator.database.inbound;

import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.btcaddressaggregator.database.inbound.jsonrpc.JsonRpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BitcoindServiceImpl {
    private final static Logger log = LoggerFactory.getLogger(BitcoindServiceImpl.class);


    private String daemonUrl = "http://127.0.0.1:8332/";
    private String username = "user";
    private String password = "password";

    public BitcoindServiceImpl() {
    }

    public BitcoindServiceImpl(String daemonUrl, String username, String password) {
        this.daemonUrl = daemonUrl;
        this.username = username;
        this.password = password;
    }

    public <T> GenericResponse<T> call(String operation, List<Object> params, ParameterizedTypeReference<GenericResponse<T>> type){
        RestTemplate template = new RestTemplate();

        JsonRpcRequest rq = new JsonRpcRequest();
        rq.setMethod(operation);
        rq.setParams(params);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        template.getInterceptors().add(
                new BasicAuthorizationInterceptor(username, password));

        HttpEntity<JsonRpcRequest> request = new HttpEntity<>(rq,requestHeaders);

        GenericResponse<T> rs = null;
        int attempts = 0;
        do {
            attempts++;
            try {
                rs = template.exchange(daemonUrl,
                        HttpMethod.POST,
                        request,
                        type).getBody();
            } catch (Exception e) {
                StringBuilder paramString = new StringBuilder();
                Optional.ofNullable(params).orElse(new ArrayList<>())
                        .forEach(o -> paramString.append(o).append(" "));
                log.error("Failed to execute daemon call for operation {} and param {}",
                        operation,
                        paramString.toString());
                if(attempts > 2){
                    throw e;
                }else{
                    try {
                        synchronized (this) {
                            this.wait(5000);
                        }
                    } catch (InterruptedException e1) {
                        log.error("InterruptedException while waiting to retry", e1);
                    }
                }
            }
        }while(rs == null && attempts <= 3);
        return rs;
    }

}
