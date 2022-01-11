package com.lab2.reservacationservice.service;

import com.lab2.reservacationservice.data.UserPaymentResponse;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class UserPayment {
    @Autowired
    RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "getFallbackUserPayment")
    public UserPaymentResponse getUserPaymentResponse(UUID paymentUid) {
        UserPaymentResponse userPaymentResponse = restTemplate.getForObject("http://localhost:8083/payment/getUserPaymentResponse/" + paymentUid, UserPaymentResponse.class);
        return userPaymentResponse;
    }
    public UserPaymentResponse getFallbackUserPayment(UUID paymentUid){
        UserPaymentResponse userPaymentResponse1 = new UserPaymentResponse();
        userPaymentResponse1.setStatus(null);
        userPaymentResponse1.setPrice(null);
        return userPaymentResponse1;
    }
}
