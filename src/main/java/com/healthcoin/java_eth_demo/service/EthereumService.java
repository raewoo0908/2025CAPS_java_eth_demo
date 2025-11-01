package com.healthcoin.java_eth_demo.service;

import com.healthcoin.java_eth_demo.contracts.SimpleWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import com.healthcoin.java_eth_demo.contracts.RaewooCoin; // 1단계에서 생성한 Wrapper 클래스
import org.springframework.beans.factory.annotation.Value;
import org.web3j.crypto.Credentials;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

// Notate Spring that this class is a component of Service layer.
@Service
public class EthereumService {

    // Inject Web3j Bean to we3j object.
    @Autowired
    private Web3j web3j;

    @Autowired
    private Credentials credentials;

    // Read 'token.contract-address' value from application.properties and inject this var 'contractAddress'.
    @Value("${token.contract-address}")
    private String contractAddress;

    @Value("${simplewallet.contract-address}")
    private String simpleWalletContractAddress;

    /**
     * Gets the latest block number of holesky network.
     */
    public BigInteger getLatestBlockNumber() throws IOException {
        EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();
        return ethBlockNumber.getBlockNumber();
    }

    /**
     * get ETH balance of specific wallet address.
     * @param address wallet address to get balance from.
     */
    public BigDecimal getEthBalance(String address) throws IOException {
        EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();

        // translate the WEI to ETH
        BigInteger weiBalance = ethGetBalance.getBalance();
        return Convert.fromWei(new BigDecimal(weiBalance), Convert.Unit.ETHER);
    }

    /**
     * get ERC-20 token balance of specific wallet.
     * @param ownerAddress wallet address to get balance from.
     * @return balance of token. (unit: Ether)
     */
    public BigDecimal getTokenBalance(String ownerAddress) throws Exception {
        // Load the contract: load the contract object using Wrapper class(RaewooCoin).
        RaewooCoin tokenContract = RaewooCoin.load(contractAddress, web3j, credentials, new DefaultGasProvider());

        // Call balanceOf function from contract.
        BigInteger balanceInWei = tokenContract.balanceOf(ownerAddress).send();

        // Return the balance in Ether.
        return Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER);
    }

    /**
     * Get full Name of ERC-20 Token.
     * @return Name of Token.
     */
    public String getTokenName() throws Exception {
        // Load the contract: load the contract object using Wrapper class(RaewooCoin).
        RaewooCoin tokenContract = RaewooCoin.load(contractAddress, web3j, credentials, new DefaultGasProvider());

        return tokenContract.name().send();
    }

    /**
     * Get full Symbol of ERC-20 Token.
     * @return Symobl of Token.
     */
    public String getTokenSymbol() throws Exception {
        // Load the contract: load the contract object using Wrapper class(RaewooCoin).
        RaewooCoin tokenContract = RaewooCoin.load(contractAddress, web3j, credentials, new DefaultGasProvider());

        return tokenContract.symbol().send();
    }

    /**
     * transfer ERC-20 token to specific address
     * @param toAddress address that receives the tokens
     * @param amount amount to send in unit ETH
     * @return transaction hash
     */
    public String sendToken(String toAddress, BigDecimal amount) throws Exception {
        // Load the contract: load the contract object using Wrapper class(RaewooCoin).
        RaewooCoin tokenContract = RaewooCoin.load(contractAddress, web3j, credentials, new DefaultGasProvider());

        // Translate ETH to Wei
        BigInteger amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

        // Call transfer function from contract, and send the token.
        // At the moment .send() is called, the transaction, signed with private key in application.properties, will be sent to Alchemy node.
        TransactionReceipt transactionReceipt = tokenContract.transfer(toAddress, amountInWei).send();

        // Return the transaction hash.
        return transactionReceipt.getTransactionHash();
    }

    /**
     * approve the spender to withdraw certain amount of Token from my wallet address
     * @param spenderAddress address to be approved
     * @param amount amount to approve to withdraw
     * @return transaction hash, from, to address
     * @throws Exception
     */
    public Map<String, String> approveToken(String spenderAddress, BigDecimal amount) throws Exception {
        RaewooCoin tokenContract = RaewooCoin.load(contractAddress, web3j, credentials, new DefaultGasProvider());
        BigInteger amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

        TransactionReceipt receipt = tokenContract.transfer(spenderAddress, amountInWei).send();

        return Map.of(
                "transactionHash", receipt.getTransactionHash(),
                "fromAddress", receipt.getFrom(), // 트랜잭션을 보낸 주소 (우리 서버 지갑)
                "toAddress", receipt.getTo(),     // 트랜잭션이 전송된 주소 (토큰 컨트랙트)
                "spenderAddress", spenderAddress    // approve 함수에 인자로 넘긴 주소
        );
    }

    /**
     * Get how much the spender can withdraw from the owner wallet
     * @param ownerAddress
     * @param spenderAddress
     * @return
     * @throws Exception
     */
    public BigDecimal getAllowance(String ownerAddress, String spenderAddress) throws Exception {
        RaewooCoin tokenContract = RaewooCoin.load(contractAddress, web3j, credentials, new DefaultGasProvider());

        BigInteger allowanceToWei = tokenContract.allowance(ownerAddress, spenderAddress).send();

        return Convert.fromWei(new BigDecimal(allowanceToWei), Convert.Unit.ETHER);
    }

    /**
     * In the limit of approved, the wallet transfer
     * @param fromAddress
     * @param toAddress
     * @param amount
     * @return
     * @throws Exception
     */
    public Map<String, String> transferTokenFrom(String fromAddress, String toAddress, BigDecimal amount) throws Exception {
        RaewooCoin tokenContract = RaewooCoin.load(contractAddress, web3j, credentials, new DefaultGasProvider());
        BigInteger amountToWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

        TransactionReceipt receipt = tokenContract.transferFrom(fromAddress, toAddress, amountToWei).send();

        return Map.of(
                "transactionHash", receipt.getTransactionHash(),
                "fromAddress", receipt.getFrom(), // 토큰이 인출된 주소
                "toAddress", receipt.getTo()     // 토큰이 전송된 주소
        );
    }

    /**
     * deposit specific amount of token to wallet
     * @param amount how much to deposit
     * @return transaction hash
     * @throws Exception
     */
    public String depositToSimpleWallet(BigDecimal amount) throws Exception {
        TransactionReceipt receipt = Transfer.sendFunds(
                web3j, credentials, simpleWalletContractAddress,
                amount, Convert.Unit.ETHER
        ).send();

        return receipt.getTransactionHash();
    }

    /**
     * withdraw specific token from the wallet
     * @param tokenAddress what token to extract
     * @param amount how much to be extracted
     * @return transaction hash
     * @throws Exception
     */
    public String withdrawFromSimpleWallet(String tokenAddress, BigDecimal amount) throws Exception {
        SimpleWallet walletContract = SimpleWallet.load(simpleWalletContractAddress, web3j, credentials, new DefaultGasProvider());
        BigInteger amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

        TransactionReceipt receipt = walletContract.withdrawErc20(tokenAddress, amountInWei).send();
        return receipt.getTransactionHash();
    }

    /**
     * Get the transaction receipt from the transaction hash.
     * @param txHash transaction hash to check
     * @return transaction receipt. In case of pending, it will return null.
     */
    public TransactionReceipt getTransactionReceipt(String txHash) throws Exception {
        return web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt().orElse(null);
    }

    /**
     * Check the status of transaction
     * @param txHash transaction hash to check
     * @return success: true, else(pending or fail): false
     */
    public boolean isTransactionSuccessful(String txHash) throws Exception {
        TransactionReceipt receipt = getTransactionReceipt(txHash);

        if (receipt == null) {
            System.out.println("Transaction " + txHash + " is still pending...");
            return false; //pending
        }

        // "0x1" (success): true, "0x0" (fail): false
        return "0x1".equals(receipt.getStatus());
    }

}