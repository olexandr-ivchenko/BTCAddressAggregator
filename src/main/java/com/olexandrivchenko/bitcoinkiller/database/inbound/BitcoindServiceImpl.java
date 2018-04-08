package com.olexandrivchenko.bitcoinkiller.database.inbound;

import com.fasterxml.jackson.core.type.TypeReference;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.JsonRpcRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class BitcoindServiceImpl {


    private String daemonUrl = "http://127.0.0.1:8332/";
    private String username = "user";
    private String password = "password";

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
//        T foo = template.postForObject(daemonUrl, request, clazz);

        GenericResponse<T> rs = template.exchange(daemonUrl,
                HttpMethod.POST,
                request,
                type).getBody();
        return rs;

    }

}
