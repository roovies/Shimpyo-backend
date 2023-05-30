package com.oneline.shimpyo.domain.reservation;

import com.oneline.shimpyo.domain.pay.PayMent;
import com.oneline.shimpyo.domain.room.Room;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter @Setter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "NON_MEMBER_RESERVATION")
public class NonMemberReservation {
    @Id @GeneratedValue
    @Column(name = "non_member_id")
    private Long id;

    @NotNull
    @OneToOne(cascade = ALL)
    @JoinColumn(name = "pay_id")
    private PayMent payMent;

    @NotNull
    private String name;

    @NotNull
    private String phoneNumber;

    @NotNull
    @Enumerated
    private ReservationStatus status;

    @NotNull
    private LocalDateTime checkInDate;

    @NotNull
    private LocalDateTime checkOutDate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
}
