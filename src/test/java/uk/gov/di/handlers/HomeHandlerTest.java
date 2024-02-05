package uk.gov.di.handlers;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;
import uk.gov.di.config.RelyingPartyConfig;
import uk.gov.di.utils.ViewHelper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class HomeHandlerTest {
    private static final HomeHandler homeHandler = new HomeHandler();

    @Test
    void homeHandlerRendersWebScreenIfClientTypeIsWeb() {
        try (MockedStatic<RelyingPartyConfig> relyingPartyConfigMock =
                        mockStatic(RelyingPartyConfig.class);
                MockedStatic<ViewHelper> viewHelperMock = mockStatic(ViewHelper.class)) {
            relyingPartyConfigMock.when(RelyingPartyConfig::serviceName).thenReturn("Test Service");
            relyingPartyConfigMock.when(RelyingPartyConfig::clientType).thenReturn("web");

            var mockRequest = Mockito.mock(Request.class);
            var mockResponse = Mockito.mock(Response.class);

            homeHandler.handle(mockRequest, mockResponse);

            viewHelperMock.verify(() -> ViewHelper.render(any(), eq("home.mustache")));
        }
    }
}
