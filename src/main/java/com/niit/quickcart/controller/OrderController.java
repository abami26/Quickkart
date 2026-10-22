package com.niit.quickcart.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niit.quickcart.dto.Dtos.OrderRequest;
import com.niit.quickcart.exception.BadRequestException;
import com.niit.quickcart.data.StoreCatalog;
import com.niit.quickcart.model.Order;
import com.niit.quickcart.model.Product;
import com.niit.quickcart.data.ProductCatalog;
import com.niit.quickcart.repository.OrderRepository;
import com.niit.quickcart.security.JwtUtil;
import com.niit.quickcart.service.DemoOrderTracking;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderController(OrderRepository orderRepository, JwtUtil jwtUtil) {
        this.orderRepository = orderRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public Map<String, Object> placeOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody OrderRequest req) {

        Long userId = jwtUtil.requireUserId(authHeader);

        if (!"DEMO".equals(req.paymentMethod)) {
            throw new BadRequestException("Only the clearly labeled demo payment flow is available.");
        }
        if (req.items == null || req.items.isEmpty()) {
            throw new BadRequestException("Your cart is empty.");
        }

        String composedAddress = req.composeAddress();
        if (composedAddress == null || composedAddress.isBlank()) {
            throw new BadRequestException("Please provide a delivery address.");
        }

        String requestedState = req.state == null ? "" : req.state.trim().toLowerCase(Locale.ROOT);
        if (!"lagos".equals(requestedState)) {
            throw new BadRequestException("QuickCart delivery is available only in Lagos, Lagos State.");
        }
        if (req.city == null || req.city.isBlank()) {
            throw new BadRequestException("Please choose a Lagos city for delivery.");
        }

        Set<Integer> availableProductIds = StoreCatalog.findByCity(req.city).stream()
                .flatMap(store -> store.getProducts().stream())
                .map(Product::getId)
                .collect(java.util.stream.Collectors.toSet());
        if (availableProductIds.isEmpty()) {
            throw new BadRequestException("No partner shops deliver to this area yet.");
        }

        int subtotal = 0;
        List<Map<String, Object>> lineItems = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : req.items.entrySet()) {
            int productId = Integer.parseInt(entry.getKey());
            int qty = entry.getValue();
            if (!availableProductIds.contains(productId)) {
                throw new BadRequestException("Some items are not available in your delivery area.");
            }
            Product product = ProductCatalog.findById(productId);
            if (product == null) continue;

            subtotal += product.getPrice() * qty;

            Map<String, Object> line = new LinkedHashMap<>();
            line.put("id", product.getId());
            line.put("name", product.getName());
            line.put("price", product.getPrice());
            line.put("qty", qty);
            lineItems.add(line);
        }

        int deliveryFee = subtotal > 0 ? 800 : 0;

        Order order = new Order();
        order.setUserId(userId);
        order.setAddress(composedAddress);
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotal(subtotal + deliveryFee);
        order.setStatus("placed");
        order.setPaymentStatus("demo_only");
        order.setItemsJson(writeItemsJson(lineItems));

        order = orderRepository.save(order);

        return Map.of("order", toResponse(order));
    }

    @GetMapping("/mine")
    public Map<String, Object> myOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long userId = jwtUtil.requireUserId(authHeader);
        List<Order> orders = orderRepository.findByUserIdOrderByIdDesc(userId);

        List<Map<String, Object>> response = orders.stream().map(this::toResponse).toList();
        return Map.of("orders", response);
    }

    // ---------- helpers ----------

    private String writeItemsJson(List<Map<String, Object>> lineItems) {
        try {
            return objectMapper.writeValueAsString(lineItems);
        } catch (Exception e) {
            throw new RuntimeException("Could not save order items.", e);
        }
    }

    private List<Map<String, Object>> readItemsJson(String itemsJson) {
        try {
            return objectMapper.readValue(itemsJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, Object> toResponse(Order order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getId());
        map.put("status", order.getStatus());
        map.put("items", readItemsJson(order.getItemsJson()));
        map.put("address", order.getAddress());
        map.put("subtotal", order.getSubtotal());
        map.put("deliveryFee", order.getDeliveryFee());
        map.put("total", order.getTotal());
        map.put("paymentStatus", order.getPaymentStatus() == null ? "not_recorded" : order.getPaymentStatus());
        map.put("createdAt", order.getCreatedAt().toString());
        if ("demo_only".equals(order.getPaymentStatus())) {
            map.put("tracking", DemoOrderTracking.forOrder(order.getCreatedAt(), LocalDateTime.now()));
        }
        return map;
    }
}
