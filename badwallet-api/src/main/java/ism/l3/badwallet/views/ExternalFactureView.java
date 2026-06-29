package ism.l3.badwallet.views;

import ism.l3.badwallet.services.proxy.PaymentServiceProxy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/external/factures")
public class ExternalFactureView {

    private final PaymentServiceProxy paymentServiceProxy;

    public ExternalFactureView(PaymentServiceProxy paymentServiceProxy) {
        this.paymentServiceProxy = paymentServiceProxy;
    }

    @GetMapping("/{walletCode}/current")
    public ResponseEntity<List<Map<String, Object>>> facturesMoisCourant(
            @PathVariable String walletCode,
            @RequestParam(required = false) String unite) {
        List<Map<String, Object>> factures = paymentServiceProxy
                .getFacturesMoisCourant(walletCode, unite);
        return ResponseEntity.ok(factures);
    }

    @GetMapping("/{walletCode}/periode")
    public ResponseEntity<List<Map<String, Object>>> facturesPeriode(
            @PathVariable String walletCode,
            @RequestParam String debut,
            @RequestParam String fin) {
        List<Map<String, Object>> factures = paymentServiceProxy
                .getFacturesPeriode(walletCode, debut, fin);
        return ResponseEntity.ok(factures);
    }
}
