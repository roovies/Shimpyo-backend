package com.oneline.shimpyo.domain.house.dto;

import com.oneline.shimpyo.domain.house.HouseType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetHouseListRes {
    private long houseId;
    private String name;
    private HouseType type;
    private int price;
    private String contents;
    private long roomId;
    private double ratio;
    private boolean soldout;
    private List<String> houseImages;

    @QueryProjection
    public GetHouseListRes(long houseId, String name, HouseType type, int price, String contents, long roomId) {
        this.houseId = houseId;
        this.name = name;
        this.type = type;
        this.price = price;
        this.contents = contents;
        this.roomId = roomId;
        this.soldout = false;
    }
}