package ism.l3.payment.services;

import ism.l3.payment.entity.Facture;
import ism.l3.payment.entity.Unite;

import java.util.List;

public interface FactureService {

    void initialiserWallet(String walletCode, String phoneNumber);

    void initialiserWalletAvecHistorique(String walletCode, String phoneNumber, int nombreMois);

    List<Facture> listerFacturesMoisCourant(String walletCode);

    List<Facture> listerFacturesMoisCourantParService(String walletCode, Unite unite);

    List<Facture> listerFacturesPeriode(String walletCode, String debut, String fin);

    void payerFactures(List<String> references);
}
