package ism.l3.payment.views;

import ism.l3.payment.entity.Facture;
import ism.l3.payment.entity.Unite;
import ism.l3.payment.services.FactureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/factures")
public class FactureView {

    private final FactureService factureService;

    public FactureView(FactureService factureService) {
        this.factureService = factureService;
    }
    @PostMapping("/init")
    public ResponseEntity<String> initialiserWallet(@RequestBody Map<String, String> body) {
        String walletCode = body.get("walletCode");
        String phoneNumber = body.get("phoneNumber");
        factureService.initialiserWallet(walletCode, phoneNumber);
        return ResponseEntity.ok("Wallet initialisé : " + walletCode);
    }

    @PostMapping("/init-historique")
    public ResponseEntity<String> initialiserWalletAvecHistorique(@RequestBody Map<String, Object> body) {
        String walletCode = (String) body.get("walletCode");
        String phoneNumber = (String) body.get("phoneNumber");
        int nombreMois = (Integer) body.get("nombreMois");
        factureService.initialiserWalletAvecHistorique(walletCode, phoneNumber, nombreMois);
        return ResponseEntity.ok("Wallet initialisé avec " + nombreMois + " mois : " + walletCode);
    }

    @GetMapping("/{walletCode}/current")
    public ResponseEntity<List<Facture>> facturesMoisCourant(
            @PathVariable String walletCode,
            @RequestParam(required = false) String unite) {

        List<Facture> factures;
        if (unite != null && !unite.isBlank()) {
            Unite uniteEnum = Unite.valueOf(unite.toUpperCase());
            factures = factureService.listerFacturesMoisCourantParService(walletCode, uniteEnum);
        } else {
            factures = factureService.listerFacturesMoisCourant(walletCode);
        }
        return ResponseEntity.ok(factures);
    }

    @GetMapping("/{walletCode}/periode")
    public ResponseEntity<List<Facture>> facturesPeriode(
            @PathVariable String walletCode,
            @RequestParam String debut,
            @RequestParam String fin) {
        String periodeDebut = debut.substring(0, 7);
        String periodeFin = fin.substring(0, 7);
        List<Facture> factures = factureService.listerFacturesPeriode(walletCode, periodeDebut, periodeFin);
        return ResponseEntity.ok(factures);
    }

    @PostMapping("/pay")
    public ResponseEntity<String> payerFactures(@RequestBody Map<String, List<String>> body) {
        List<String> references = body.get("references");
        factureService.payerFactures(references);
        return ResponseEntity.ok(references.size() + " facture(s) payée(s)");
    }
}
