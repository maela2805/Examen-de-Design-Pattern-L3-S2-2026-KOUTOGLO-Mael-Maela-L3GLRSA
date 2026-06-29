package ism.l3.badwallet.services.deposit;

import ism.l3.badwallet.entity.Wallet;

import java.math.BigDecimal;

public interface DepositStrategy {
    void effectuerDepot(Wallet wallet, BigDecimal amount);
}
