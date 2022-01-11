package com.lab2.reservacationservice.controller;

import com.lab2.reservacationservice.dao.HotelsDAOImpl;
import com.lab2.reservacationservice.data.*;
import com.lab2.reservacationservice.service.UserLoyalty;
import com.lab2.reservacationservice.service.UserPayment;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;


@RestController
public class UserController {
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    UserPayment payment;

    @Autowired
    UserLoyalty userLoyalty;

    @GetMapping("/api/v1/me")
    public ResponseEntity<UserData> getReservations(@RequestParam(value = "username", required = false) String username){
        //get hotel response
        HotelIdResponse hotelIdResponse = jdbcTemplate.queryForObject(
                "select hotelId from reservation where username = :username",
                new MapSqlParameterSource()
                        .addValue("username", username),
                new BeanPropertyRowMapper<>(HotelIdResponse.class));
        Integer hotelId = hotelIdResponse.getHotelId();
        HotelsDAOImpl hotelsDAO1 = new HotelsDAOImpl();
        UserHotelsResponse userHotelsResponse = hotelsDAO1.getUserHotelsResponse(hotelId);

        //get payment response
        PaymentUidResponse paymentUidResponse = jdbcTemplate.queryForObject(
                "select paymentUid from reservation where username =:username",
                new MapSqlParameterSource()
                        .addValue("username", username),
                new BeanPropertyRowMapper<>(PaymentUidResponse.class));
        UUID paymentUid = paymentUidResponse.getPaymentUid();
        UserPaymentResponse userPaymentResponse = payment.getUserPaymentResponse(paymentUid);

        //get reservation response
        UserReservationResponse userReservationResponse = new UserReservationResponse();
        UserReservationResponseAdd userReservationResponseAdd = jdbcTemplate.queryForObject(
                "select reservationUid,startDate,endDate,status from reservation where username = :username",
                new MapSqlParameterSource()
                        .addValue("username", username),
                new BeanPropertyRowMapper<>(UserReservationResponseAdd.class));
        userReservationResponse.setUserReservationResponseAdd(userReservationResponseAdd);
        userReservationResponse.setUserHotelsResponse(userHotelsResponse);
        userReservationResponse.setUserPaymentResponse(userPaymentResponse);

        //get loyalty response
        UserLoyaltyResponse userLoyaltyResponse = userLoyalty.getUserLoyaltyResponse(username);

        //get response
        UserData userData = new UserData();
        userData.setUserLoyaltyResponse(userLoyaltyResponse);
        userData.setUserReservationResponse(userReservationResponse);


        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Name",username);
        return ResponseEntity.status(200).headers(headers).body(userData);
    }


}
