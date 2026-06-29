package ism.l3.badwallet.views;

import ism.l3.badwallet.entity.Transaction;
import ism.l3.badwallet.entity.Wallet;
import ism.l3.badwallet.services.WalletService;
import ism.l3.badwallet.services.SeederService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
public class WalletView {

    private final WalletService walletService;
    private final SeederService seederService;

    public WalletView(WalletService walletService, SeederService seederService) {
        this.walletService = walletService;
        this.seederService = seederService;
    }

    @PostMapping("/seed")
    public ResponseEntity<String> seed(
            @RequestParam(defaultValue = "10") int numWallets,
            @RequestParam(defaultValue = "100") int eventsPerWallet) {
        seederService.seeder(numWallets, eventsPerWallet);
        return ResponseEntity.accepted().body(
                "Seeding lancé en arrière-plan : " + numWallets + " wallets, " + eventsPerWallet + " événements/wallet");
    }

    @PostMapping
    public ResponseEntity<Wallet> creerWallet(@RequestBody Map<String, Object> body) {
        Wallet wallet = new Wallet(
                (String) body.get("phoneNumber"),
                (String) body.get("email"),
                (String) body.get("code"),
                body.getOrDefault("currency", "XOF").toString(),
                new BigDecimal(body.get("initialBalance").toString())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.creerWallet(wallet));
    }

    @GetMapping
    public ResponseEntity<Page<Wallet>> listerWallets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(walletService.listerWallets(PageRequest.of(page, size)));
    }

    @GetMapping("/{phoneNumber}")
    public ResponseEntity<Wallet> consulterWallet(@PathVariable String phoneNumber) {
        return walletService.recupererParTelephone(phoneNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{phoneNumber}/balance")
    public ResponseEntity<Map<String, Object>> consulterSolde(@PathVariable String phoneNumber) {
        BigDecimal solde = walletService.consulterSolde(phoneNumber);
        return ResponseEntity.ok(Map.of(
                "phoneNumber", phoneNumber,
                "balance", solde
        ));
    }


    @PostMapping("/{id}/deposit")
    public ResponseEntity<String> effectuerDepot(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String paymentMethod = (String) body.get("paymentMethod");
        walletService.effectuerDepot(id, amount, paymentMethod);
        return ResponseEntity.ok("Dépôt effectué avec succès");
    }


    @PostMapping("/withdraw")
    public ResponseEntity<String> effectuerRetrait(@RequestBody Map<String, Object> body) {
        String phoneNumber = (String) body.get("phoneNumber");
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        walletService.effectuerRetrait(phoneNumber, amount);
        return ResponseEntity.ok("Retrait effectué avec succès");
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> effectuerTransfert(@RequestBody Map<String, Object> body) {
        String senderPhone = (String) body.get("senderPhone");
        String receiverPhone = (String) body.get("receiverPhone");
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        walletService.effectuerTransfert(senderPhone, receiverPhone, amount);
        return ResponseEntity.ok("Transfert effectué avec succès");
    }

    @PostMapping("/pay")
    public ResponseEntity<String> payerFactureMois(@RequestBody Map<String, Object> body) {
        String phoneNumber = (String) body.get("phoneNumber");
        String serviceName = (String) body.get("serviceName");
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        walletService.payerFactureDuMois(phoneNumber, serviceName, amount);
        return ResponseEntity.ok("Paiement de la facture " + serviceName + " effectué");
    }

    @PostMapping("/pay-factures")
    public ResponseEntity<String> payerFacturesSpecifiques(@RequestBody Map<String, Object> body) {
        String phoneNumber = (String) body.get("phoneNumber");
        String serviceName = (String) body.get("serviceName");
        @SuppressWarnings("unchecked")
        List<String> references = (List<String>) body.get("factureReferences");
        walletService.payerFacturesSpecifiques(phoneNumber, serviceName, references);
        return ResponseEntity.ok(references.size() + " facture(s) payée(s) pour " + serviceName);
    }

    
    @GetMapping("/{phoneNumber}/transactions")
    public ResponseEntity<List<Transaction>> listerTransactions(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(walletService.listerTransactions(phoneNumber));
    }
}
