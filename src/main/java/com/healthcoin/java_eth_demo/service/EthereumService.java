package com.healthcoin.java_eth_demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

// Notate Spring that this class is a component of Service layer.
@Service
public class EthereumService {

    // Inject Web3j Bean to we3j object.
    @Autowired
    private Web3j web3j;

    /**
     * Gets the latest block number.
     */
    public BigInteger getLatestBlockNumber() throws IOException {
        EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();
        return ethBlockNumber.getBlockNumber();
    }

    /**
     * get ETH balance of specific wallet address.
     * @param address address of wallet.
     */
    public BigDecimal getEthBalance(String address) throws IOException {
        EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();

        // 3. translate the WEI to ETH
        BigInteger weiBalance = ethGetBalance.getBalance();
        return Convert.fromWei(new BigDecimal(weiBalance), Convert.Unit.ETHER);
    }
}