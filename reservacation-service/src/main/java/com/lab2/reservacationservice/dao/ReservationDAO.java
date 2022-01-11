package com.lab2.reservacationservice.dao;

import com.lab2.reservacationservice.data.*;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface ReservationDAO {

    List<ReservationData> getReservations(String username);
    ReservationData getReservationById(String username, UUID reservationUid);
    CreateReservationResponse createReservation(String name, UUID hotelUid, Date startDate, Date endDate);
    ReservationUidResponse getReservationUid(String username);

}
