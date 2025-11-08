package com.healthcoin.java_eth_demo.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.7.0.
 */
@SuppressWarnings("rawtypes")
public class TOGame extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_CURRENTROUND = "currentRound";

    public static final String FUNC_GETCONTRACTBALANCE = "getContractBalance";

    public static final String FUNC_GETROUNDINFO = "getRoundInfo";

    public static final String FUNC_GETROUNDPLAYERS = "getRoundPlayers";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_ROUNDS = "rounds";

    public static final String FUNC_SETWINNERPERCENTAGE = "setWinnerPercentage";

    public static final String FUNC_STARTNEWROUND = "startNewRound";

    public static final String FUNC_SUBMIT = "submit";

    public static final String FUNC_TOKEN = "token";

    public static final String FUNC_WINNERPERCENTAGE = "winnerPercentage";

    public static final String FUNC_WINNERS = "winners";

    public static final Event GAMEEND_EVENT = new Event("GameEnd", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event NUMBERSUBMITTED_EVENT = new Event("NumberSubmitted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event WINNERPERCENTAGEUPDATED_EVENT = new Event("WinnerPercentageUpdated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected TOGame(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TOGame(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected TOGame(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected TOGame(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<GameEndEventResponse> getGameEndEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(GAMEEND_EVENT, transactionReceipt);
        ArrayList<GameEndEventResponse> responses = new ArrayList<GameEndEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            GameEndEventResponse typedResponse = new GameEndEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.round = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.winner = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.finalIndex = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.prizeAmount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static GameEndEventResponse getGameEndEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(GAMEEND_EVENT, log);
        GameEndEventResponse typedResponse = new GameEndEventResponse();
        typedResponse.log = log;
        typedResponse.round = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.winner = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.finalIndex = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.prizeAmount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<GameEndEventResponse> gameEndEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getGameEndEventFromLog(log));
    }

    public Flowable<GameEndEventResponse> gameEndEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(GAMEEND_EVENT));
        return gameEndEventFlowable(filter);
    }

    public static List<NumberSubmittedEventResponse> getNumberSubmittedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(NUMBERSUBMITTED_EVENT, transactionReceipt);
        ArrayList<NumberSubmittedEventResponse> responses = new ArrayList<NumberSubmittedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            NumberSubmittedEventResponse typedResponse = new NumberSubmittedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.round = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.player = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.number = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.newIndex = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static NumberSubmittedEventResponse getNumberSubmittedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(NUMBERSUBMITTED_EVENT, log);
        NumberSubmittedEventResponse typedResponse = new NumberSubmittedEventResponse();
        typedResponse.log = log;
        typedResponse.round = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.player = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.number = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.newIndex = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<NumberSubmittedEventResponse> numberSubmittedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getNumberSubmittedEventFromLog(log));
    }

    public Flowable<NumberSubmittedEventResponse> numberSubmittedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(NUMBERSUBMITTED_EVENT));
        return numberSubmittedEventFlowable(filter);
    }

    public static List<WinnerPercentageUpdatedEventResponse> getWinnerPercentageUpdatedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(WINNERPERCENTAGEUPDATED_EVENT, transactionReceipt);
        ArrayList<WinnerPercentageUpdatedEventResponse> responses = new ArrayList<WinnerPercentageUpdatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            WinnerPercentageUpdatedEventResponse typedResponse = new WinnerPercentageUpdatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.newPercentage = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static WinnerPercentageUpdatedEventResponse getWinnerPercentageUpdatedEventFromLog(
            Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(WINNERPERCENTAGEUPDATED_EVENT, log);
        WinnerPercentageUpdatedEventResponse typedResponse = new WinnerPercentageUpdatedEventResponse();
        typedResponse.log = log;
        typedResponse.newPercentage = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<WinnerPercentageUpdatedEventResponse> winnerPercentageUpdatedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getWinnerPercentageUpdatedEventFromLog(log));
    }

    public Flowable<WinnerPercentageUpdatedEventResponse> winnerPercentageUpdatedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(WINNERPERCENTAGEUPDATED_EVENT));
        return winnerPercentageUpdatedEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> currentRound() {
        final Function function = new Function(FUNC_CURRENTROUND, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> getContractBalance() {
        final Function function = new Function(FUNC_GETCONTRACTBALANCE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple4<BigInteger, BigInteger, Boolean, BigInteger>> getRoundInfo(
            BigInteger _round) {
        final Function function = new Function(FUNC_GETROUNDINFO, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_round)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple4<BigInteger, BigInteger, Boolean, BigInteger>>(function,
                new Callable<Tuple4<BigInteger, BigInteger, Boolean, BigInteger>>() {
                    @Override
                    public Tuple4<BigInteger, BigInteger, Boolean, BigInteger> call() throws
                            Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<BigInteger, BigInteger, Boolean, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (Boolean) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue());
                    }
                });
    }

    public RemoteFunctionCall<List> getRoundPlayers(BigInteger _round) {
        final Function function = new Function(FUNC_GETROUNDPLAYERS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_round)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Player>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Tuple4<BigInteger, BigInteger, Boolean, BigInteger>> rounds(
            BigInteger param0) {
        final Function function = new Function(FUNC_ROUNDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple4<BigInteger, BigInteger, Boolean, BigInteger>>(function,
                new Callable<Tuple4<BigInteger, BigInteger, Boolean, BigInteger>>() {
                    @Override
                    public Tuple4<BigInteger, BigInteger, Boolean, BigInteger> call() throws
                            Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<BigInteger, BigInteger, Boolean, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (Boolean) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> setWinnerPercentage(BigInteger _newPercentage) {
        final Function function = new Function(
                FUNC_SETWINNERPERCENTAGE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_newPercentage)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> startNewRound() {
        final Function function = new Function(
                FUNC_STARTNEWROUND, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> submit(BigInteger _round, BigInteger _number,
            BigInteger _amount) {
        final Function function = new Function(
                FUNC_SUBMIT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_round), 
                new org.web3j.abi.datatypes.generated.Uint256(_number), 
                new org.web3j.abi.datatypes.generated.Uint256(_amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> token() {
        final Function function = new Function(FUNC_TOKEN, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> winnerPercentage() {
        final Function function = new Function(FUNC_WINNERPERCENTAGE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> winners(BigInteger param0) {
        final Function function = new Function(FUNC_WINNERS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    @Deprecated
    public static TOGame load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new TOGame(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static TOGame load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TOGame(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static TOGame load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new TOGame(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static TOGame load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new TOGame(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class Player extends StaticStruct {
        public String playerAddress;

        public BigInteger amount;

        public Player(String playerAddress, BigInteger amount) {
            super(new org.web3j.abi.datatypes.Address(160, playerAddress), 
                    new org.web3j.abi.datatypes.generated.Uint256(amount));
            this.playerAddress = playerAddress;
            this.amount = amount;
        }

        public Player(Address playerAddress, Uint256 amount) {
            super(playerAddress, amount);
            this.playerAddress = playerAddress.getValue();
            this.amount = amount.getValue();
        }
    }

    public static class GameEndEventResponse extends BaseEventResponse {
        public BigInteger round;

        public String winner;

        public BigInteger finalIndex;

        public BigInteger prizeAmount;
    }

    public static class NumberSubmittedEventResponse extends BaseEventResponse {
        public BigInteger round;

        public String player;

        public BigInteger number;

        public BigInteger newIndex;
    }

    public static class WinnerPercentageUpdatedEventResponse extends BaseEventResponse {
        public BigInteger newPercentage;
    }
}
