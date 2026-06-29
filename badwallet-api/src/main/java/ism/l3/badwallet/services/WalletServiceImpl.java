package ism.l3.badwallet.services;

import ism.l3.badwallet.entity.Transaction;
import ism.l3.badwallet.entity.TransactionType;
import ism.l3.badwallet.entity.Wallet;
import ism.l3.badwallet.events.TransactionEvent;
import ism.l3.badwallet.exception.InsufficientBalanceException;
import ism.l3.badwallet.exception.WalletNotFoundException;
import ism.l3.badwallet.repository.TransactionRepository;
import ism.l3.badwallet.repository.WalletRepository;
import ism.l3.badwallet.services.deposit.DepositStrategy;
import ism.l3.badwallet.services.deposit.DepositStrategyFactory;
import ism.l3.badwallet.services.proxy.PaymentServiceProxy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WalletServiceImpl implements WalletService {

    private static final BigDecimal TAUX_FRAIS = new BigDecimal("0.01");
    private static final BigDecimal PLAFOND_FRAIS = new BigDecimal("5000");

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final DepositStrategyFactory depositStrategyFactory;
    private final PaymentServiceProxy paymentServiceProxy;
    private final ApplicationEventPublisher eventPublisher;

    public WalletServiceImpl(WalletRepository walletRepository,
                              TransactionRepository transactionRepository,
                              DepositStrategyFactory depositStrategyFactory,
                              PaymentServiceProxy paymentServiceProxy,
                              ApplicationEventPublisher eventPublisher) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.depositStrategyFactory = depositStrategyFactory;
        this.paymentServiceProxy = paymentServiceProxy;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Wallet creerWallet(Wallet wallet) {
        Wallet saved = walletRepository.save(wallet);

        try {
            paymentServiceProxy.initialiserWallet(saved.getCode(), saved.getPhoneNumber());
        } catch (Exception e) {
            System.err.println("Avertissement : impossible d'initialiser les factures - " + e.getMessage());
        }
        return saved;
    }

    @Override
    public Page<Wallet> listerWallets(Pageable pageable) {
        return walletRepository.findAll(pageable);
    }

    @Override
    public Optional<Wallet> recupererParTelephone(String phoneNumber) {
        return walletRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public BigDecimal consulterSolde(String phoneNumber) {
        Wallet wallet = walletRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new WalletNotFoundException("Wallet introuvable : " + phoneNumber));
        return wallet.getBalance();
    }

    @Override
    @Transactional
    public void effectuerDepot(Long walletId, BigDecimal amount, String paymentMethod) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet introuvable : id=" + walletId));
        DepositStrategy strategy = depositStrategyFactory.getStrategy(paymentMethod);
        strategy.effectuerDepot(wallet, amount);

        walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void effectuerRetrait(String phoneNumber, BigDecimal amount) {
        Wallet wallet = walletRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new WalletNotFoundException("Wallet introuvable : " + phoneNumber));
        BigDecimal frais = amount.multiply(TAUX_FRAIS).min(PLAFOND_FRAIS);
        BigDecimal totalDebit = amount.add(frais);
        if (wallet.getBalance().compareTo(totalDebit) < 0) {
            throw new InsufficientBalanceException(
                    "Solde insuffisant. Solde : " + wallet.getBalance() +
                    " | Montant + frais : " + totalDebit);
        }

        wallet.setBalance(wallet.getBalance().subtract(totalDebit));
        walletRepository.save(wallet);
        eventPublisher.publishEvent(new TransactionEvent(
                this, wallet, TransactionType.WITHDRAWAL,
                amount, frais,
                "Retrait de " + amount + " " + wallet.getCurrency() + " (frais : " + frais + ")"
        ));
    }

    @Override
    @Transactional
    public void effectuerTransfert(String senderPhone, String receiverPhone, BigDecimal amount) {
        if (senderPhone.equals(receiverPhone)) {
            throw new IllegalArgumentException("Impossible de transférer vers le même wallet");
        }

        Wallet sender = walletRepository.findByPhoneNumber(senderPhone)
                .orElseThrow(() -> new WalletNotFoundException("Expéditeur introuvable : " + senderPhone));
        Wallet receiver = walletRepository.findByPhoneNumber(receiverPhone)
                .orElseThrow(() -> new WalletNotFoundException("Destinataire introuvable : " + receiverPhone));

        BigDecimal frais = amount.multiply(TAUX_FRAIS).min(PLAFOND_FRAIS);
        BigDecimal totalDebit = amount.add(frais);

        if (sender.getBalance().compareTo(totalDebit) < 0) {
            throw new InsufficientBalanceException(
                    "Solde insuffisant pour le transfert. Solde : " + sender.getBalance() +
                    " | Montant + frais : " + totalDebit);
        }
        sender.setBalance(sender.getBalance().subtract(totalDebit));
        walletRepository.save(sender);

        receiver.setBalance(receiver.getBalance().add(amount));
        walletRepository.save(receiver);
        eventPublisher.publishEvent(new TransactionEvent(
                this, sender, TransactionType.TRANSFER_SEND,
                amount, frais,
                "Transfert vers " + receiverPhone + " - Montant : " + amount + " (frais : " + frais + ")"
        ));
        eventPublisher.publishEvent(new TransactionEvent(
                this, receiver, TransactionType.TRANSFER_RECEIVE,
                amount, BigDecimal.ZERO,
                "Transfert reçu de " + senderPhone + " - Montant : " + amount
        ));
    }

    @Override
    @Transactional
    public void payerFactureDuMois(String phoneNumber, String serviceName, BigDecimal amount) {
        Wallet wallet = walletRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new WalletNotFoundException("Wallet introuvable : " + phoneNumber));
        List<Map<String, Object>> factures = paymentServiceProxy
                .getFacturesMoisCourant(wallet.getCode(), serviceName);

        if (factures == null || factures.isEmpty()) {
            throw new RuntimeException("Aucune facture impayée ce mois pour le service : " + serviceName);
        }
        BigDecimal total = factures.stream()
                .map(f -> new BigDecimal(f.get("montant").toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (wallet.getBalance().compareTo(total) < 0) {
            throw new InsufficientBalanceException(
                    "Solde insuffisant pour payer les factures. Solde : " + wallet.getBalance() +
                    " | Total factures : " + total);
        }
        wallet.setBalance(wallet.getBalance().subtract(total));
        walletRepository.save(wallet);
        List<String> references = factures.stream()
                .map(f -> f.get("reference").toString())
                .toList();
        paymentServiceProxy.payerFactures(references);
        eventPublisher.publishEvent(new TransactionEvent(
                this, wallet, TransactionType.PAYMENT,
                total, BigDecimal.ZERO,
                "Paiement facture(s) " + serviceName + " du mois courant : " + total + " " + wallet.getCurrency()
        ));
    }

    @Override
    @Transactional
    public void payerFacturesSpecifiques(String phoneNumber, String serviceName, List<String> factureReferences) {
        Wallet wallet = walletRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new WalletNotFoundException("Wallet introuvable : " + phoneNumber));

        List<Map<String, Object>> toutesFactures = paymentServiceProxy
                .getFacturesMoisCourant(wallet.getCode(), null);
        List<Map<String, Object>> facturesDemandees = toutesFactures.stream()
                .filter(f -> factureReferences.contains(f.get("reference").toString()))
                .toList();

        BigDecimal total = facturesDemandees.stream()
                .map(f -> new BigDecimal(f.get("montant").toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (wallet.getBalance().compareTo(total) < 0) {
            throw new InsufficientBalanceException(
                    "Solde insuffisant. Solde : " + wallet.getBalance() + " | Total : " + total);
        }

        wallet.setBalance(wallet.getBalance().subtract(total));
        walletRepository.save(wallet);

        paymentServiceProxy.payerFactures(factureReferences);

        eventPublisher.publishEvent(new TransactionEvent(
                this, wallet, TransactionType.PAYMENT,
                total, BigDecimal.ZERO,
                "Paiement factures spécifiques " + serviceName + " : " + factureReferences + " | Total : " + total
        ));
    }
    
    @Override
    public List<Transaction> listerTransactions(String phoneNumber) {
        return transactionRepository.findByWallet_PhoneNumberOrderByCreatedAtDesc(phoneNumber);
    }
}
