package service;

import org.ton.ton4j.cell.Cell;
import org.ton.ton4j.cell.CellBuilder;
import org.ton.ton4j.smartcontract.SendMode;
import org.ton.ton4j.smartcontract.types.WalletV4R2Config;

import client.TonApiClient;

import java.math.BigInteger;

import org.ton.ton4j.address.Address;
import wrappers.Wallet;

public class SwapServiceImpl{
    private static final String  DEDUST_CONTRACT_ADDRESS = "EQBfBWT7X2BHg9tXAxzhz2aKiNTU1tpt5NsiK0uSDW_YAJ67";
    private static final Integer VAULT_SWAP_OP_CODE      = 0xe3a0d482 ; 

    private Wallet wallet;
    private TonApiClient tonApiClient;

    public SwapServiceImpl(Wallet wallet, TonApiClient tonApiClient) {
        this.wallet = wallet;
        this.tonApiClient = tonApiClient;
    }

    public void desustSwap(
        String jettonA, 
        String jettonB,
        BigInteger jettonAmount
    ) {
        String vaultAddress = this.tonApiClient.getVaultAddress(DEDUST_CONTRACT_ADDRESS, jettonA);
        System.out.println("Vault address -> " + vaultAddress);
        String poolAddress  = this.tonApiClient.getPoolAddress(DEDUST_CONTRACT_ADDRESS, jettonA, jettonB);
        System.out.println("Pool address -> " + poolAddress);

        // String jettonMinterAddress  = this.tonApiClient.getMinterAddress(jettonAddress)
        String jettonAWalletAddress  = this.tonApiClient.getJettonWalletAddress(jettonA, this.wallet.getWalletAddress());
        System.out.println("jw A address -> " + jettonAWalletAddress);
        String jettonBWalletAddress  = this.tonApiClient.getJettonWalletAddress(jettonB, this.wallet.getWalletAddress());
        System.out.println("jw B address -> " + jettonBWalletAddress);

        this.buildSwapBody(
            jettonAmount, 
            VAULT_SWAP_OP_CODE,
            0,
            Address.of(poolAddress), 
            BigInteger.ZERO, 
            null, 
            0, 
            Address.of(jettonBWalletAddress), 
            null, 
            null, 
            null
        );

        WalletV4R2Config walletSendConfig = (WalletV4R2Config) this.wallet.buildConfig(
            Address.of(vaultAddress),
            jettonAmount,
            0L,
            0L,
            jettonAWalletAddress,
            false,
            SendMode.PAY_GAS_SEPARATELY
        );

        this.wallet.sendMessage(walletSendConfig);
        return; 
    }


    private CellBuilder buildSwapStep(
        CellBuilder curretCell,
        Address pool_address,
        BigInteger limit,
        Cell multiStep
    ) {
        return (
            curretCell.storeAddress(pool_address)
                .storeUint(0, 1)
                .storeCoins(limit)
                .storeRefMaybe(multiStep)
        );
    }


    private Cell buldSwapParams(
        int deadline,
        Address recipientAddress,
        Address referalAddress,
        Cell successCustomPayload,
        Cell failureCustomPayload
    ) {
        return (
            CellBuilder.beginCell()
                .storeUint(deadline, 32)
                .storeAddress(recipientAddress)
                .storeAddress(referalAddress)
                .storeRefMaybe(successCustomPayload)
                .storeRefMaybe(failureCustomPayload)
            .endCell()
        );
    }


    private Cell buildSwapBody(
        BigInteger jettonAmount,
        int opCode,
        int queryId,
        Address pool_address,
        BigInteger limit,
        Cell multiStep,
        int deadline,
        Address recipientAddress,
        Address referalAddress,
        Cell successCustomPayload,
        Cell failureCustomPayload
    ) {

        Cell swapParams = this.buldSwapParams(
            deadline,
            recipientAddress,
            referalAddress,
            successCustomPayload,
            failureCustomPayload
        );

        CellBuilder builder = CellBuilder.beginCell()
                .storeUint(opCode & 0xFFFFFFFFL, 32)
                .storeUint(queryId, 64)
                .storeCoins(jettonAmount);

        Cell body = this.buildSwapStep(builder, pool_address, limit, multiStep).storeRef(swapParams).endCell();

        return body;
    }
}