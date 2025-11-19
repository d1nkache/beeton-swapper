package client;

import org.ton.ton4j.address.Address;

import model.Tuple;

public interface TonApiClient {
    String getJettonWalletAddress(String jettonMinterAddress, Address walletAddress);
    String getVaultAddress(String dedustContractAddress, String jettonMinterAddress);
    String getPoolAddress(String dedustContractAddress, String from, String to);
    Tuple<String, String> getAccountTrades(String walletAddress);
}