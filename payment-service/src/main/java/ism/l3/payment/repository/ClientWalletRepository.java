package ism.l3.payment.repository;

import ism.l3.payment.entity.ClientWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientWalletRepository extends JpaRepository<ClientWallet, Long> {

    Optional<ClientWallet> findByWalletCode(String walletCode);

    boolean existsByWalletCode(String walletCode);
}
