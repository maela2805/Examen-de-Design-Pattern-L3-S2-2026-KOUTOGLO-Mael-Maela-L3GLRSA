package ism.l3.badwallet.services.proxy;

import ism.l3.badwallet.entity.Wallet;

import java.util.List;
import java.util.Map;


public interface PaymentServiceProxy {
    void initialiserWallet(String walletCode, String phoneNumber);

    void initialiserWalletAvecHistorique(String walletCode, String phoneNumber, int nombreMois);

    List<Map<String, Object>> getFacturesMoisCourant(String walletCode, String unite);

    List<Map<String, Object>> getFacturesPeriode(String walletCode, String debut, String fin);
    void payerFactures(List<String> references);
}
