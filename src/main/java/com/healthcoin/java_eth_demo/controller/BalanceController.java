package com.healthcoin.java_eth_demo.controller;

import com.healthcoin.java_eth_demo.service.EthereumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/balance")
public class BalanceController {

    @Autowired
    private EthereumService ethereumService;

    @GetMapping("/eth/{address}")
    public ResponseEntity<Map<String, Object>> getEthBalance(@PathVariable String address) {
        try {
            BigDecimal balance = ethereumService.getEthBalance(address);
            Map<String, Object> response = Map.of("address", address, "currency", "ETH", "balance", balance);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }

    }
}
