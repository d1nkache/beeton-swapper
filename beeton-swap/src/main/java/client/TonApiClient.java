package client;

import org.ton.ton4j.address.Address;

public interface TonApiClient {
    String getJettonWalletAddress(String jettonMinterAddress, Address walletAddress);
    String getVaultAddress(String dedustContractAddress, String jettonMinterAddress);
    String getPoolAddress(String dedustContractAddress, String from, String to);
}