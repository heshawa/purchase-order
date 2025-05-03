package org.springboot.java17.api.order.dto;

import java.util.List;

import org.springboot.java17.api.order.util.OrderUtilConstants.OrderStatus;

public class OrderDTO {
	private String orderId;
	private String orderDate;
	private OrderStatus orderStatus;
	private List<OrderLineDTO> orderLineDTO;
	
	private String totalPrice;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public List<OrderLineDTO> getOrderLine() {
		return orderLineDTO;
	}

	public void setOrderLine(List<OrderLineDTO> orderLineDTO) {
		this.orderLineDTO = orderLineDTO;
	}

	public String getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(String totalPrice) {
		this.totalPrice = totalPrice;
	}
}
