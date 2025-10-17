package wrappers;

import java.math.BigInteger;

import org.ton.ton4j.address.Address;
import org.ton.ton4j.smartcontract.SendMode;
import org.ton.ton4j.smartcontract.SendResponse;
import org.ton.ton4j.smartcontract.types.WalletConfig;
import org.ton.ton4j.smartcontract.types.WalletV4R2Config;
import org.ton.ton4j.smartcontract.wallet.Contract;

import model.Tuple;


public interface Wallet {
    public Contract asWalletContract();
    public Tuple<Long, String> sendMessage(WalletConfig config);

    public WalletConfig buildConfig(
        Address destination,
        BigInteger amount,
        long seqno,
        long walletId,
        String comment,
        boolean bounce,
        SendMode sendMode
    );
}