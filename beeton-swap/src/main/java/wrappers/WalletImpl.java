package wrappers;

import model.Tuple;

import org.ton.ton4j.address.Address;
import org.ton.ton4j.adnl.AdnlLiteClient;
import org.ton.ton4j.adnl.globalconfig.TonGlobalConfig;
import org.ton.ton4j.cell.Cell;
import org.ton.ton4j.mnemonic.Mnemonic;
import org.ton.ton4j.mnemonic.Pair;
import org.ton.ton4j.smartcontract.SendMode;
import org.ton.ton4j.smartcontract.SendResponse;
import org.ton.ton4j.smartcontract.types.WalletConfig;
import org.ton.ton4j.smartcontract.types.WalletV4R2Config;
import org.ton.ton4j.smartcontract.wallet.v4.WalletV4R2;
import org.ton.ton4j.tonlib.types.globalconfig.TonlibConfig;
import org.ton.ton4j.utils.Utils;

import com.iwebpp.crypto.TweetNaclFast;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class WalletImpl implements Wallet {
    private AdnlLiteClient adnl;

    private byte[] publicKey;
    private byte[] secretKey;
    private boolean isTestnet;

    public Address walletAddress;

    public WalletImpl(List<String> mnemonic, boolean isTestnet) {
        TonGlobalConfig tonGlobalConfig = TonGlobalConfig.loadFromUrl("");

        try {
            this.adnl = AdnlLiteClient.builder().globalConfig(tonGlobalConfig).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build AdnlLiteClient", e);
        }

        Pair keyPair = mnemonicToKeyPair(mnemonic);
        this.isTestnet = isTestnet;
        this.publicKey = keyPair.getPublicKey();
        this.secretKey = keyPair.getSecretKey();
    }

    @Override
    public Address getWalletAddress() {
        if (this.walletAddress == null) {
            this.walletAddress = this.asWalletContract().getAddress();
            return walletAddress;
        }
        return this.walletAddress;
    }

    @Override
    public WalletV4R2 asWalletContract() {
        TweetNaclFast.Signature.KeyPair keyPair = TweetNaclFast.Signature.keyPair_fromSeed(this.secretKey);

        return WalletV4R2.builder()
                .keyPair(keyPair)
                .adnlLiteClient(this.adnl)
                .wc(0)
                .walletId(698983191L)
                .build();
    }

    @Override
    public Tuple<Long, String> sendMessage(WalletConfig config) {
        System.out.println("=== sendMessage START ===");
        System.out.println("Wallet config: " + config);

        WalletV4R2 wallet = this.asWalletContract();
        System.out.println("Wallet instance created: " + wallet);

        SendResponse response = wallet.send((WalletV4R2Config) config);
        System.out.println("Send response code: " + response.getCode());
        System.out.println("Send response message: " + response.getMessage());
        System.out.println("=== sendMessage END ===");

        return new Tuple<>((long) response.getCode(), response.getMessage());
    }

    @Override
    public WalletConfig buildConfig(
            Address destination,
            BigInteger amount,
            long seqno,
            long walletId,
            String comment,
            boolean bounce,
            SendMode sendMode
    ) {
        return WalletV4R2Config.builder()
                .destination(destination)
                .amount(amount)
                .walletId(walletId)
                .seqno(seqno)
                .bounce(bounce)
                .sendMode(sendMode)
                .build();
    }

    @Override
    public WalletConfig buildConfig(
            Address destination,
            BigInteger amount,
            long seqno,
            long walletId,
            String comment,
            boolean bounce,
            SendMode sendMode,
            Cell body
    ) {
        return WalletV4R2Config.builder()
                .destination(destination)
                .amount(amount)
                .walletId(walletId)
                .seqno(seqno)
                .bounce(bounce)
                .sendMode(sendMode)
                .body(body)
                .build();
    }

    private static Pair mnemonicToKeyPair(List<String> mnemonic) {
        try {
            return Mnemonic.toKeyPair(mnemonic);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new RuntimeException("Failed to derive key pair from mnemonic", ex);
        }
    }
}
