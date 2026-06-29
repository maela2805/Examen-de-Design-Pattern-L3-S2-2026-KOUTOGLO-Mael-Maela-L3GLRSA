package ism.l3.badwallet.repository;

import ism.l3.badwallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByWallet_PhoneNumberOrderByCreatedAtDesc(String phoneNumber);
}
