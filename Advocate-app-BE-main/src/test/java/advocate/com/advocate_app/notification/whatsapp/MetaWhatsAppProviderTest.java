package advocate.com.advocate_app.communication.provider.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MetaWhatsAppProviderTest {

    private MetaWhatsAppProvider provider;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        provider = new MetaWhatsAppProvider(restTemplate, new ObjectMapper());

        ReflectionTestUtils.setField(provider, "apiBaseUrl", "https://graph.facebook.com/v23.0");
        ReflectionTestUtils.setField(provider, "phoneNumberId", "1235152873008482");
        ReflectionTestUtils.setField(provider, "accessToken", "test-access-token");
    }

    @Test
    void sendsMetaCompliantTextPayload() {
        server.expect(once(), requestTo("https://graph.facebook.com/v23.0/1235152873008482/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-access-token"))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                        {
                          "messaging_product": "whatsapp",
                          "to": "917867083865",
                          "type": "text",
                          "text": {
                            "body": "Hello SURYA,\\n\\nWelcome! You have been registered as a client with Advocate MANOJ."
                          }
                        }
                        """, true))
                .andRespond(withSuccess("{\"messages\":[{\"id\":\"wamid.test\"}]}", MediaType.APPLICATION_JSON));

        provider.sendMessage("7867083865",
                "Hello SURYA,\n\nWelcome! You have been registered as a client with Advocate MANOJ.");

        server.verify();
    }
}
