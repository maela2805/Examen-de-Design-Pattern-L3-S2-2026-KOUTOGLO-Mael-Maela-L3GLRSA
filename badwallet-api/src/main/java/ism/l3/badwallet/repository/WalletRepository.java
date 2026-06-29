package ism.l3.badwallet.repository;

import ism.l3.badwallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByCode(String code);
}
