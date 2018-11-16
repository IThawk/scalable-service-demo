package com.scalable.c3shardingjdbc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.scalable.domain.OrderEntity;

@Mapper
public interface OrderMapper {

    List<OrderEntity> queryForList();

    OrderEntity query(Integer userId);

    OrderEntity queryById(Integer id);

	void insert(OrderEntity user);

}
