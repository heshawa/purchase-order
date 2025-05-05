package org.springboot.java17.api.order.service;

import org.springboot.java17.api.order.dto.OrderDTO;

public interface OrderService {
	OrderDTO createOrder(OrderDTO orderDTO) throws Exception;
	
	void publishOrderDetailsToTopic(OrderDTO orderDTO) throws Exception;
}
