package com.scalable.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@SuppressWarnings("serial")
public class OrderEntity implements Serializable {

    private Long id;
    private Integer orderId;
    private Integer userId;
    private String userName;
    private String passWord;
    private UserSexEnum userSex;
    private String nickName;

}
