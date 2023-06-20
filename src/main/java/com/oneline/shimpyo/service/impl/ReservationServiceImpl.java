package com.oneline.shimpyo.service.impl;

import com.oneline.shimpyo.domain.BaseException;
import com.oneline.shimpyo.domain.GetPageRes;
import com.oneline.shimpyo.domain.house.HouseImage;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.member.MemberGrade;
import com.oneline.shimpyo.domain.pay.PayMent;
import com.oneline.shimpyo.domain.reservation.Reservation;
import com.oneline.shimpyo.domain.reservation.ReservationStatus;
import com.oneline.shimpyo.domain.reservation.dto.*;
import com.oneline.shimpyo.domain.room.Room;
import com.oneline.shimpyo.repository.*;
import com.oneline.shimpyo.repository.dsl.MyCouponQuerydsl;
import com.oneline.shimpyo.repository.dsl.ReservationQuerydsl;
import com.oneline.shimpyo.service.PaymentService;
import com.oneline.shimpyo.service.ReservationService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.oneline.shimpyo.domain.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private final PaymentService paymentService;
    private final ReservationRepository reservationRepository;
    private final ReservationQuerydsl reservationQuerydsl;
    private final MemberRepository memberRepository;
    private final HouseImageRepository houseImageRepository;
    private final RoomRepository roomRepository;
    private final MyCouponQuerydsl myCouponQuerydsl;

    @Override
    public GetPrepareReservationRes prepareReservation(long memberId) throws BaseException {
        String uuid = UUID.randomUUID().toString();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));
        MemberGrade memberGrade = member.getMemberGrade();
        List<CouponReq> myCouponList = myCouponQuerydsl.getMyCouponList(memberId);

        return new GetPrepareReservationRes(uuid, memberGrade.getGrade().getRank(), memberGrade.getDiscount(), myCouponList);
    }

    @Transactional
    @Override
    public long createReservation(long memberId, PostReservationReq postReservationReq)
            throws BaseException, IamportResponseException, IOException {
        PayMent payment = paymentService.createPayment(memberId, postReservationReq);

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));
        Room room = roomRepository.findById(postReservationReq.getRoomId())
                .orElseThrow(() -> new BaseException(ROOM_NONEXISTENT));

        Reservation reservation = Reservation.builder().room(room).member(member).payMent(payment)
                .peopleCount(postReservationReq.getPeopleCount()).phoneNumber(postReservationReq.getPhoneNumber())
                .reservationStatus(ReservationStatus.COMPLETE)
                .checkInDate(postReservationReq.stringToLocalDateTime(postReservationReq.getCheckInDate()))
                .checkOutDate(postReservationReq.stringToLocalDateTime(postReservationReq.getCheckOutDate()))
                .build();
        reservationRepository.save(reservation);

        return reservation.getId();
    }

    @Override
    public GetPageRes<GetReservationListRes> readReservationList(long memberId, Pageable pageable) {
        memberRepository.findById(memberId).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));

        return new GetPageRes<>(reservationQuerydsl.readReservationList(memberId, pageable));
    }

    @Override
    public GetReservationRes readReservation(long memberId, long reservationId) {
        memberRepository.findById(memberId).orElseThrow(() -> new BaseException(MEMBER_NONEXISTENT));
        reservationRepository.findById(reservationId).orElseThrow(() -> new BaseException(RESERVATION_NONEXISTENT));

        GetReservationRes getReservationRes = reservationQuerydsl.readReservation(reservationId);
        List<HouseImage> houseImageList = houseImageRepository.findByHouseId(getReservationRes.getHouseId());
        getReservationRes.setHouseImageUrl(houseImageList.stream().map(HouseImage::getSavedURL).collect(Collectors.toList()));

        return getReservationRes;
    }

    @Transactional
    @Override
    public void cancelReservation(long memberId, long reservationId, PatchReservationReq patchReservationReq)
            throws BaseException, IamportResponseException, IOException {
        Reservation reservation = paymentService.cancelPayment(memberId, reservationId, patchReservationReq);

        reservation.setReservationStatus(ReservationStatus.CANCEL);
    }

}
