package service;

import java.math.BigInteger;

import org.ton.ton4j.cell.Cell;
import org.ton.ton4j.cell.CellBuilder;
import org.ton.ton4j.smartcontract.SendMode;
import org.ton.ton4j.smartcontract.types.WalletV4R2Config;
import org.ton.ton4j.address.Address;

import client.TonApiClient;
import wrappers.Wallet;
import model.SwapResponse;
import model.Tuple;


public class SwapServiceImpl {

    private static final String DEDUST_CONTRACT_ADDRESS = "EQBfBWT7X2BHg9tXAxzhz2aKiNTU1tpt5NsiK0uSDW_YAJ67";
    private static final int VAULT_JETTON_SWAP_OP = 0xe3a0d482;
    private static final int VAULT_NATIVE_SWAP_OP = 0xea06185d;
    private static final int POOL_TYPE_VOLATILE = 0;

    private final Wallet wallet;
    private final TonApiClient tonApiClient;

    public SwapServiceImpl(Wallet wallet, TonApiClient tonApiClient) {
        this.wallet = wallet;
        this.tonApiClient = tonApiClient;
    }

    public SwapResponse desustSwapSell(String jettonA, String jettonB, String swapType, BigInteger jettonAmount) {
        SwapResponse resp = new SwapResponse();
        try {
            String assetJettonA = fromJettonToAssetHex(jettonA, false);
            String assetJettonB = fromJettonToAssetHex(jettonB, false);
            String assetNative = fromJettonToAssetHex(null, true);

            String vaultAddress = null;
            String poolAddressFirstStep = null;
            Cell multiStep = null;

            if (swapType.equals("native")) {
                vaultAddress = this.tonApiClient.getVaultAddress(DEDUST_CONTRACT_ADDRESS, assetJettonA);
                poolAddressFirstStep = this.tonApiClient.getPoolAddress(DEDUST_CONTRACT_ADDRESS, assetJettonA, assetNative);
            } else if (swapType.equals("multi")) {
                vaultAddress = this.tonApiClient.getVaultAddress(DEDUST_CONTRACT_ADDRESS, assetJettonA);
                poolAddressFirstStep = this.tonApiClient.getPoolAddress(DEDUST_CONTRACT_ADDRESS, assetJettonA, assetNative);
                String poolAddressSecondStep = this.tonApiClient.getPoolAddress(DEDUST_CONTRACT_ADDRESS, assetNative, assetJettonB);

                multiStep = CellBuilder.beginCell()
                        .storeAddress(Address.of(poolAddressSecondStep))
                        .storeUint(0, 1)
                        .storeCoins(BigInteger.ZERO)
                        .storeRefMaybe(null)
                        .endCell();
            } else {
                resp.setStatus("error");
                resp.setMessage("Invalid route type (must be 'native' or 'multi')");
                return resp;
            }

            String jettonAWalletAddress = this.tonApiClient.getJettonWalletAddress(jettonA, this.wallet.getWalletAddress());
            
            System.out.println("Wallet address:" + jettonAWalletAddress);
            System.out.println("jettonA address:" + jettonA);

            Cell jettonSwapBody = this.buildJettonSwapBody(
                    jettonAmount,
                    VAULT_JETTON_SWAP_OP,
                    0,
                    Address.of(poolAddressFirstStep),
                    BigInteger.ZERO,
                    multiStep,
                    0,
                    Address.of(this.wallet.getWalletAddress()),
                    null,
                    null,
                    null
            );

            Cell jettonTransferBody = this.buildJettonTransferBody(
                    Address.of(vaultAddress),
                    jettonAmount,
                    0,
                    Address.of(this.wallet.getWalletAddress()),
                    null,
                    BigInteger.valueOf(250_000_000L),
                    jettonSwapBody
            );

            WalletV4R2Config walletSendConfig = (WalletV4R2Config) this.wallet.buildConfig(
                    Address.of(jettonAWalletAddress),
                    BigInteger.valueOf(500_000_000L),
                    wallet.asWalletContract().getSeqno(),
                    698983191L,
                    "comment",
                    false,
                    SendMode.PAY_GAS_SEPARATELY,
                    jettonTransferBody);

            this.wallet.sendMessage(walletSendConfig);

            Thread.sleep(10000);
            Tuple<String, String> tradeInfo = this.tonApiClient.getAccountTrades(wallet.getWalletAddress().toString());

            fillSuccessResponse(resp, "sell", swapType, jettonA, jettonB, jettonAmount, tradeInfo, "Transaction sent successfully");
            return resp;

        } catch (Exception e) {
            resp.setStatus("error");
            resp.setMessage("Swap sell failed: " + e.getMessage());
            return resp;
        }
    }


    public SwapResponse desustSwapBuy(String jettonA, String jettonB, String swapType, BigInteger jettonAmount) {
        SwapResponse resp = new SwapResponse();
        try {
            String assetJettonA = fromJettonToAssetHex(jettonA, false);
            String assetJettonB = fromJettonToAssetHex(jettonB, false);
            String assetNative = fromJettonToAssetHex(null, true);

            String vaultAddress = null;
            String poolAddressFirstStep = null;
            Cell multiStep = null;

            if (swapType.equals("native")) {
                vaultAddress = this.tonApiClient.getVaultAddress(DEDUST_CONTRACT_ADDRESS, assetNative);
                poolAddressFirstStep = this.tonApiClient.getPoolAddress(DEDUST_CONTRACT_ADDRESS, assetNative, assetJettonA);
            } else if (swapType.equals("multi")) {
                this.buyViaJetton(jettonA, assetJettonA, assetJettonB, assetNative, jettonAmount);
                
                Thread.sleep(10000);
                Tuple<String, String> tradeInfo = this.tonApiClient.getAccountTrades(wallet.getWalletAddress().toString());

                fillSuccessResponse(resp, "buy", "multi", jettonA, jettonB, jettonAmount, tradeInfo, "Transaction (multi) sent successfully");
                return resp;
            } else {
                resp.setStatus("error");
                resp.setMessage("Invalid route type (must be 'native' or 'multi')");
                return resp;
            }

            Cell nativeSwapBody = this.buildNativeSwapBody(
                    VAULT_NATIVE_SWAP_OP,
                    0L,
                    jettonAmount,
                    Address.of(poolAddressFirstStep),
                    BigInteger.ZERO,
                    multiStep,
                    0,
                    Address.of(this.wallet.getWalletAddress()),
                    null,
                    null
                );

                BigInteger sendAmount = jettonAmount.add(BigInteger.valueOf(100_000_000L));

                WalletV4R2Config walletSendConfig = (WalletV4R2Config) this.wallet.buildConfig(
                    Address.of(vaultAddress),
                    sendAmount,
                    wallet.asWalletContract().getSeqno(),
                    698983191L,
                    "comment",
                    true,
                    SendMode.PAY_GAS_SEPARATELY,
                    nativeSwapBody);

            this.wallet.sendMessage(walletSendConfig);

            Thread.sleep(10000);
            Tuple<String, String> tradeInfo = this.tonApiClient.getAccountTrades(wallet.getWalletAddress().toString());

            fillSuccessResponse(resp, "buy", swapType, jettonA, jettonB, jettonAmount, tradeInfo, "Transaction sent successfully");
            return resp;
        } catch (Exception e) {
            resp.setStatus("error");
            resp.setMessage("Swap buy failed: " + e.getMessage());
            return resp;
        }
    }

    public String fromJettonToAssetHex(String jettonA, boolean isNative) {
        Cell cell;

        if (isNative) {
            cell = CellBuilder.beginCell()
                    .storeUint(0, 4)
                    .endCell();
        } else {
            cell = CellBuilder.beginCell()
                    .storeUint(1, 4)
                    .storeUint(0, 8)
                    .storeBytes(Address.of(jettonA).hashPart)
                    .endCell();
        }

        byte[] rawBytes = cell.toBoc();

        StringBuilder sb = new StringBuilder();

        for (byte b : rawBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void buyViaJetton(
            String jettonA,
            String assetJettonA,
            String assetJettonB,
            String assetNative,
            BigInteger jettonAmount) {

        String vaultAddress = this.tonApiClient.getVaultAddress(DEDUST_CONTRACT_ADDRESS, assetJettonA);
        String poolAddressFirstStep = this.tonApiClient.getPoolAddress(DEDUST_CONTRACT_ADDRESS, assetJettonA, assetNative);
        String poolAddressSecondStep = this.tonApiClient.getPoolAddress(DEDUST_CONTRACT_ADDRESS, assetNative, assetJettonB);

        Cell multiStep = CellBuilder.beginCell()
                .storeAddress(Address.of(poolAddressSecondStep))
                .storeUint(0, 1)
                .storeCoins(BigInteger.ZERO)
                .storeRefMaybe(null)
                .endCell();

        Cell jettonSwapBody = this.buildJettonSwapBody(
                jettonAmount,
                VAULT_JETTON_SWAP_OP,
                0,
                Address.of(poolAddressFirstStep),
                BigInteger.ZERO,
                multiStep,
                0,
                Address.of(this.wallet.getWalletAddress()),
                null,
                null,
                null);

        String jettonAWalletAddress = this.tonApiClient.getJettonWalletAddress(jettonA, this.wallet.getWalletAddress());

        Cell jettonTransferBody = buildJettonTransferBody(
                Address.of(vaultAddress),
                jettonAmount,
                0,
                Address.of(this.wallet.getWalletAddress()),
                null,
                BigInteger.valueOf(250_000_000L),
                jettonSwapBody);

        WalletV4R2Config walletSendConfig = (WalletV4R2Config) this.wallet.buildConfig(
                Address.of(jettonAWalletAddress),
                BigInteger.valueOf(270_000_000L),
                wallet.asWalletContract().getSeqno(),
                698983191L,
                "comment",
                true,
                SendMode.PAY_GAS_SEPARATELY,
                jettonTransferBody);

        this.wallet.sendMessage(walletSendConfig);
    }

    private void fillSuccessResponse(
            SwapResponse resp,
            String direction,
            String route,
            String jettonA,
            String jettonB,
            BigInteger jettonAmount,
            Tuple<String, String> tradeInfo,
            String message
    ) {
        resp.setStatus("success");
        resp.setDirection(direction);
        resp.setRoute(route);
        resp.setJettonA(jettonA);
        resp.setJettonB(jettonB);
        resp.setJettonAmount(jettonAmount != null ? jettonAmount.toString() : null);

        if (tradeInfo != null) {
            resp.setAmountIn(tradeInfo.first);
            resp.setAmountOut(tradeInfo.second);
        }

        resp.setMessage(message);
    }

    private Cell buildJettonTransferBody(
        Address destination, 
        BigInteger amount, 
        long queryId,
        Address responseAddress, 
        Cell customPayload, 
        BigInteger forwardAmount, 
        Cell forwardPayload
    ) {
        return (CellBuilder.beginCell()
                .storeUint(BigInteger.valueOf(0x0f8a7ea5L), 32)
                .storeUint(BigInteger.valueOf(queryId), 64)
                .storeCoins(amount)
                .storeAddress(destination)
                .storeAddress(responseAddress)
                .storeRefMaybe(customPayload)
                .storeCoins(forwardAmount)
                .storeRefMaybe(forwardPayload)
                .endCell());
    }

    private Cell buildNativeSwapBody(
        int opCode, 
        long queryId, 
        BigInteger amountTon, 
        Address poolAddress,
        BigInteger limit, 
        Cell multiStep, 
        int deadline, 
        Address recipientAddress, 
        Cell successCustomPayload,
        Cell failureCustomPayload
    ) {
        Cell swapParams = CellBuilder.beginCell()
                .storeUint(deadline, 32)
                .storeAddress(recipientAddress)
                .storeAddress(null)
                .storeRefMaybe(successCustomPayload)
                .storeRefMaybe(failureCustomPayload)
                .endCell();

        return CellBuilder.beginCell()
                .storeUint(opCode & 0xFFFFFFFFL, 32)
                .storeUint(queryId, 64)
                .storeCoins(amountTon)
                .storeAddress(poolAddress)
                .storeUint(0, 1)
                .storeCoins(limit)
                .storeRefMaybe(multiStep)
                .storeRef(swapParams)
                .endCell();
    }

    private Cell buildJettonSwapBody(
        BigInteger jettonAmount, 
        int opCode, 
        int queryId, 
        Address poolAddress,
        BigInteger limit, 
        Cell multiStep, 
        int deadline, 
        Address recipientAddress, 
        Address referalAddress,        
        Cell successCustomPayload, 
        Cell failureCustomPayload
    ) {
        Cell swapParams = CellBuilder.beginCell()
                .storeUint(deadline, 32)
                .storeAddress(recipientAddress)
                .storeAddress(null)
                .storeRefMaybe(successCustomPayload)
                .storeRefMaybe(failureCustomPayload)
                .endCell();

        return CellBuilder.beginCell()
                .storeUint(VAULT_JETTON_SWAP_OP & 0xFFFFFFFFL, 32)
                .storeAddress(poolAddress)
                .storeUint(0, 1)
                .storeCoins(limit)
                .storeRefMaybe(multiStep)
                .storeRef(swapParams)
                .endCell();
    }
}