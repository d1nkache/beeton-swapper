import wrappers.WalletImpl;
import org.ton.ton4j.smartcontract.SendMode;
import org.ton.ton4j.address.Address;
import org.ton.ton4j.smartcontract.types.WalletConfig;

import client.TonApiClientImpl;
import service.SwapServiceImpl;

import java.math.BigInteger;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Wallet Test...");

        List<String> mnemonic = List.of(
        );




        WalletImpl wallet = new WalletImpl(mnemonic, false);
        System.out.println(wallet.getWalletAddress().toString());
        // long seqno = wallet.asWalletContract().getSeqno();

        // System.out.println(wallet.getWalletAddress());
        // System.out.println(seqno);

        // WalletConfig config = wallet.buildConfig(
        //         Address.of("0QB6zEKUZAKBTboZvhXo0tALkzkHieuGsfMfisrBFIX5muxX"),      // адрес получателя
        //         BigInteger.valueOf(1_000_000_0L),                                    // 1 TON = 1e9 нанотонов
        //         seqno,                                                               // seqno
        //         698983191L,                                                          // walletId
        //         "Test message",                                                      // 
        //         true,                                                                // bounce
        //         SendMode.PAY_GAS_SEPARATELY                                          // режим отправки
        // );

        // var response = wallet.sendMessage(config);
        // System.out.println("Response: " + response.second);

        SwapServiceImpl swapServiceImpl = new SwapServiceImpl(wallet, new TonApiClientImpl());
        // swapServiceImpl.desustSwapSell(
        //     "EQCi9nWtRY5rdEWkZIPOe_9n1WXog8ObXCIf6RGmwFCnrrT8",
        //     "EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs",
        //     "native",
        //     BigInteger.valueOf(100_000_000L)
        // );

        swapServiceImpl.desustSwapBuy(
            "EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs",
            "EQCi9nWtRY5rdEWkZIPOe_9n1WXog8ObXCIf6RGmwFCnrrT8",
            "multi",
            BigInteger.valueOf(100_000L)
        );
    }
}