package ism.l3.badwallet.services.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class PaymentServiceProxyImpl implements PaymentServiceProxy {

    private final RestTemplate restTemplate;
    private final String paymentServiceUrl;

    public PaymentServiceProxyImpl(RestTemplate restTemplate,
                                    @Value("${payment.service.url}") String paymentServiceUrl) {
        this.restTemplate = restTemplate;
        this.paymentServiceUrl = paymentServiceUrl;
    }

    @Override
    public void initialiserWallet(String walletCode, String phoneNumber) {
        String url = paymentServiceUrl + "/api/factures/init";
        Map<String, String> body = new HashMap<>();
        body.put("walletCode", walletCode);
        body.put("phoneNumber", phoneNumber);
        restTemplate.postForEntity(url, body, String.class);
    }

    @Override
    public void initialiserWalletAvecHistorique(String walletCode, String phoneNumber, int nombreMois) {
        String url = paymentServiceUrl + "/api/factures/init-historique";
        Map<String, Object> body = new HashMap<>();
        body.put("walletCode", walletCode);
        body.put("phoneNumber", phoneNumber);
        body.put("nombreMois", nombreMois);
        restTemplate.postForEntity(url, body, String.class);
    }

    @Override
    public List<Map<String, Object>> getFacturesMoisCourant(String walletCode, String unite) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(paymentServiceUrl + "/api/factures/" + walletCode + "/current");
        if (unite != null && !unite.isBlank()) {
            builder.queryParam("unite", unite);
        }

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    @Override
    public List<Map<String, Object>> getFacturesPeriode(String walletCode, String debut, String fin) {
        String url = UriComponentsBuilder
                .fromHttpUrl(paymentServiceUrl + "/api/factures/" + walletCode + "/periode")
                .queryParam("debut", debut)
                .queryParam("fin", fin)
                .toUriString();

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    @Override
    public void payerFactures(List<String> references) {
        String url = paymentServiceUrl + "/api/factures/pay";
        Map<String, List<String>> body = new HashMap<>();
        body.put("references", references);
        restTemplate.postForEntity(url, body, String.class);
    }
}
