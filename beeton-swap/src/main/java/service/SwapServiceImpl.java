package service;

import org.ton.ton4j.cell.Cell;
import org.ton.ton4j.cell.CellBuilder;

import java.math.BigInteger;

import org.ton.ton4j.address.Address;
import wrappers.Wallet;

public class SwapServiceImpl{
    private Wallet wallet;

    public SwapServiceImpl(Wallet wallet) {
        this.wallet = wallet;
    }

    // public void desustSwap(
    //     String codeJettonA,
    //     String codeJettonB,
    // ) {
    //     return;
    //     // poolAddress 
    //     // vaultAddress
    // }


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
                .storeUint(opCode, 32)
                .storeUint(queryId, 64)
                .storeCoins(jettonAmount);

        Cell body = this.buildSwapStep(builder, pool_address, limit, multiStep).storeRef(swapParams).endCell();

        return body;
    }

}
