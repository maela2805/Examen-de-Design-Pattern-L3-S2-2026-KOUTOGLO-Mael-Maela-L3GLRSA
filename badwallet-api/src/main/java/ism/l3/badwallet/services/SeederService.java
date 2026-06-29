package ism.l3.badwallet.services;

import ism.l3.badwallet.entity.TransactionType;
import ism.l3.badwallet.entity.Wallet;
import ism.l3.badwallet.events.TransactionEvent;
import ism.l3.badwallet.repository.WalletRepository;
import ism.l3.badwallet.services.proxy.PaymentServiceProxy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class SeederService {

    private static final String[] CURRENCIES = {"XOF"};
    private final Random random = new Random();

    private final WalletRepository walletRepository;
    private final PaymentServiceProxy paymentServiceProxy;
    private final ApplicationEventPublisher eventPublisher;

    public SeederService(WalletRepository walletRepository,
                          PaymentServiceProxy paymentServiceProxy,
                          ApplicationEventPublisher eventPublisher) {
        this.walletRepository = walletRepository;
        this.paymentServiceProxy = paymentServiceProxy;
        this.eventPublisher = eventPublisher;
    }
    @Async
    @Transactional
    public void seeder(int numWallets, int eventsPerWallet) {
        System.out.println("🌱 Démarrage du seeding : " + numWallets + " wallets...");
        int nombreMois = Math.max(1, eventsPerWallet / 2);

        for (int i = 1; i <= numWallets; i++) {
            String numero = String.format("%07d", i);
            String walletCode = "WLT-" + numero;
            String phoneNumber = "+22177000" + String.format("%04d", i);
            String email = "user" + i + "@badwallet.sn";
            if (walletRepository.existsByCode(walletCode)) {
                continue;
            }
            BigDecimal soldeInitial = BigDecimal.valueOf(10000 + random.nextInt(490001));

            Wallet wallet = new Wallet(phoneNumber, email, walletCode, "XOF", soldeInitial);
            walletRepository.save(wallet);
            for (int j = 0; j < eventsPerWallet; j++) {
                BigDecimal montant = BigDecimal.valueOf(1000 + random.nextInt(49001));
                boolean estDepot = random.nextBoolean();

                if (estDepot) {
                    wallet.setBalance(wallet.getBalance().add(montant));
                    eventPublisher.publishEvent(new TransactionEvent(
                            this, wallet, TransactionType.DEPOSIT,
                            montant, BigDecimal.ZERO,
                            "Dépôt seeder #" + (j + 1)
                    ));
                } else {
                    BigDecimal frais = montant.multiply(new BigDecimal("0.01"))
                            .min(new BigDecimal("5000"));
                    BigDecimal total = montant.add(frais);
                    if (wallet.getBalance().compareTo(total) >= 0) {
                        wallet.setBalance(wallet.getBalance().subtract(total));
                        eventPublisher.publishEvent(new TransactionEvent(
                                this, wallet, TransactionType.WITHDRAWAL,
                                montant, frais,
                                "Retrait seeder #" + (j + 1)
                        ));
                    }
                }
                walletRepository.save(wallet);
            }
            try {
                paymentServiceProxy.initialiserWalletAvecHistorique(
                        walletCode, phoneNumber, nombreMois);
            } catch (Exception e) {
                System.err.println("Avertissement payment-service pour " + walletCode + " : " + e.getMessage());
            }

            System.out.println("Wallet seedé : " + walletCode + " (" + i + "/" + numWallets + ")");
        }

        System.out.println("Seeding terminé !");
    }
}
