package com.lab2.reservacationservice.controller;


import com.lab2.reservacationservice.data.*;
import com.lab2.reservacationservice.dao.ReservationDAOImpl;
import com.lab2.reservacationservice.service.Loyalty;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ReservationController {

    @Autowired
    ReservationDAOImpl reservationDAOImpl;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    Loyalty loyalty;


    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationData>> getReservations(@RequestParam(value = "username", required = false) String username){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Name",username);
        return ResponseEntity.status(200).headers(headers).body(reservationDAOImpl.getReservations(username));

    }

    @GetMapping("/{reservationUid}")
    public ResponseEntity<ReservationData> getReservationById(@PathVariable("reservationUid") UUID reservationUid,@RequestParam(value = "username", required = false) String username){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Name",username);
        return ResponseEntity.status(200).headers(headers).body(reservationDAOImpl.getReservationById(username,reservationUid));
    }

    @PostMapping("/reservations")
    @HystrixCommand(fallbackMethod = "getFallbackCreateReservation")
    public ResponseEntity<CreateReservationResponse> createReservation(@RequestParam(value = "username", required = false) String username, @RequestParam(value = "hotelUid", required = false) UUID hotelUid, @RequestParam(value = "startDate", required = false) Date startDate, @RequestParam(value = "endDate", required = false) Date endDate){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Name",username);
        return ResponseEntity.status(201).headers(headers).body(reservationDAOImpl.createReservation(username,hotelUid,startDate,endDate));
    }
    public ResponseEntity<CreateReservationResponse> getFallbackCreateReservation(@RequestParam(value = "username", required = false) String username, @RequestParam(value = "hotelUid", required = false) UUID hotelUid, @RequestParam(value = "startDate", required = false) Date startDate, @RequestParam(value = "endDate", required = false) Date endDate){
        return new ResponseEntity<CreateReservationResponse>(null , null , HttpStatus.SERVICE_UNAVAILABLE);
    }

    @DeleteMapping("/{reservationUid}")
    public ResponseEntity<ReservationStatusResponse> cancelReservation(@PathVariable("reservationUid") UUID reservationUid, @RequestParam(value = "username", required = false) String username){
        //set reservation status
        jdbcTemplate.update(
                "update reservation set status='CANCELED' where reservationUid =:reservationUid ",
                new MapSqlParameterSource()
                        .addValue("reservationUid", reservationUid));

        //set payment status
        PaymentUidResponse paymentUidResponse = jdbcTemplate.queryForObject(
                "select paymentUid from reservation where reservationUid = :reservationUid",
                new MapSqlParameterSource()
                        .addValue("reservationUid", reservationUid),
                new BeanPropertyRowMapper<>(PaymentUidResponse.class));
        UUID paymentUid = paymentUidResponse.getPaymentUid();
        String paymentStatus = restTemplate.postForObject("http://localhost:8083/payment/cancel/{paymentUid}", paymentUid, String.class, paymentUid);
        if (paymentStatus == null){
            System.exit(1);
            return new ResponseEntity<>(null,null,HttpStatus.SERVICE_UNAVAILABLE);
        }
        //update loyalty
        Integer count = loyalty.extracted(username , reservationUid);
        String Status = restTemplate.postForObject("http://localhost:8082/api/v1/updateStatus/{username}", username, String.class, username);

        //response
        ReservationStatusResponse reservationStatusResponse = jdbcTemplate.queryForObject("select status from reservation where reservationUid =:reservationUid",
                new MapSqlParameterSource()
                        .addValue("reservationUid", reservationUid),
                new BeanPropertyRowMapper<>(ReservationStatusResponse.class));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Name",username);
        return ResponseEntity.status(200).headers(headers).body(reservationStatusResponse);
    }



    @GetMapping("/reservationUid/{username}")
    public UUID getReservationUid(@PathVariable("username") String username){
        ReservationUidResponse reservationUidResponse = reservationDAOImpl.getReservationUid(username);
        UUID uuid = reservationUidResponse.getReservationUid();
        return uuid;
    }




}
