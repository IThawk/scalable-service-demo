package com.scalable.c3shardingjdbc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scalable.c3shardingjdbc.dao.OrderMapper;
import com.scalable.domain.OrderEntity;

import java.util.List;

@Service
public class OrderService {

	@Autowired
	private OrderMapper orderMapper;

	public List<OrderEntity> queryForList() {
		return orderMapper.queryForList();
	}

	public OrderEntity query(Integer userId) {
		return orderMapper.query(userId);
	}

	public OrderEntity queryById(Integer id) {
		return orderMapper.queryById(id);
	}

	// @Transactional(value="test1TransactionManager",rollbackFor =
	// Exception.class,timeout=36000) //说明针对Exception异常也进行回滚，如果不标注，则Spring
	// 默认只有抛出 RuntimeException才会回滚事务
	public void add(OrderEntity user) {
		try {
			orderMapper.insert(user);
			System.out.println(String.valueOf(user));
		} catch (Exception e) {
			System.out.println("find exception!");
			throw e; // 事物方法中，如果使用trycatch捕获异常后，需要将异常抛出，否则事物不回滚。
		}

	}
}
