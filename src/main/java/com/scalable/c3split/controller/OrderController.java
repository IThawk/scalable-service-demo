package com.scalable.c3split.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdeledu.domain.ServiceResult;
import com.scalable.c3split.core.SimpleSplitJdbcTemplate;
import com.scalable.domain.OrderEntity;

@RestController
public class OrderController {

	@Autowired
	private SimpleSplitJdbcTemplate simpleSplitJdbcTemplate;

	//http://127.0.0.10:8080/getid/1
	@RequestMapping("/getid/{id}")
	public ServiceResult<OrderEntity> getOrderById(@PathVariable("id") Long id) {
		OrderEntity order = simpleSplitJdbcTemplate.get(id, id, OrderEntity.class);
		return ServiceResult.getSuccessResult(order);
	}

	//http://127.0.0.10:8080/getname/2/zsl
	@RequestMapping("/getname/{id}/{name}")
	public ServiceResult<OrderEntity> getOrderByName(@PathVariable("id") Long id, @PathVariable("name") String name) {
		OrderEntity order = simpleSplitJdbcTemplate.get(id, "userName", name, OrderEntity.class);
		return ServiceResult.getSuccessResult(order);
	}

	//http://127.0.0.10:8080/getname2/2/zsl
	@RequestMapping("/getname2/{id}/{name}")
	public ServiceResult<List<OrderEntity>> getOrderByName2(@PathVariable("id") Long id, @PathVariable("name") String name) {
		String sql = "select * from test_msg.order_entity where userName=?";
		List<OrderEntity> orders = simpleSplitJdbcTemplate.query(id, sql, new String[]{name}, OrderEntity.class);
		return ServiceResult.getSuccessResult(orders);
	}

	//http://127.0.0.10:8080/add/18/zsl18
	@RequestMapping("/add/{id}/{name}")
	public ServiceResult<OrderEntity> add(@PathVariable("id") Long id, @PathVariable("name") String name) {
		OrderEntity obj = new OrderEntity();
		obj.setId(id);
		obj.setUserName(name);
		simpleSplitJdbcTemplate.insert(id, obj);
		OrderEntity order = simpleSplitJdbcTemplate.get(id, id, OrderEntity.class);
		return ServiceResult.getSuccessResult(order);
	}

	//http://127.0.0.10:8080/add2/19/zsl19
	@RequestMapping("/add2/{id}/{name}")
	public ServiceResult<OrderEntity> add2(@PathVariable("id") Long id, @PathVariable("name") String name) {
		simpleSplitJdbcTemplate.update(id, "insert into test_msg.order_entity(id, userName) values (?, ?)", new Object[] {id, name});
		OrderEntity order = simpleSplitJdbcTemplate.get(id, id, OrderEntity.class);
		return ServiceResult.getSuccessResult(order);
	}

	//http://127.0.0.10:8080/del/18
	@RequestMapping("/del/{id}")
	public ServiceResult<Boolean> del(@PathVariable("id") Long id) {
		simpleSplitJdbcTemplate.update(id, "delete from test_msg.order_entity where id = ?", new Object[] {id});
		OrderEntity order = simpleSplitJdbcTemplate.get(id, id, OrderEntity.class);
		return ServiceResult.getSuccessResult(true);
	}

	//http://127.0.0.10:8080/del2/19
	@RequestMapping("/del2/{id}")
	public ServiceResult<Boolean> del2(@PathVariable("id") Long id) {
		simpleSplitJdbcTemplate.delete(id, id, OrderEntity.class);
		return ServiceResult.getSuccessResult(true);
	}

	//http://127.0.0.10:8080/update/2/zsl100
	@RequestMapping("/update/{id}/{name}")
	public ServiceResult<OrderEntity> update(@PathVariable("id") Long id, @PathVariable("name") String name) {
		simpleSplitJdbcTemplate.update(id, "update test_msg.order_entity set userName = ? where id = ?", new Object[] {name, id});
		OrderEntity order = simpleSplitJdbcTemplate.get(id, id, OrderEntity.class);
		return ServiceResult.getSuccessResult(order);
	}

	//http://127.0.0.10:8080/update2/2/zsl101
	@RequestMapping("/update2/{id}/{name}")
	public ServiceResult<OrderEntity> update2(@PathVariable("id") Long id, @PathVariable("name") String name) {
		OrderEntity obj = new OrderEntity();
		obj.setId(id);
		obj.setUserName(name);
		simpleSplitJdbcTemplate.update(id, obj);
		OrderEntity order = simpleSplitJdbcTemplate.get(id, id, OrderEntity.class);
		return ServiceResult.getSuccessResult(order);
	}

}
