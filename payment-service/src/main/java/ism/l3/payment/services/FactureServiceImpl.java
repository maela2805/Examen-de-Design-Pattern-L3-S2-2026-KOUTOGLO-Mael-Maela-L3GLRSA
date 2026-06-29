package ism.l3.payment.services;

import ism.l3.payment.entity.*;
import ism.l3.payment.repository.ClientWalletRepository;
import ism.l3.payment.repository.FactureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class FactureServiceImpl implements FactureService {

    // Montants fixes par service (en XOF)
    private static final BigDecimal MONTANT_ISM = new BigDecimal("5000");
    private static final BigDecimal MONTANT_WOYAFAL = new BigDecimal("15000");

    private final ClientWalletRepository clientWalletRepository;
    private final FactureRepository factureRepository;

    public FactureServiceImpl(ClientWalletRepository clientWalletRepository,
                               FactureRepository factureRepository) {
        this.clientWalletRepository = clientWalletRepository;
        this.factureRepository = factureRepository;
    }

    @Override
    @Transactional
    public void initialiserWallet(String walletCode, String phoneNumber) {
        // Vérifier si le ClientWallet existe déjà
        if (clientWalletRepository.existsByWalletCode(walletCode)) {
            return;
        }

        ClientWallet clientWallet = new ClientWallet(walletCode, phoneNumber);
        clientWalletRepository.save(clientWallet);

        // Générer les factures du mois en cours pour chaque service
        YearMonth moisCourant = YearMonth.now();
        genererFacturesPourMois(clientWallet, moisCourant);
    }

    @Override
    @Transactional
    public void initialiserWalletAvecHistorique(String walletCode, String phoneNumber, int nombreMois) {
        if (clientWalletRepository.existsByWalletCode(walletCode)) {
            return;
        }

        ClientWallet clientWallet = new ClientWallet(walletCode, phoneNumber);
        clientWalletRepository.save(clientWallet);

        // Générer les factures sur N mois passés (du plus ancien au plus récent)
        YearMonth moisCourant = YearMonth.now();
        for (int i = nombreMois - 1; i >= 0; i--) {
            YearMonth mois = moisCourant.minusMonths(i);
            genererFacturesPourMois(clientWallet, mois);
        }
    }

    /**
     * Génère une facture par service pour un mois donné si elle n'existe pas déjà.
     */
    private void genererFacturesPourMois(ClientWallet clientWallet, YearMonth mois) {
        String periode = mois.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate dateFact = mois.atDay(1);

        for (Unite unite : Unite.values()) {
            // Éviter les doublons
            if (factureRepository.existsByClientWallet_WalletCodeAndPeriodeAndUnite(
                    clientWallet.getWalletCode(), periode, unite)) {
                continue;
            }

            // Numéro séquentiel de la facture pour ce wallet + service
            long compteur = factureRepository.countByClientWallet_WalletCodeAndUnite(
                    clientWallet.getWalletCode(), unite) + 1;

            // Extraire le numéro du wallet : "WLT-0000003" → "3"
            String walletNumero = extraireNumeroWallet(clientWallet.getWalletCode());

            // Format : FAC-ISM-3-1
            String reference = String.format("FAC-%s-%s-%d", unite.name(), walletNumero, compteur);

            BigDecimal montant = getMontantParUnite(unite);

            Facture facture = new Facture(reference, montant, dateFact, periode, unite, clientWallet);
            factureRepository.save(facture);
        }
    }

    @Override
    public List<Facture> listerFacturesMoisCourant(String walletCode) {
        String periode = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return factureRepository.findByClientWallet_WalletCodeAndStatutAndPeriode(
                walletCode, StatutFacture.IMPAYEE, periode);
    }

    @Override
    public List<Facture> listerFacturesMoisCourantParService(String walletCode, Unite unite) {
        String periode = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return factureRepository.findByClientWallet_WalletCodeAndStatutAndPeriodeAndUnite(
                walletCode, StatutFacture.IMPAYEE, periode, unite);
    }

    @Override
    public List<Facture> listerFacturesPeriode(String walletCode, String debut, String fin) {
        return factureRepository.findByClientWallet_WalletCodeAndPeriodeBetween(
                walletCode, debut, fin);
    }

    @Override
    @Transactional
    public void payerFactures(List<String> references) {
        for (String reference : references) {
            factureRepository.findByReference(reference).ifPresent(facture -> {
                if (facture.getStatut() == StatutFacture.IMPAYEE) {
                    facture.setStatut(StatutFacture.PAYEE);
                    factureRepository.save(facture);
                }
            });
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private String extraireNumeroWallet(String walletCode) {
        // "WLT-0000003" → supprime les zéros → "3"
        return walletCode.replace("WLT-", "").replaceFirst("^0+(?!$)", "");
    }

    private BigDecimal getMontantParUnite(Unite unite) {
        return switch (unite) {
            case ISM -> MONTANT_ISM;
            case WOYAFAL -> MONTANT_WOYAFAL;
        };
    }
}
