package wrappers;
import java.nio.file.*;
import model.Tuple;
import org.ton.ton4j.tonlib.Tonlib;

import org.ton.ton4j.smartcontract.SendMode;
import org.ton.ton4j.smartcontract.SendResponse;
import org.ton.ton4j.smartcontract.wallet.v4.WalletV4R2;
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


public class WalletImpl {
    private Tonlib tonLib;
    private byte[] publicKey;
    private byte[] secretKey;
    private boolean isTestnet;

    public WalletImpl(List<String> mnemonic, boolean isTestnet) {
        System.out.println("constructor");
        this.tonLib = Tonlib.builder()
            .pathToTonlibSharedLib("tonlibjson.dll")
            .testnet(isTestnet)
            .build();
        
        System.out.println("constructor_before_key_pair");
        Pair keyPair    = this.mnemonicToKeyPair(mnemonic);
        System.out.println("constructor_after_key_pair");
        this.publicKey  = keyPair.getPublicKey();
        this.secretKey  = keyPair.getSecretKey();
        System.out.println("constructor_end");
    }

    public WalletV4R2 asWalletV4R2() {
        TweetNaclFast.Signature.KeyPair keyPair = TweetNaclFast.Signature.keyPair_fromSeed(this.secretKey);

        WalletV4R2 wallet = WalletV4R2.builder()
            .keyPair(keyPair)
            .tonlib(this.tonLib)
            .wc(0)
            .walletId(698983191)
            .build();
        
        return wallet;
    }


    public Tuple<Long, String> sendMessage(WalletV4R2Config config) {
        WalletV4R2 wallet = this.asWalletV4R2();
        SendResponse response = wallet.send(config);
        
        return new Tuple<>((long) response.getCode(), response.getMessage());
    }


    public static WalletV4R2Config buildConfig(
        Address destination,
        BigInteger amount,
        long seqno,
        long walletId,
        String comment,
        boolean bounce,
        SendMode sendMode
    ) {
        System.out.println("config");
        return WalletV4R2Config.builder()
            .destination(destination)
            .amount(amount)
            .walletId(walletId)
            .seqno(seqno)
            .bounce(bounce)
            .sendMode(sendMode)
            .build();
    }


    public static WalletV4R2Config buildConfig(
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
            Pair keyPair = Mnemonic.toKeyPair(mnemonic);
            return keyPair;
        }
        catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            System.out.println("Error" + ex.getMessage().toString());
            throw new RuntimeException("Failed to derive key pair from mnemonic", ex);
        }
    }
}