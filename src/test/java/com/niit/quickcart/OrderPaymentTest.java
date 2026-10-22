package com.niit.quickcart;

import com.niit.quickcart.controller.OrderController;
import com.niit.quickcart.data.StoreCatalog;
import com.niit.quickcart.dto.Dtos.OrderRequest;
import com.niit.quickcart.exception.BadRequestException;
import com.niit.quickcart.model.Order;
import com.niit.quickcart.model.User;
import com.niit.quickcart.repository.OrderRepository;
import com.niit.quickcart.security.JwtUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderPaymentTest {

    @Test
    void rejectsOrderWithoutDemoPaymentMethod() {
        AtomicInteger saves = new AtomicInteger();
        OrderController controller = new OrderController(repository(saves), new JwtUtil());

        OrderRequest request = new OrderRequest();
        request.items = Map.of("1", 1);

        assertThrows(BadRequestException.class, () -> controller.placeOrder(token(), request));
        assertEquals(0, saves.get());
    }

    @Test
    void recordsDemoOnlyPaymentStatusForOrder() {
        AtomicInteger saves = new AtomicInteger();
        OrderController controller = new OrderController(repository(saves), new JwtUtil());

        int productId = StoreCatalog.findByCity("Ikeja").get(0).getProducts().get(0).getId();
        OrderRequest request = new OrderRequest();
        request.paymentMethod = "DEMO";
        request.items = Map.of(String.valueOf(productId), 1);
        request.street = "15 Marina Road";
        request.city = "Ikeja";
        request.state = "Lagos";

        Map<String, Object> response = controller.placeOrder(token(), request);
        Map<?, ?> order = (Map<?, ?>) response.get("order");

        assertEquals(42L, order.get("id"));
        assertEquals("demo_only", order.get("paymentStatus"));
        assertEquals(1, saves.get());
    }

    private static OrderRepository repository(AtomicInteger saves) {
        return (OrderRepository) Proxy.newProxyInstance(
                OrderRepository.class.getClassLoader(),
                new Class<?>[]{OrderRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("save")) {
                        saves.incrementAndGet();
                        Order order = (Order) args[0];
                        order.setId(42L);
                        return order;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static String token() {
        User user = new User("Test User", "test@example.com", "unused");
        user.setId(1L);
        return "Bearer " + new JwtUtil().generateToken(user);
    }
}
