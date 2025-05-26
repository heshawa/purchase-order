package org.springboot.java17.api.order.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springboot.java17.api.ResponseMessage;
import org.springboot.java17.api.common.constant.OrderStatus;
import org.springboot.java17.api.common.dto.ItemDTO;
import org.springboot.java17.api.common.dto.OrderDTO;
import org.springboot.java17.api.common.dto.OrderLineDTO;
import org.springboot.java17.api.order.model.Order;
import org.springboot.java17.api.order.model.OrderLine;
import org.springboot.java17.api.order.model.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService{
	
	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private WebClient webClient;

	@Autowired
	private KafkaTemplate kafkaTemplate;
	
	@Value("${springboot.purchase.order.topic}")
	private String topicName;

	@Override
	public OrderDTO createOrder(OrderDTO orderDTO) throws Exception {
		if(orderDTO == null || CollectionUtils.isEmpty(orderDTO.getOrderLine())) {
			return null;
		}
		
		Order newOrder = convertToOrder(orderDTO);
		newOrder.setOrderDate(new Date());
		newOrder.setOrderStatus(OrderStatus.NEW);
		
		Order createdOrder = orderRepository.save(newOrder);
		
		
		return convertToOderDTO(createdOrder);
		
	}

	@Override
	public void publishOrderDetailsToTopic(OrderDTO orderDTO) throws Exception {
		kafkaTemplate.send(topicName,orderDTO.getOrderId().toString(),orderDTO);
	}

	@Override
	public ResponseMessage<ItemDTO> makeInventoryReservation(List<ItemDTO> orderItems) throws Exception {
		return webClient.post().uri("/allocate")
				.contentType(MediaType.APPLICATION_JSON).bodyValue(orderItems)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ResponseMessage<ItemDTO>>() {})
				.block();

	}

	private Order convertToOrder(OrderDTO orderDTO) throws ParseException {
		Order order = new Order();
		if(!StringUtils.isEmpty(orderDTO.getOrderId())){
			order.setOrderId(Integer.parseInt(orderDTO.getOrderId()));
		}
		
		if(!StringUtils.isEmpty(orderDTO.getOrderDate())){
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust format as needed
			order.setOrderDate(dateFormat.parse(orderDTO.getOrderDate()));		}
		
		if(!StringUtils.isEmpty(orderDTO.getTotalPrice())){
			order.setTotalPrice(new BigDecimal(orderDTO.getTotalPrice()));
		}

		order.setOrderStatus(orderDTO.getOrderStatus());
		if (!CollectionUtils.isEmpty(orderDTO.getOrderLine())) {
			orderDTO.getOrderLine().stream()
					.map(orderLineDTO -> convertToOrderLine(orderLineDTO))
					.forEach(orderLine -> {
						if(order.getOrderLine() == null){
							order.setOrderLine(new ArrayList());
						}
						order.getOrderLine().add(orderLine);
					});
		}
		
		return order;
	}
	
	private OrderDTO convertToOderDTO(Order order){
		OrderDTO orderDTO = new OrderDTO();
		orderDTO.setOrderId(String.valueOf(order.getOrderId()));
		orderDTO.setOrderDate(String.valueOf(order.getOrderDate()));
		orderDTO.setOrderStatus(order.getOrderStatus());
		orderDTO.setTotalPrice(String.valueOf(order.getTotalPrice()));
		
		if (!CollectionUtils.isEmpty(order.getOrderLine())) {
			order.getOrderLine().stream().map(orderLine -> convertToOrderLineDTO(orderLine))
					.forEach(orderLineDTO -> {
						if(orderDTO.getOrderLine() == null){
							orderDTO.setOrderLine(new ArrayList());
						}
						orderDTO.getOrderLine().add(orderLineDTO);
					});
		}
		
		return orderDTO;
	}
	
	private OrderLineDTO convertToOrderLineDTO(OrderLine orderLine) {
		OrderLineDTO orderLineDTO = new OrderLineDTO();
		orderLineDTO.setItemId(orderLine.getItemId());
		orderLineDTO.setName(orderLine.getName());
		orderLineDTO.setQuantity(orderLine.getQuantity());
		orderLineDTO.setPrice(orderLine.getPrice());
		orderLineDTO.setTotalPrice(orderLine.getTotalPrice());
		
		return orderLineDTO;
	}
	
	private OrderLine convertToOrderLine(OrderLineDTO orderLineDTO) {
		OrderLine orderLine = new OrderLine();
		orderLine.setItemId(orderLineDTO.getItemId());
		orderLine.setName(orderLineDTO.getName());
		orderLine.setQuantity(orderLineDTO.getQuantity());
		orderLine.setPrice(orderLineDTO.getPrice());
		orderLine.setTotalPrice(orderLineDTO.getTotalPrice());
		
		return orderLine;
	}
}
