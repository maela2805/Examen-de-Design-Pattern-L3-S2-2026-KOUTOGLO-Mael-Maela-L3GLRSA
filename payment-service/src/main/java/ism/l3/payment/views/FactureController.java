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
public class FactureController {

    private final FactureService factureService;

    public FactureController(FactureService factureService) {
        this.factureService = factureService;
    }

    /**
     * Initialiser un ClientWallet et générer ses factures du mois courant.
     * Appelé par badwallet-api lors de la création d'un wallet.
     * POST /api/factures/init
     */
    @PostMapping("/init")
    public ResponseEntity<String> initialiserWallet(@RequestBody Map<String, String> body) {
        String walletCode = body.get("walletCode");
        String phoneNumber = body.get("phoneNumber");
        factureService.initialiserWallet(walletCode, phoneNumber);
        return ResponseEntity.ok("Wallet initialisé : " + walletCode);
    }

    /**
     * Initialiser un wallet avec un historique de N mois (pour le seeder).
     * POST /api/factures/init-historique
     */
    @PostMapping("/init-historique")
    public ResponseEntity<String> initialiserWalletAvecHistorique(@RequestBody Map<String, Object> body) {
        String walletCode = (String) body.get("walletCode");
        String phoneNumber = (String) body.get("phoneNumber");
        int nombreMois = (Integer) body.get("nombreMois");
        factureService.initialiserWalletAvecHistorique(walletCode, phoneNumber, nombreMois);
        return ResponseEntity.ok("Wallet initialisé avec " + nombreMois + " mois : " + walletCode);
    }

    /**
     * Factures impayées du mois en cours.
     * GET /api/factures/{walletCode}/current
     * GET /api/factures/{walletCode}/current?unite=WOYAFAL
     */
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

    /**
     * Factures sur une période donnée.
     * GET /api/factures/{walletCode}/periode?debut=2026-05-01&fin=2026-07-01
     */
    @GetMapping("/{walletCode}/periode")
    public ResponseEntity<List<Facture>> facturesPeriode(
            @PathVariable String walletCode,
            @RequestParam String debut,
            @RequestParam String fin) {

        // Convertir "2026-05-01" → "2026-05" pour correspondre au champ 'periode'
        String periodeDebut = debut.substring(0, 7);
        String periodeFin = fin.substring(0, 7);

        List<Facture> factures = factureService.listerFacturesPeriode(walletCode, periodeDebut, periodeFin);
        return ResponseEntity.ok(factures);
    }

    /**
     * Payer une ou plusieurs factures.
     * POST /api/factures/pay
     * Body: { "references": ["FAC-ISM-3-1", "FAC-ISM-3-3"] }
     */
    @PostMapping("/pay")
    public ResponseEntity<String> payerFactures(@RequestBody Map<String, List<String>> body) {
        List<String> references = body.get("references");
        factureService.payerFactures(references);
        return ResponseEntity.ok(references.size() + " facture(s) payée(s)");
    }
}
