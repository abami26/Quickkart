package com.niit.quickcart;

import com.niit.quickcart.dto.Dtos.OrderRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LagosAddressRequestTest {

    @Test
    void orderRequestCanComposeLagosAddressFromStreetCityAndState() {
        OrderRequest req = new OrderRequest();
        req.street = "15 Marina Road";
        req.city = "Lagos";
        req.state = "Lagos";

        String address = req.composeAddress();

        assertNotNull(address);
        assertEquals("15 Marina Road, Lagos, Lagos", address);
    }

}
