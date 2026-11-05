package com.niit.quickcart;

import com.niit.quickcart.controller.OrderController;
import com.niit.quickcart.data.StoreCatalog;
import com.niit.quickcart.dto.Dtos.OrderRequest;
import com.niit.quickcart.exception.BadRequestException;
import com.niit.quickcart.model.Order;
import com.niit.quickcart.model.User;
import com.niit.quickcart.repository.OrderRepository;
import com.niit.quickcart.security.JwtUtil;
import com.niit.quickcart.service.ReceiptStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderPaymentTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @TempDir
    Path receiptsDir;

    @Test
    void rejectsOrderWithoutBankTransferPaymentMethod() {
        AtomicInteger saves = new AtomicInteger();
        OrderController controller = controller(saves);

        OrderRequest request = new OrderRequest();
        request.items = Map.of("1", 1);

        assertThrows(BadRequestException.class, () -> controller.placeOrder(token(), request, receipt(PNG, "image/png")));
        assertEquals(0, saves.get());
    }

    @Test
    void rejectsOrderWithoutReceipt() {
        AtomicInteger saves = new AtomicInteger();
        OrderController controller = controller(saves);

        assertThrows(BadRequestException.class, () -> controller.placeOrder(token(), validRequest(), null));
        assertThrows(BadRequestException.class,
                () -> controller.placeOrder(token(), validRequest(), receipt(new byte[0], "image/png")));
        assertEquals(0, saves.get());
    }

    @Test
    void rejectsReceiptThatIsNotAnImageOrPdf() {
        AtomicInteger saves = new AtomicInteger();
        OrderController controller = controller(saves);

        byte[] script = "<script>alert(1)</script>".getBytes();
        assertThrows(BadRequestException.class,
                () -> controller.placeOrder(token(), validRequest(), receipt(script, "image/png")));
        assertEquals(0, saves.get());
    }

    @Test
    void rejectsReceiptOverFiveMegabytes() {
        AtomicInteger saves = new AtomicInteger();
        OrderController controller = controller(saves);

        byte[] big = new byte[5 * 1024 * 1024 + 1];
        System.arraycopy(PNG, 0, big, 0, PNG.length);
        assertThrows(BadRequestException.class,
                () -> controller.placeOrder(token(), validRequest(), receipt(big, "image/png")));
        assertEquals(0, saves.get());
    }

    @Test
    void recordsAwaitingConfirmationAndStoresReceipt() throws Exception {
        AtomicInteger saves = new AtomicInteger();
        OrderController controller = controller(saves);

        Map<String, Object> response = controller.placeOrder(token(), validRequest(), receipt(PNG, "image/png"));
        Map<?, ?> order = (Map<?, ?>) response.get("order");

        assertEquals(42L, order.get("id"));
        assertEquals("awaiting_confirmation", order.get("paymentStatus"));
        assertEquals("pending_confirmation", order.get("status"));
        assertEquals(true, order.get("receiptUploaded"));
        assertEquals(1, saves.get());
        try (var files = Files.list(receiptsDir)) {
            assertEquals(1, files.filter(f -> f.toString().endsWith(".png")).count());
        }
    }

    private OrderController controller(AtomicInteger saves) {
        return new OrderController(repository(saves), new JwtUtil(), new ReceiptStorage(receiptsDir));
    }

    private static OrderRequest validRequest() {
        int productId = StoreCatalog.findByCity("Ikeja").get(0).getProducts().get(0).getId();
        OrderRequest request = new OrderRequest();
        request.paymentMethod = "BANK_TRANSFER";
        request.items = Map.of(String.valueOf(productId), 1);
        request.street = "15 Marina Road";
        request.city = "Ikeja";
        request.state = "Lagos";
        return request;
    }

    private static MockMultipartFile receipt(byte[] content, String type) {
        return new MockMultipartFile("receipt", "receipt.png", type, content);
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
