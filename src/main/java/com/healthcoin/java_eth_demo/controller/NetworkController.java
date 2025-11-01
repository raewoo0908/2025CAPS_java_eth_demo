package com.healthcoin.java_eth_demo.controller;

import com.healthcoin.java_eth_demo.service.EthereumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigInteger;
import java.util.Map;

@Controller
@RequestMapping("/network")
public class NetworkController {

    @Autowired
    private EthereumService ethereumService;

    @Value("${alchemy.api-url}")
    private String networkUrl;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getNetworkStatus(){
        try{
            BigInteger blockNumber = ethereumService.getLatestBlockNumber();
            Map<String, Object> response = Map.of(
                    "status", "Connected",
                    "networkUrl", networkUrl,
                    "latestBlockNumber", blockNumber
            );
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.status(503).body(Map.of(
                    "status", "Disconnected",
                    "error", e.getMessage()
            ));
        }
    }
}
