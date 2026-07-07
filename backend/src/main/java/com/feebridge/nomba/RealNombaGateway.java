package com.feebridge.nomba;

import com.feebridge.common.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Calls the real Nomba API (used when {@code feebridge.nomba.enabled=true}). Obtains an OAuth2
 * client-credentials token and creates a dynamic virtual account via {@code POST /v1/accounts/virtual}.
 * Endpoint paths / field names follow developer.nomba.com; verify against your account's docs.
 */
@Component
@ConditionalOnProperty(name = "feebridge.nomba.enabled", havingValue = "true")
public class RealNombaGateway implements NombaGateway {

    private static final Logger log = LoggerFactory.getLogger(RealNombaGateway.class);

    private final NombaProperties props;
    private final RestClient client;

    private volatile String cachedToken;
    private volatile Instant tokenExpiry = Instant.EPOCH;

    public RealNombaGateway(NombaProperties props) {
        this.props = props;
        this.client = RestClient.builder().baseUrl(props.baseUrl()).build();
    }

    @Override
    public VirtualAccountResult createVirtualAccount(String accountRef, String accountName, Money expectedAmount,
                                                     Instant expiry) {
        Map<String, Object> body = Map.of(
                "accountRef", accountRef,
                "accountName", accountName,
                "currency", "NGN",
                "expectedAmount", expectedAmount.toNaira(),
                "expiryDate", expiry.toString());

        @SuppressWarnings("unchecked")
        Map<String, Object> response = client.post()
                .uri("/v1/accounts/virtual")
                .header("Authorization", "Bearer " + token())
                .header("accountId", props.accountId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        Map<String, Object> data = asMap(response == null ? null : response.get("data"), response);
        String number = str(data.get("bankAccountNumber"));
        String name = str(data.getOrDefault("bankAccountName", accountName));
        String bank = str(data.getOrDefault("bankName", "Nomba"));
        if (number == null) {
            log.warn("Nomba createVirtualAccount returned no account number: {}", response);
            throw new IllegalStateException("Nomba did not return a virtual account number");
        }
        return new VirtualAccountResult(number, name, bank, expiry);
    }

    private synchronized String token() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", props.clientId());
        form.add("client_secret", props.clientSecret());

        @SuppressWarnings("unchecked")
        Map<String, Object> response = client.post()
                .uri("/v1/auth/token/issue")
                .header("accountId", props.accountId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        Map<String, Object> data = asMap(response == null ? null : response.get("data"), response);
        String accessToken = str(data.getOrDefault("access_token", data.get("accessToken")));
        if (accessToken == null) {
            throw new IllegalStateException("Nomba token response missing access token");
        }
        cachedToken = accessToken;
        tokenExpiry = Instant.now().plus(50, ChronoUnit.MINUTES);
        return accessToken;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value, Map<String, Object> fallback) {
        return value instanceof Map ? (Map<String, Object>) value : (fallback == null ? Map.of() : fallback);
    }

    private String str(Object value) {
        return value == null ? null : value.toString();
    }
}
