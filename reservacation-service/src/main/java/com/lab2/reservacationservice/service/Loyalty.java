package com.lab2.reservacationservice.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class Loyalty {
    @Autowired
    RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "getFallback",commandProperties={
            @HystrixProperty(
                    name="execution.isolation.thread.timeoutInMilliseconds",
                    value="10000")
    })
    public Integer extracted(String username , UUID reservationUid) {
        Integer reservationCount = restTemplate.postForObject("http://localhost:8082/api/v1/reduceCount/{username}", username, Integer.class, username);
        return reservationCount;
    }
    public Integer getFallback(String username, UUID reservationUid){

        restTemplate.delete("http://localhost:8086/reservation-service/api/v1/" + reservationUid + "?username=" + username, username , reservationUid);
        return null;
    }
}
