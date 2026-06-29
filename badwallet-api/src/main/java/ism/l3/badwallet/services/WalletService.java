package ism.l3.badwallet.services;

import ism.l3.badwallet.entity.Transaction;
import ism.l3.badwallet.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WalletService {
    Wallet creerWallet(Wallet wallet);
    Page<Wallet> listerWallets(Pageable pageable);
    Optional<Wallet> recupererParTelephone(String phoneNumber);
    BigDecimal consulterSolde(String phoneNumber);
    void effectuerDepot(Long walletId, BigDecimal amount, String paymentMethod);
    void effectuerRetrait(String phoneNumber, BigDecimal amount);
    void effectuerTransfert(String senderPhone, String receiverPhone, BigDecimal amount);
    void payerFactureDuMois(String phoneNumber, String serviceName, BigDecimal amount);
    void payerFacturesSpecifiques(String phoneNumber, String serviceName, List<String> factureReferences);
    List<Transaction> listerTransactions(String phoneNumber);
}
