package com.scalable.c3shardingjdbc.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdeledu.domain.ServiceResult;
import com.scalable.c3shardingjdbc.service.OrderService;
import com.scalable.domain.OrderEntity;

@RestController
public class OrderController {

	@Autowired
	private OrderService orderService;

	@RequestMapping("/getall")
	public ServiceResult<List<OrderEntity>> getOrderAll() {
		return ServiceResult.getSuccessResult(orderService.queryForList());
	}

	@RequestMapping("/get/{userId}")
	public ServiceResult<OrderEntity> getOrder(@PathVariable("userId") Integer userId) {
		return ServiceResult.getSuccessResult(orderService.query(userId));
	}

	@RequestMapping("/getid/{id}")
	public ServiceResult<OrderEntity> getOrderById(@PathVariable("id") Integer id) {
		return ServiceResult.getSuccessResult(orderService.queryById(id));
	}

	@RequestMapping("/add/{userId}/{orderId}")
    public void addOrder(@PathVariable("userId") Integer userId, @PathVariable("orderId") Integer orderId) {
		OrderEntity entity10 = new OrderEntity();
        entity10.setOrderId(orderId);
        entity10.setUserId(userId);
        orderService.add(entity10);
    }

}
