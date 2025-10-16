package wrappers;

import java.nio.file.*;
import model.Tuple;
import org.ton.ton4j.tonlib.Tonlib;

import org.ton.ton4j.smartcontract.SendMode;
import org.ton.ton4j.smartcontract.SendResponse;
import org.ton.ton4j.smartcontract.wallet.Contract;
import org.ton.ton4j.smartcontract.wallet.v4.WalletV4R2;
import org.ton.ton4j.smartcontract.types.WalletConfig;
import org.ton.ton4j.smartcontract.types.WalletV4R2Config;

import org.ton.ton4j.cell.Cell;
import org.ton.ton4j.utils.Utils;

import com.iwebpp.crypto.TweetNaclFast;

import org.ton.ton4j.mnemonic.Pair;
import org.ton.ton4j.address.Address;
import org.ton.ton4j.mnemonic.Mnemonic;

import java.nio.file.*;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class WalletImpl implements Wallet {
    private Tonlib tonLib;
    private byte[] publicKey;
    private byte[] secretKey;
    private boolean isTestnet;

    public WalletImpl(List<String> mnemonic, boolean isTestnet) {
        this.tonLib = Tonlib.builder()
            .pathToTonlibSharedLib("tonlibjson.dll")
            .testnet(isTestnet)
            .build();

        Pair keyPair = mnemonicToKeyPair(mnemonic);
        this.publicKey = keyPair.getPublicKey();
        this.secretKey = keyPair.getSecretKey();
    }

    @Override
    public WalletV4R2 asWalletContract() {
        TweetNaclFast.Signature.KeyPair keyPair = TweetNaclFast.Signature.keyPair_fromSeed(this.secretKey);

        return WalletV4R2.builder()
            .keyPair(keyPair)
            .tonlib(this.tonLib)
            .wc(0)
            .walletId(698983191)
            .build();
    }

    @Override
    public Tuple<Long, String> sendMessage(WalletConfig config) {
        WalletV4R2 wallet = this.asWalletContract();
        SendResponse response = wallet.send((WalletV4R2Config) config);
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

    // Дополнительная удобная версия
    public static WalletConfig buildConfig(
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
