package ism.l3.badwallet.services.deposit;

import ism.l3.badwallet.entity.Wallet;
import ism.l3.badwallet.events.TransactionEvent;
import ism.l3.badwallet.entity.TransactionType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class WalletTargetDepositStrategy implements DepositStrategy {

    private final ApplicationEventPublisher eventPublisher;

    public WalletTargetDepositStrategy(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void effectuerDepot(Wallet wallet, BigDecimal amount) {
        wallet.setBalance(wallet.getBalance().add(amount));
        eventPublisher.publishEvent(new TransactionEvent(
                this, wallet, TransactionType.DEPOSIT,
                amount, BigDecimal.ZERO,
                "Dépôt reçu depuis un autre portefeuille : " + amount + " " + wallet.getCurrency()
        ));
    }
}
