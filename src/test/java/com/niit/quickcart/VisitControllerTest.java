package com.niit.quickcart;

import com.niit.quickcart.controller.VisitController;
import com.niit.quickcart.repository.SiteVisitorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class VisitControllerTest {

    @Test
    void doesNotTrackWithoutAcceptedConsent() {
        SiteVisitorRepository repository = mock(SiteVisitorRepository.class);
        when(repository.count()).thenReturn(0L);
        VisitController controller = new VisitController(repository);

        Map<String, Object> result = controller.countVisitor(null, null,
                new MockHttpServletRequest(), new MockHttpServletResponse());

        assertEquals(false, result.get("tracked"));
        verify(repository, never()).save(any());
    }

    @Test
    void createsOneHttpOnlyVisitorCookieAfterConsent() {
        SiteVisitorRepository repository = mock(SiteVisitorRepository.class);
        when(repository.existsById(anyString())).thenReturn(false);
        when(repository.count()).thenReturn(1L);
        VisitController controller = new VisitController(repository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSecure(true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        Map<String, Object> result = controller.countVisitor("accepted", null, request, response);

        assertEquals(true, result.get("tracked"));
        assertTrue(response.getHeader("Set-Cookie").contains("HttpOnly"));
        assertTrue(response.getHeader("Set-Cookie").contains("Secure"));
        verify(repository).save(any());
    }

    @Test
    void removesVisitorWhenConsentIsWithdrawn() {
        SiteVisitorRepository repository = mock(SiteVisitorRepository.class);
        when(repository.count()).thenReturn(0L);
        VisitController controller = new VisitController(repository);
        MockHttpServletResponse response = new MockHttpServletResponse();
        String visitorId = "3914f2be-8e48-4c6f-9b06-4adff2ea4e02";

        Map<String, Object> result = controller.countVisitor("declined", visitorId,
                new MockHttpServletRequest(), response);

        assertEquals(0L, result.get("uniqueVisitors"));
        assertTrue(response.getHeader("Set-Cookie").contains("Max-Age=0"));
        verify(repository).deleteById(visitorId);
    }
}