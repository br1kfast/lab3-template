package com.lab2.reservacationservice.service;

import com.lab2.reservacationservice.data.UserLoyaltyResponse;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserLoyalty {
    @Autowired
    RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "getFallbackUserLoyalty")
    public UserLoyaltyResponse getUserLoyaltyResponse(String username) {
        UserLoyaltyResponse userLoyaltyResponse = restTemplate.getForObject("http://localhost:8082/api/v1/getUserLoyalty/" + username, UserLoyaltyResponse.class);
        return userLoyaltyResponse;
    }
    public UserLoyaltyResponse getFallbackUserLoyalty(String username){
        UserLoyaltyResponse userLoyaltyResponse1 = new UserLoyaltyResponse();
        userLoyaltyResponse1.setDiscount(null);
        userLoyaltyResponse1.setStatus(null);
        return userLoyaltyResponse1;
    }

}
