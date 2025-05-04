package org.springboot.java17.api.order.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springboot.java17.api.ResponseMessage;
import org.springboot.java17.api.order.dto.ItemDTO;
import org.springboot.java17.api.order.dto.OrderDTO;
import org.springboot.java17.api.order.dto.OrderLineDTO;
import org.springboot.java17.api.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private WebClient webClient;
	
	@PostMapping("/create")
	public ResponseEntity createOder(@RequestBody OrderDTO orderDto){
		if(orderDto == null || CollectionUtils.isEmpty(orderDto.getOrderLine())){
			log.warn("Order line is empty");
			return ResponseEntity.badRequest().body("Order line is empty");
		}
		//Reserve inventory
		//List<Integer> itemIds = orderDto.getOrderLine().stream().map(OrderLineDTO::getItemId).toList();
		List<ItemDTO> allocateOrder = orderDto.getOrderLine().stream()
				.map(orderLine->convertToItemDTO(orderLine))
				.collect(Collectors.toList());

		ResponseMessage<ItemDTO> response = null;

		try{
			response = webClient.post().uri("/allocate")
					.contentType(MediaType.APPLICATION_JSON).bodyValue(allocateOrder)
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<ResponseMessage<ItemDTO>>() {})
					.block();
		}catch (Exception ex){
			log.error("Error while calling inventory API. Content: {}",allocateOrder.toString(), ex);
			return ResponseEntity.internalServerError().body(new ResponseMessage("Error while calling inventory API. " + ex.getMessage()));
		}
		
		if(!response.isSuccess() || CollectionUtils.isEmpty(response.getData())){
			log.warn("No items found for the given order");
			return ResponseEntity.badRequest().body("No items found for the given order");
		}

		response.getData().stream().forEach(item -> {
			OrderLineDTO orderedItem = orderDto.getOrderLine().stream().filter(orderLine -> orderLine.getItemId() == item.getId())
					.findFirst().orElse(null);
			orderedItem.setName(item.getName());
			orderedItem.setPrice(Double.parseDouble(item.getPrice()));
			orderedItem.setTotalPrice(orderedItem.getPrice() * orderedItem.getQuantity());
		});
		
		double orderTotal = orderDto.getOrderLine().stream().map(OrderLineDTO::getTotalPrice).reduce(0.0, Double::sum);
		
		orderDto.setTotalPrice(String.valueOf(orderTotal));
		
		OrderDTO order = null;
		try {
			order = orderService.createOrder(orderDto);
		} catch (Exception e) {
			log.error("Error while creating order", e);
			return ResponseEntity.internalServerError().body("Error while creating order");
		}
		//Update payments
		//Post to queue for delivery
		ResponseMessage message = new ResponseMessage("");
		message.getData().add(order);
		return ResponseEntity.ok(order);
	}
	
	private ItemDTO convertToItemDTO(OrderLineDTO orderLineDTO){
		ItemDTO itemDto = new ItemDTO();
		itemDto.setId(orderLineDTO.getItemId());
		itemDto.setQuantity(String.valueOf(orderLineDTO.getQuantity()));
		
		return itemDto;
	}
}
