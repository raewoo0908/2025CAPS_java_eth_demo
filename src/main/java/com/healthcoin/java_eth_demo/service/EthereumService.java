package com.healthcoin.java_eth_demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Convert;
import com.healthcoin.java_eth_demo.contracts.RaewooCoin; // 1단계에서 생성한 Wrapper 클래스
import org.springframework.beans.factory.annotation.Value;
import org.web3j.crypto.Credentials; // 미리 추가
import org.web3j.tx.gas.DefaultGasProvider; // 미리 추가

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

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
}