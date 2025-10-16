import wrappers.WalletImpl;
import org.ton.ton4j.smartcontract.SendMode;
import org.ton.ton4j.address.Address;
import org.ton.ton4j.smartcontract.types.WalletConfig;
import org.ton.ton4j.smartcontract.types.WalletV4R2Config;

import java.math.BigInteger;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Wallet Test...");

        List<String> mnemonic = List.of(

        );

        WalletImpl wallet = new WalletImpl(mnemonic, true);
        long seqno = wallet.asWalletContract().getSeqno();

        WalletConfig config = wallet.buildConfig(
                Address.of("UQCdnvsDd_mmIjnaWtDIZLimrScNF8z56ydBLUcnjHqOh6PP"), // адрес получателя
                BigInteger.valueOf(1_000_000_0L),                               // 1 TON = 1e9 нанотонов
                seqno,                                                          // seqno
                698983191L,                                                     // walletId
                "Test message",
                true,                                                           // bounce
                SendMode.PAY_GAS_SEPARATELY                                     // режим отправки
        );

        var response = wallet.sendMessage(config);
        System.out.println("Response: " + response.second);
    }
}
