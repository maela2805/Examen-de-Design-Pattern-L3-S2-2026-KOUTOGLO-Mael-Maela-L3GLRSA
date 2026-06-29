package ism.l3.payment.repository;

import ism.l3.payment.entity.Facture;
import ism.l3.payment.entity.StatutFacture;
import ism.l3.payment.entity.Unite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByClientWallet_WalletCode(String walletCode);

    List<Facture> findByClientWallet_WalletCodeAndStatutAndPeriode(
            String walletCode, StatutFacture statut, String periode);

    List<Facture> findByClientWallet_WalletCodeAndStatutAndPeriodeAndUnite(
            String walletCode, StatutFacture statut, String periode, Unite unite);

    List<Facture> findByClientWallet_WalletCodeAndStatutAndPeriodeBetween(
            String walletCode, StatutFacture statut, String debut, String fin);

    List<Facture> findByClientWallet_WalletCodeAndPeriodeBetween(
            String walletCode, String debut, String fin);

    Optional<Facture> findByReference(String reference);

    boolean existsByClientWallet_WalletCodeAndPeriodeAndUnite(
            String walletCode, String periode, Unite unite);

    long countByClientWallet_WalletCodeAndUnite(String walletCode, Unite unite);
}
