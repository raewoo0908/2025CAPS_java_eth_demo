package com.healthcoin.java_eth_demo.service;

import com.healthcoin.java_eth_demo.contracts.SimpleWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import org.web3j.crypto.Credentials;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.contracts.eip20.generated.ERC20;

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
     * @param ownerAddress          wallet address to get balance from.
     * @param tokenContractAddress  token contract address
     * @param ownerCredentials      owner credentials
     * @return
     * @throws Exception
     */
    public BigDecimal getTokenBalance(String ownerAddress,
                                      String tokenContractAddress,
                                      Credentials ownerCredentials) throws Exception {
        // Load the contract
        ERC20 tokenContract = ERC20.load(tokenContractAddress, web3j, ownerCredentials, new DefaultGasProvider());

        // Call balanceOf function from contract.
        BigInteger balanceInWei = tokenContract.balanceOf(ownerAddress).send();

        // Return the balance in Ether.
        return Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER);
    }

    /**
     * Get full Name of ERC-20 Token.
     * @return Name of Token.
     */
    public String getTokenName(String tokenContractAddress,
                               Credentials credentials) throws Exception {
        // Load the contract
        ERC20 tokenContract = ERC20.load(tokenContractAddress, web3j, credentials, new DefaultGasProvider());

        return tokenContract.name().send();
    }

    /**
     * Get full Symbol of ERC-20 Token.
     * @return Symobl of Token.
     */
    public String getTokenSymbol(String tokenContractAddress,
                                 Credentials credentials) throws Exception {
        // Load the contract
        ERC20 tokenContract = ERC20.load(tokenContractAddress, web3j, credentials, new DefaultGasProvider());

        return tokenContract.symbol().send();
    }

    /**
     * transfer ERC-20 token to specific address
     * @param tokenContractAddress  token contract address
     * @param fromCredentials       credentials that send token from
     * @param toAddress             address that receives the tokens
     * @param amount                amount to send in unit ETH
     * @return                      transaction receipt
     * @throws Exception
     */
    public TransactionReceipt sendToken(String tokenContractAddress,
                            Credentials fromCredentials,
                            String toAddress,
                            BigDecimal amount) throws Exception {
        // Load the contract
        ERC20 tokenContract = ERC20.load(tokenContractAddress, web3j, fromCredentials, new DefaultGasProvider());

        // Translate ETH to Wei
        BigInteger amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

        // Call transfer function from contract, and send the token.
        // At the moment .send() is called, the transaction, signed with private key in application.properties, will be sent to Alchemy node.
        TransactionReceipt transactionReceipt = tokenContract.transfer(toAddress, amountInWei).send();

        // Return the transaction hash.
        return transactionReceipt;
    }

    /**
     * approve the spender to withdraw certain amount of Token from my wallet address
     * @param tokenContractAddress Token contract
     * @param spenderAddress address to be approved
     * @param ownerCredential credential object
     * @param amount amount to approve to withdraw
     * @return transaction hash, from, to address
     * @throws Exception
     */
    public Map<String, String> approveToken(String tokenContractAddress,
                                            Credentials ownerCredential,
                                            String spenderAddress,
                                            BigDecimal amount) throws Exception {
        ERC20 tokenContract = ERC20.load(tokenContractAddress, web3j, ownerCredential, new DefaultGasProvider());
        BigInteger amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

        TransactionReceipt receipt = tokenContract.approve(spenderAddress, amountInWei).send();

        return Map.of(
                "transactionHash", receipt.getTransactionHash(),
                "fromAddress", receipt.getFrom(), // 트랜잭션을 보낸 주소 (우리 서버 지갑)
                "toAddress", receipt.getTo(),     // 트랜잭션이 전송된 주소 (토큰 컨트랙트)
                "spenderAddress", spenderAddress    // approve 함수에 인자로 넘긴 주소
        );
    }

    /**
     * Get how much the spender can withdraw from the owner wallet
     * @param tokenContractAddress Token Contract address
     * @param credentials any Credentials
     * @param ownerAddress EOA who owns the token
     * @param spenderAddress EOA who are allowed to use the token
     * @return
     * @throws Exception
     */
    public BigDecimal getAllowance(String tokenContractAddress,
                                   Credentials credentials,
                                   String ownerAddress,
                                   String spenderAddress) throws Exception {
        ERC20 tokenContract = ERC20.load(tokenContractAddress, web3j, credentials, new DefaultGasProvider());

        BigInteger allowanceToWei = tokenContract.allowance(ownerAddress, spenderAddress).send();

        return Convert.fromWei(new BigDecimal(allowanceToWei), Convert.Unit.ETHER);
    }

    /**
     * In the limit of approved, the "spender" transfer the token from "owner" to "reciptient".
     * @param tokenContractAddress
     * @param spenderCredential
     * @param ownerCredential
     * @param ownerAddress
     * @param recipientAddress
     * @param amount
     * @return
     * @throws Exception
     */
    public TransactionReceipt transferTokenFrom(String tokenContractAddress,
                                                 Credentials spenderCredential,
                                                 Credentials ownerCredential,
                                                 String ownerAddress,
                                                 String recipientAddress,
                                                 BigDecimal amount) throws Exception {
        // condition 1: allowance must be bigger than amount
        BigDecimal allowance = getAllowance(tokenContractAddress, spenderCredential, ownerAddress, spenderCredential.getAddress());
        System.out.println("spenderAddresss: " + spenderCredential.getAddress());
        if (amount.compareTo(allowance) > 0) {
            throw new RuntimeException("Amount exceeds allowance. Allowance: " + allowance + " Amount: " + amount);
        }

        //condition 2: owner must have enough token.
        BigDecimal balance = getTokenBalance(ownerAddress, tokenContractAddress, ownerCredential);
        if (amount.compareTo(balance) > 0) {
           throw new RuntimeException("Amount exceeds balance");
        }

        ERC20 tokenAsSpender = ERC20.load(tokenContractAddress, web3j, spenderCredential, new DefaultGasProvider());

        BigInteger amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

        TransactionReceipt receipt = tokenAsSpender.transferFrom(ownerAddress, recipientAddress, amountInWei).send();

        return receipt;
    }

    /**
     * Deposit owner's ERC20 to wallet.
     * @param walletContractAddress wallet CA deposit to
     * @param ownerCredentials      EOA of token owner
     * @param tokenContractAddress  token CA to deposit
     * @param amount                amount of token to deposit
     * @return                      transaction receipt
     * @throws Exception
     */
    public TransactionReceipt depositERC20(String walletContractAddress,
                                           Credentials ownerCredentials,
                                           String tokenContractAddress,
                                           BigDecimal amount) throws Exception {

        BigDecimal allowance = getAllowance(tokenContractAddress, ownerCredentials, ownerCredentials.getAddress(), walletContractAddress);

        // allowance must be greater than amount.
        if (amount.compareTo(allowance) > 0) {
            throw new RuntimeException("Amount exceeds allowance");
        }

        // amount must be greater than zero
        if (amount.compareTo(new BigDecimal(0)) <= 0){
            throw new RuntimeException("Amount must be greater than zero");
        }

        // load wallet contract on behalf of Owner credentials.
        SimpleWallet walletContract = SimpleWallet.load(
                walletContractAddress,
                web3j,
                ownerCredentials,
                new DefaultGasProvider());

        BigInteger amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

        TransactionReceipt receipt = walletContract.depositErc20(tokenContractAddress, amountInWei).send();

        return receipt;
    }

    /**
     * Withdraw ERC20 from wallet to specific EOA.
     * @param walletContractAddress
     * @param ownerCredentials
     * @param tokenContractAddress
     * @param amount
     * @return
     * @throws Exception
     */
    public TransactionReceipt withdrawERC20(String walletContractAddress,
                                            Credentials ownerCredentials,
                                            String tokenContractAddress,
                                            BigDecimal amount) throws Exception {

        BigDecimal balance = getERC20BalanceFromWallet(walletContractAddress, ownerCredentials, tokenContractAddress);

        // balance must be greater than amount
        if (amount.compareTo(balance) > 0) {
            throw new RuntimeException("Amount exceeds balance");
        }

        SimpleWallet walletContract = SimpleWallet.load(
                walletContractAddress,
                web3j,
                ownerCredentials,
                new DefaultGasProvider());

        BigInteger amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

        TransactionReceipt receipt = walletContract.withdrawErc20(tokenContractAddress, amountInWei).send();

        return receipt;
    }

    /**
     * Get specific owner's balance of ERC20 from wallet.
     * @param walletContractAddress
     * @param ownerCredentials
     * @param tokenContractAddress
     * @return
     * @throws Exception
     */
    public BigDecimal getERC20BalanceFromWallet(String walletContractAddress, Credentials ownerCredentials, String tokenContractAddress) throws Exception{

        SimpleWallet walletContract = SimpleWallet.load(
                walletContractAddress,
                web3j,
                ownerCredentials,
                new DefaultGasProvider());

        BigInteger balanceToWei = walletContract.getErc20Balance(tokenContractAddress).send();

        return Convert.fromWei(new BigDecimal(balanceToWei), Convert.Unit.ETHER);
    }
}