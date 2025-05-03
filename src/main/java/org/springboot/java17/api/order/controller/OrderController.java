package org.springboot.java17.api.order.controller;

import org.springboot.java17.api.order.dto.OrderDTO;
import org.springboot.java17.api.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
	@PostMapping("/create")
	public ResponseEntity createOder(@RequestBody OrderDTO orderDto){
		if(orderDto == null || CollectionUtils.isEmpty(orderDto.getOrderLine())){
			log.warn("Order line is empty");
			return ResponseEntity.badRequest().body("Order line is empty");
		}
		//Reserve inventory		
		OrderDTO order = null;
		try {
			order = orderService.createOrder(orderDto);
		} catch (Exception e) {
			log.error("Error while creating order", e);
			return ResponseEntity.internalServerError().body("Error while creating order");
		}
		//Update payments
		//Post to queue for delivery
		return ResponseEntity.ok(order);
	}
}
