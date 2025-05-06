package org.springboot.java17.api.order.service;

import java.util.List;

import org.springboot.java17.api.ResponseMessage;
import org.springboot.java17.api.common.dto.ItemDTO;
import org.springboot.java17.api.order.dto.OrderDTO;

public interface OrderService {
	OrderDTO createOrder(OrderDTO orderDTO) throws Exception;
	
	void publishOrderDetailsToTopic(OrderDTO orderDTO) throws Exception;
	
	ResponseMessage makeInventoryReservation(List<ItemDTO> orderItems) throws Exception;
}
