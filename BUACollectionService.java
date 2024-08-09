package com.stanbic.bua.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.stanbic.bua.dto.*;
import com.stanbic.bua.entity.PayloadLog;
import com.stanbic.bua.repository.ProducerPayloadRepository;
import com.stanbic.bua.repository.TransactionCheckRepository;
import com.stanbic.bua.util.DateUtil;
import com.stanbic.bua.util.WebClientUtil;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class BUACollectionService {
    @Value("${bua.access.token}")
    private String accessToken;
    @Value("${bua.cement.validate.order.endpoint}")
    private String validateOrderEndpoint;

    @Value("${bua.cement.verify.payment.endpoint}")
    private String verifyOrderEndpoint;

    @Value("${bua.cement.payment.endpoint}")
    private String newPaymentCementEndpoint;

    @Value("${bua.cement.legacy.payment.endpoint}")
    private String legacyPaymentCementEndpoint;


    @Value("${bua.food.validate.order.endpoint}")
    private String validateFoodOrderEndpoint;

    @Value("${bua.food.verify.payment.endpoint}")
    private String verifyFoodOrderEndpoint;

    @Value("${bua.food.payment.endpoint}")
    private String newPaymentFoodEndpoint;

    @Value("${bua.food.legacy.payment.endpoint}")
    private String legacyPaymentFoodEndpoint;

    @Value("${bua.cement.account.number}")
    private String cementAccountNumber;

    @Value("${bua.cement.sokoto.account.number}")
    private String cementAccountSokotoNumber;

    @Value("${bua.food.account.number}")
    private String foodAccountNumber;

    @Value("${isOverProxy}")
    private boolean useProxy;

    private final ProducerPayloadRepository producerPayloadRepository;
    private final TransactionCheckRepository transactionCheckRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(BUACollectionService.class);

    @Autowired
    WebClientUtil webClientUtil;
    DateUtil dateUtil = new DateUtil();
    public ValidateCementOrderResponse validateCementOrder(ValidateCementOrderRequest request) throws JsonProcessingException, JSONException, JSONException {
        LOGGER.info("Executing validate order........");
        String validateOrderRequest = "{\n"
                + "           \"orderid\":\"" + request.getOtherRequestDetails().getOrderid() + "\"\n"
                + "      }";
        LOGGER.info("request" + validateOrderRequest);
        ValidateCementOrderResponse  validateCementOrderResponse = new ValidateCementOrderResponse();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);

        String response = webClientUtil.httpPostRequest(validateOrderEndpoint,validateOrderRequest,"POST",useProxy,headers);
        LOGGER.info("response"+response);
        Date currentDate = new Date();
        PayloadLog log = new PayloadLog();
        log.setLogCreateTime(currentDate);
        log.setLogUpdateTime(currentDate);
        log.setBiller("BUA_CEMENT");
        log.setCollectionType("VALIDATION");
        log.setAPIUrl(validateOrderEndpoint);
        String tranId = request.getOtherRequestDetails().getOrderid() + "||"+ dateUtil.createRequestUUID2();
        log.setTranId(tranId);
        log.setRequestPayload(validateOrderRequest);
        log.setResponsePayload(response);
        log.setProducerReqPayload(validateOrderRequest);
        log.setProducerRespPayload(response);
        log.setReversalReqPayload("");
        log.setReversalRespPayload("");
        log.setTransferReqPayload("");
        log.setTransferRespPayload("");
        log.setTransactionDate(currentDate);
        LOGGER.info("log"+log);
        producerPayloadRepository.save(log);

        JSONObject jsonResponse = new JSONObject(response);
        String httpStatusCode = jsonResponse.getString("status");
        String message = jsonResponse.getString("message");
        String name = jsonResponse.getString("name");
        String amount = jsonResponse.getString("amount");

        OtherResponseDetails otherResponseDetails = new OtherResponseDetails();
        otherResponseDetails.setName(name);
        otherResponseDetails.setAmount(amount);
        otherResponseDetails.setMessage(message);

        if(response != null && httpStatusCode != null){
            if(httpStatusCode.equals("200")){
                validateCementOrderResponse.setResponseCode("000");
                validateCementOrderResponse.setResponseMessage(message);
                validateCementOrderResponse.setOtherResponseDetails(otherResponseDetails);
            }
            else if (!httpStatusCode.equals("200")){
                validateCementOrderResponse.setResponseCode("000");
                validateCementOrderResponse.setResponseMessage(message);
                validateCementOrderResponse.setOtherResponseDetails(otherResponseDetails);
            } else{
                validateCementOrderResponse.setResponseCode("99");
                validateCementOrderResponse.setResponseMessage("Connection Timed out");
            }
        }else{
            validateCementOrderResponse.setResponseCode("99");
            validateCementOrderResponse.setResponseMessage("Error while processing vendor response payload");
        }
        return validateCementOrderResponse;
    }

    public VerifyPaymentResponse verifyCementPayment(VerifyPaymentRequest request) throws JsonProcessingException, JSONException {
        String verifyPaymentRequest = "{\"creditaccountnumber\":\"" + request.getOtherRequestDetails().getCreditaccountnumber() + "\",\n"
                + "           \"bank_transaction_id\":\"" + request.getTransactionId() + "\"\n"
                + "      }";
        LOGGER.info("request" + verifyPaymentRequest);
        VerifyPaymentResponse  verifyPaymentResponse = new VerifyPaymentResponse();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);

        String response = webClientUtil.httpPostRequest(verifyOrderEndpoint,verifyPaymentRequest,"POST",useProxy,headers);
        LOGGER.info("verifyOrderEndpoint>>>>>>"+verifyOrderEndpoint);
        LOGGER.info("response"+response);
        Date currentDate = new Date();
        PayloadLog log = new PayloadLog();
        log.setLogCreateTime(currentDate);
        log.setLogUpdateTime(currentDate);
        log.setBiller("BUA_CEMENT");
        log.setCollectionType("VALIDATION");
        log.setAPIUrl(verifyOrderEndpoint);
        String tranId = request.getOtherRequestDetails().getOrderid() + "||"+ dateUtil.createRequestUUID2();
        log.setTranId(tranId);
        log.setRequestPayload(verifyPaymentRequest);
        log.setResponsePayload(response);
        log.setProducerReqPayload(verifyPaymentRequest);
        log.setProducerRespPayload(response);
        log.setReversalReqPayload("");
        log.setReversalRespPayload("");
        log.setTransferReqPayload("");
        log.setTransferRespPayload("");
        log.setTransactionDate(currentDate);
        LOGGER.info("log"+log);
        producerPayloadRepository.save(log);

        JSONObject jsonResponse = new JSONObject(response);
        String httpStatusCode = jsonResponse.getString("status");
        String message = jsonResponse.getString("message");
        String name = jsonResponse.getString("name");
        String amount = jsonResponse.getString("amount");

        OtherResponseDetails otherResponseDetails = new OtherResponseDetails();
        otherResponseDetails.setName(name);
        otherResponseDetails.setAmount(amount);
        otherResponseDetails.setMessage(message);
        LOGGER.info("httpStatusCode"+httpStatusCode);

        if(response != null && httpStatusCode != null){
            if(httpStatusCode.equals("200")){
                verifyPaymentResponse.setResponseCode("000");
                verifyPaymentResponse.setResponseMessage(message);
                verifyPaymentResponse.setOtherResponseDetails(otherResponseDetails);
            }
            else if (!httpStatusCode.equals("200")){
                verifyPaymentResponse.setResponseCode("000");
                verifyPaymentResponse.setResponseMessage(message);
            } else{
                verifyPaymentResponse.setResponseCode("99");
                verifyPaymentResponse.setResponseMessage("Connection Timed out");
            }
        }else{
            verifyPaymentResponse.setResponseCode("99");
            verifyPaymentResponse.setResponseMessage("Error while processing vendor response payload");
        }
        return verifyPaymentResponse;
    }

    public MakePaymentResponse postCementPaymentNotification(MakePaymentRequest request) throws JsonProcessingException, JSONException {
        String validateOrderRequest = "{\n"
                + "           \"orderid\":\"" + request.getOtherRequestDetails().getOrderid() + "\"\n"
                + "      }";
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);
        String response = webClientUtil.httpPostRequest(validateOrderEndpoint,validateOrderRequest,"POST",useProxy,headers);
        JSONObject jsonRes = new JSONObject(response);
        String httpStatusCode = jsonRes.getString("status");
        LOGGER.info("validate order response"+response);
        String httpCallResponseNewPaymentEndpoint;
        if (httpStatusCode.equals("200")) {
            String newPaymentEndpointRequest = "{\n"
                    + "           \"orderid\":\"" + request.getOtherRequestDetails().getOrderid() + "\",\n"
                    + "           \"debitaccountnumber\":\"" + request.getOtherRequestDetails().getDebitaccountnumber() + "\",\n"
                    + "           \"creditaccountnumber\":\"" + request.getOtherRequestDetails().getCreditaccountnumber() + "\",\n"
                    + "           \"amount\":\"" + request.getPrincipalAmount() + "\",\n"
                    + "           \"bank_transaction_id\":\"" + request.getTransactionId() + "\",\n"
                    + "           \"paymentdate\":\"" + dateUtil.getNowTimeAsNumbersWithSSS() + "\"\n"
                    + "      }";
            LOGGER.info("newPaymentEndpointRequest"+newPaymentEndpointRequest);

            httpCallResponseNewPaymentEndpoint = webClientUtil.httpPostRequest(newPaymentCementEndpoint,newPaymentEndpointRequest,"POST",useProxy,headers);
            Date currentDate = new Date();
            PayloadLog log = new PayloadLog();
            log.setLogCreateTime(currentDate);
            log.setLogUpdateTime(currentDate);
            log.setBiller("BUA_CEMENT");
            log.setCollectionType("PAYMENT");
            log.setAPIUrl(newPaymentCementEndpoint);
            String tranId = request.getOtherRequestDetails().getOrderid() + "||"+ dateUtil.createRequestUUID2();
            log.setTranId(tranId);
            log.setRequestPayload(newPaymentEndpointRequest);
            log.setResponsePayload(httpCallResponseNewPaymentEndpoint);
            log.setProducerReqPayload(newPaymentCementEndpoint);
            log.setProducerRespPayload(httpCallResponseNewPaymentEndpoint);
            log.setReversalReqPayload("");
            log.setReversalRespPayload("");
            log.setTransferReqPayload("");
            log.setTransferRespPayload("");
            log.setTransactionDate(currentDate);
            LOGGER.info("log"+log);
            producerPayloadRepository.save(log);
        } else {
            String legacyEndpointRequest = "{\n"
                    + "           \"name\":\"" + request.getOtherRequestDetails().getName() + "\",\n"
                    + "           \"debitaccountnumber\":\"" + request.getOtherRequestDetails().getDebitaccountnumber() + "\",\n"
                    + "           \"creditaccountnumber\":\"" + request.getOtherRequestDetails().getCreditaccountnumber() + "\",\n"
                    + "           \"amount\":\"" + request.getPrincipalAmount() + "\",\n"
                    + "           \"bank_transaction_id\":\"" + request.getTransactionId() + "\",\n"
                    + "           \"paymentdate\":\"" + dateUtil.getNowTimeAsNumbersWithSSS() + "\"\n"
                    + "      }";
            LOGGER.info("legacyEndpointRequest"+legacyEndpointRequest);
            httpCallResponseNewPaymentEndpoint = webClientUtil.httpPostRequest(legacyPaymentCementEndpoint,legacyEndpointRequest,"POST",useProxy,headers);

            Date currentDate = new Date();
            PayloadLog log = new PayloadLog();
            log.setLogCreateTime(currentDate);
            log.setLogUpdateTime(currentDate);
            log.setBiller("BUA_CEMENT");
            log.setCollectionType("PAYMENT");
            log.setAPIUrl(legacyPaymentCementEndpoint);
            String tranId = request.getOtherRequestDetails().getOrderid() + "||"+ dateUtil.createRequestUUID2();
            log.setTranId(tranId);
            log.setRequestPayload(legacyEndpointRequest);
            log.setResponsePayload(httpCallResponseNewPaymentEndpoint);
            log.setProducerReqPayload(legacyEndpointRequest);
            log.setProducerRespPayload(httpCallResponseNewPaymentEndpoint);
            log.setReversalReqPayload("");
            log.setReversalRespPayload("");
            log.setTransferReqPayload("");
            log.setTransferRespPayload("");
            log.setTransactionDate(currentDate);
            LOGGER.info("log"+log);
            producerPayloadRepository.save(log);
        }

        JSONObject jsonResponse = new JSONObject(httpCallResponseNewPaymentEndpoint);
        String status = jsonResponse.getString("status");
        String message2 = jsonResponse.getString("message");
        String name2 = jsonResponse.getString("name");
        String tranId2 = jsonResponse.getString("transactionid");

        MakePaymentResponse makePaymentResponse = new MakePaymentResponse();
        OtherResponseDetails otherResponseDetails = new OtherResponseDetails();
        otherResponseDetails.setName(name2);
        otherResponseDetails.setTranId(tranId2);
        otherResponseDetails.setMessage(message2);

        if (httpCallResponseNewPaymentEndpoint != null) {
            if (status.equals("200") || status.equals("100")) {
                makePaymentResponse.setResponseCode("000");
                makePaymentResponse.setResponseMessage("SUCCESS");
                makePaymentResponse.setOtherResponseDetails(otherResponseDetails);
            } else{
                makePaymentResponse.setResponseCode("99");
                makePaymentResponse.setResponseMessage("FAILED");
                makePaymentResponse.setOtherResponseDetails(otherResponseDetails);
            }
        } else {
            makePaymentResponse.setResponseCode("99");
            makePaymentResponse.setResponseMessage("Error while processing vendor response payload");
        }
        return makePaymentResponse;
    }

    public ValidateCementOrderResponse validateFoodOrder(ValidateCementOrderRequest request) throws JsonProcessingException, JSONException {
        String validateOrderRequest = "{\n"
                + "           \"orderid\":\"" + request.getOtherRequestDetails().getOrderid() + "\"\n"
                + "      }";
        ValidateCementOrderResponse  validateCementOrderResponse = new ValidateCementOrderResponse();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = webClientUtil.httpPostRequest(validateFoodOrderEndpoint,validateOrderRequest,"POST",useProxy,headers);
       LOGGER.info("response"+responseBody);

        Date currentDate = new Date();
        PayloadLog log = new PayloadLog();
        log.setLogCreateTime(currentDate);
        log.setLogUpdateTime(currentDate);
        log.setBiller("BUA_FOOD");
        log.setCollectionType("VALIDATION");
        log.setAPIUrl(validateFoodOrderEndpoint);
        String tranId = request.getOtherRequestDetails().getOrderid() + "||"+ dateUtil.createRequestUUID2();
        log.setTranId(tranId);
        log.setRequestPayload(validateOrderRequest);
        log.setResponsePayload(responseBody);
        log.setProducerReqPayload(validateOrderRequest);
        log.setProducerRespPayload(responseBody);
        log.setReversalReqPayload("");
        log.setReversalRespPayload("");
        log.setTransferReqPayload("");
        log.setTransferRespPayload("");
        log.setTransactionDate(currentDate);
        LOGGER.info("log"+log);
        producerPayloadRepository.save(log);
        JSONObject jsonResponse = new JSONObject(responseBody);
        String httpStatusCode = jsonResponse.getString("status");
        String message = jsonResponse.getString("message");
        String name = jsonResponse.getString("name");
        String amount = jsonResponse.getString("amount");


        OtherResponseDetails otherResponseDetails = new OtherResponseDetails();
        otherResponseDetails.setName(name);
        otherResponseDetails.setAmount(amount);
        otherResponseDetails.setMessage(message);
        LOGGER.info("httpStatusCode"+httpStatusCode);


        if(responseBody != null && httpStatusCode != null){
            if(httpStatusCode.equals("200")){
                validateCementOrderResponse.setResponseCode("000");
                validateCementOrderResponse.setResponseMessage(message);
                validateCementOrderResponse.setOtherResponseDetails(otherResponseDetails);
            }
            else if (!httpStatusCode.equals("200")){
                validateCementOrderResponse.setResponseCode("000");
                validateCementOrderResponse.setResponseMessage(message);
                validateCementOrderResponse.setOtherResponseDetails(otherResponseDetails);
            } else{
                validateCementOrderResponse.setResponseCode("99");
                validateCementOrderResponse.setResponseMessage("Connection Timed out");
            }
        }else{
            validateCementOrderResponse.setResponseCode("99");
            validateCementOrderResponse.setResponseMessage("Error while processing vendor response payload");
        }
        return validateCementOrderResponse;
    }

    public VerifyPaymentResponse verifyFoodPayment(VerifyPaymentRequest request) throws JsonProcessingException, JSONException {
        String verifyPaymentRequest = "{\"creditaccountnumber\":\"" + request.getOtherRequestDetails().getCreditaccountnumber() + "\",\n"
                + "           \"bank_transaction_id\":\"" + request.getTransactionId() + "\"\n"
                + "      }";
        VerifyPaymentResponse  verifyPaymentResponse = new VerifyPaymentResponse();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = webClientUtil.httpPostRequest(verifyFoodOrderEndpoint,verifyPaymentRequest,"POST",useProxy,headers);
        LOGGER.info("response"+responseBody);

        Date currentDate = new Date();
        PayloadLog log = new PayloadLog();
        log.setLogCreateTime(currentDate);
        log.setLogUpdateTime(currentDate);
        log.setBiller("BUA_FOOD");
        log.setCollectionType("VALIDATION");
        log.setAPIUrl(validateOrderEndpoint);
        String tranId = request.getOtherRequestDetails().getOrderid() + "||"+ dateUtil.createRequestUUID2();
        log.setTranId(tranId);
        log.setRequestPayload(verifyPaymentRequest);
        log.setResponsePayload(responseBody);
        log.setProducerReqPayload(verifyPaymentRequest);
        log.setProducerRespPayload(responseBody);
        log.setReversalReqPayload("");
        log.setReversalRespPayload("");
        log.setTransferReqPayload("");
        log.setTransferRespPayload("");
        log.setTransactionDate(currentDate);
        LOGGER.info("log"+log);
        producerPayloadRepository.save(log);

        JSONObject jsonResponse = new JSONObject(responseBody);
        String httpStatusCode = jsonResponse.getString("status");
        String message = jsonResponse.getString("message");
        String name = jsonResponse.getString("name");
        String amount = jsonResponse.getString("amount");

        OtherResponseDetails otherResponseDetails = new OtherResponseDetails();
        otherResponseDetails.setName(name);
        otherResponseDetails.setAmount(amount);
        otherResponseDetails.setMessage(message);
        LOGGER.info("httpStatusCode"+httpStatusCode);

        if(responseBody != null && httpStatusCode != null){
            if(httpStatusCode.equals("200")){
                verifyPaymentResponse.setResponseCode("000");
                verifyPaymentResponse.setResponseMessage(message);
                verifyPaymentResponse.setOtherResponseDetails(otherResponseDetails);
            }
            else if (!httpStatusCode.equals("200")){
                verifyPaymentResponse.setResponseCode("000");
                verifyPaymentResponse.setResponseMessage(message);
                verifyPaymentResponse.setOtherResponseDetails(otherResponseDetails);
            } else{
                verifyPaymentResponse.setResponseCode("99");
                verifyPaymentResponse.setResponseMessage("Connection Timed out");
            }
        }else{
            verifyPaymentResponse.setResponseCode("99");
            verifyPaymentResponse.setResponseMessage("Error while processing vendor response payload");
        }
        return verifyPaymentResponse;
    }

    public MakePaymentResponse postFoodPaymentNotification(MakePaymentRequest request) throws JsonProcessingException, JSONException {
        String validateOrderRequest = "{\n"
                + "           \"orderid\":\"" + request.getOtherRequestDetails().getOrderid() + "\"\n"
                + "      }";
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);
        String httpCallResponse = webClientUtil.httpPostRequest(validateFoodOrderEndpoint,validateOrderRequest,"POST",useProxy,headers);
        JSONObject jsonRes = new JSONObject(httpCallResponse);
        String httpStatusCode = jsonRes.getString("status");

        LOGGER.info("validate order status code"+httpStatusCode);
        String httpCallResponse2;
        if (httpStatusCode.equals("200")) {
            String newPaymentEndpointRequest = "{\n"
                    + "           \"orderid\":\"" + request.getOtherRequestDetails().getOrderid() + "\",\n"
                    + "           \"debitaccountnumber\":\"" + request.getOtherRequestDetails().getDebitaccountnumber() + "\",\n"
                    + "           \"creditaccountnumber\":\"" + request.getOtherRequestDetails().getCreditaccountnumber() + "\",\n"
                    + "           \"amount\":\"" + request.getPrincipalAmount() + "\",\n"
                    + "           \"bank_transaction_id\":\"" + request.getTransactionId() + "\",\n"
                    + "           \"paymentdate\":\"" + dateUtil.getNowTimeAsNumbersWithSSS() + "\"\n"
                    + "      }";
            LOGGER.info("newPaymentEndpointRequest"+newPaymentEndpointRequest);
            httpCallResponse2 = webClientUtil.httpPostRequest(newPaymentFoodEndpoint,newPaymentEndpointRequest,"POST",useProxy,headers);
            Date currentDate = new Date();
            PayloadLog log = new PayloadLog();
            log.setLogCreateTime(currentDate);
            log.setLogUpdateTime(currentDate);
            log.setBiller("BUA_FOOD");
            log.setCollectionType("PAYMENT");
            log.setAPIUrl(newPaymentFoodEndpoint);
            String tranId = request.getOtherRequestDetails().getOrderid() + "||"+ dateUtil.createRequestUUID2();
            log.setTranId(tranId);
            log.setRequestPayload(newPaymentEndpointRequest);
            log.setResponsePayload(httpCallResponse2);
            log.setProducerReqPayload(newPaymentEndpointRequest);
            log.setProducerRespPayload(httpCallResponse2);
            log.setReversalReqPayload("");
            log.setReversalRespPayload("");
            log.setTransferReqPayload("");
            log.setTransferRespPayload("");
            log.setTransactionDate(currentDate);
            LOGGER.info("log"+log);
            producerPayloadRepository.save(log);

        } else {
            String legacyEndpointRequest = "{\n"
                    + "           \"name\":\"" + request.getOtherRequestDetails().getName() + "\",\n"
                    + "           \"debitaccountnumber\":\"" + request.getOtherRequestDetails().getDebitaccountnumber() + "\",\n"
                    + "           \"creditaccountnumber\":\"" + request.getOtherRequestDetails().getCreditaccountnumber() + "\",\n"
                    + "           \"amount\":\"" + request.getPrincipalAmount() + "\",\n"
                    + "           \"bank_transaction_id\":\"" + request.getTransactionId() + "\",\n"
                    + "           \"paymentdate\":\"" + dateUtil.getNowTimeAsNumbersWithSSS() + "\"\n"
                    + "      }";
            LOGGER.info("legacyEndpointRequest"+legacyEndpointRequest);
            httpCallResponse2 = webClientUtil.httpPostRequest(legacyPaymentFoodEndpoint,legacyEndpointRequest,"POST",useProxy,headers);

            Date currentDate = new Date();
            PayloadLog log = new PayloadLog();
            log.setLogCreateTime(currentDate);
            log.setLogUpdateTime(currentDate);
            log.setBiller("BUA_FOOD");
            log.setCollectionType("PAYMENT");
            log.setAPIUrl(legacyPaymentFoodEndpoint);
            String tranId = request.getOtherRequestDetails().getOrderid() + "||"+ dateUtil.createRequestUUID2();
            log.setTranId(tranId);
            log.setRequestPayload(legacyEndpointRequest);
            log.setResponsePayload(httpCallResponse2);
            log.setProducerReqPayload(legacyEndpointRequest);
            log.setProducerRespPayload(httpCallResponse2);
            log.setReversalReqPayload("");
            log.setReversalRespPayload("");
            log.setTransferReqPayload("");
            log.setTransferRespPayload("");
            log.setTransactionDate(currentDate);
            LOGGER.info("log"+log);
            producerPayloadRepository.save(log);
        }

        JSONObject jsonResponse = new JSONObject(httpCallResponse2);
        String status = jsonResponse.getString("status");
        String message2 = jsonResponse.getString("message");
        String name2 = jsonResponse.getString("name");
        String tranId2 = jsonResponse.getString("transactionid");

        MakePaymentResponse makePaymentResponse = new MakePaymentResponse();
        OtherResponseDetails otherResponseDetails = new OtherResponseDetails();
        otherResponseDetails.setName(name2);
        otherResponseDetails.setTranId(tranId2);
        otherResponseDetails.setMessage(message2);

        if (httpCallResponse2 != null) {
            if (status.equals("200") || status.equals("100")) {
                makePaymentResponse.setResponseCode("000");
                makePaymentResponse.setResponseMessage(message2);
                makePaymentResponse.setOtherResponseDetails(otherResponseDetails);
            }  else {
                LOGGER.info("httpStatusCode2<<<<<<<<<<"+httpCallResponse2);
                makePaymentResponse.setResponseCode("99");
                makePaymentResponse.setResponseMessage("FAILED");
                makePaymentResponse.setOtherResponseDetails(otherResponseDetails);
            }
        } else {
            makePaymentResponse.setResponseCode("99");
            makePaymentResponse.setResponseMessage("Error while processing vendor response payload");
        }
        return makePaymentResponse;
    }

    public MakePaymentResponse reversalPaymentNotification(MakePaymentRequest request) throws JsonProcessingException, JSONException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);
        String creditAccountNumber =  request.getOtherRequestDetails().getCreditaccountnumber();
            String reversalRequest = "{\n"
                    + "           \"name\":\"" + request.getOtherRequestDetails().getName() + "\",\n"
                    + "           \"debitaccountnumber\":\"" + request.getOtherRequestDetails().getDebitaccountnumber() + "\",\n"
                    + "           \"creditaccountnumber\":\"" + creditAccountNumber + "\",\n"
                    + "           \"amount\":\"-" + request.getPrincipalAmount() + "\",\n"
                    + "           \"bank_transaction_id\":\"" + request.getOtherRequestDetails().getBank_transaction_id() + "-R\",\n"
                    + "           \"paymentdate\":\"" + dateUtil.getNowTimeAsNumbersWithSSS() + "\"\n"
                    + "      }";
            LOGGER.info("reversalRequest"+reversalRequest);
            String url;
            if(creditAccountNumber.equals(cementAccountNumber)){
                url = legacyPaymentCementEndpoint;
            }else if(creditAccountNumber.equals(foodAccountNumber)){
                    url = legacyPaymentFoodEndpoint;
            }else{
                url = legacyPaymentCementEndpoint;
            }
        String responseBody = webClientUtil.httpPostRequest(url,reversalRequest,"POST",useProxy,headers);

            Date currentDate = new Date();
            PayloadLog log = new PayloadLog();
            log.setLogCreateTime(currentDate);
            log.setLogUpdateTime(currentDate);
            log.setBiller("BUA_CEMENT");
            log.setCollectionType("REVERSAL");
            log.setAPIUrl(legacyPaymentCementEndpoint);
            String tranId = request.getOtherRequestDetails().getOrderid() + "||"+ dateUtil.createRequestUUID2();
            log.setTranId(tranId);
            log.setRequestPayload(reversalRequest);
            log.setResponsePayload(responseBody);
            log.setProducerReqPayload(reversalRequest);
            log.setProducerRespPayload(responseBody);
            log.setReversalReqPayload("");
            log.setReversalRespPayload("");
            log.setTransferReqPayload("");
            log.setTransferRespPayload("");
            log.setTransactionDate(currentDate);
            LOGGER.info("log"+log);
            producerPayloadRepository.save(log);

        JSONObject jsonResponse = new JSONObject(responseBody);
        String status = jsonResponse.getString("status");
        String message2 = jsonResponse.getString("message");
        String name2 = jsonResponse.getString("name");
        String tranId2 = jsonResponse.getString("transactionid");

        LOGGER.info("responseBody"+responseBody);
        MakePaymentResponse makePaymentResponse = new MakePaymentResponse();
        OtherResponseDetails otherResponseDetails = new OtherResponseDetails();
        otherResponseDetails.setName(name2);
        otherResponseDetails.setTranId(tranId2);
        otherResponseDetails.setMessage(message2);

        if (responseBody != null) {
            if (status.equals("200") || status.equals("100")) {
                makePaymentResponse.setResponseCode("000");
                makePaymentResponse.setResponseMessage(message2);
                makePaymentResponse.setOtherResponseDetails(otherResponseDetails);
            }  else {
                LOGGER.info("httpStatusCode2<<<<<<<<<<"+status);
                makePaymentResponse.setResponseCode("900");
                makePaymentResponse.setResponseMessage("FAILED");
                makePaymentResponse.setOtherResponseDetails(otherResponseDetails);
            }
        } else {
            makePaymentResponse.setResponseCode("A94");
            makePaymentResponse.setResponseMessage("Error while processing vendor response payload");
        }
        return makePaymentResponse;
    }

    public MakePaymentResponse postCementPaymentNotificationforRepush(MakePaymentRequest request) throws JsonProcessingException, JSONException {
        MakePaymentResponse makePaymentResponse = new MakePaymentResponse();
        int checkIfTransactionExist = transactionCheckRepository.checkIfRecordExist(request.getTransactionId());
        if(checkIfTransactionExist > 0){
            String validateOrderRequest = "{\n"
                    + "           \"orderid\":\"" + request.getOtherRequestDetails().getOrderid() + "\"\n"
                    + "      }";
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + accessToken);
            String httpCallResponse = webClientUtil.httpPostRequest(validateOrderEndpoint,validateOrderRequest,"POST",useProxy,headers);
            JSONObject jsonRes = new JSONObject(httpCallResponse);
            String httpStatusCode = jsonRes.getString("status");
            LOGGER.info("validate order status code"+httpStatusCode);
            String httpCallResponse2;
            if (httpStatusCode.equals("200")) {
                String newPaymentEndpointRequest = "{\n"
                        + "           \"orderid\":\"" + request.getOtherRequestDetails().getOrderid() + "\",\n"
                        + "           \"debitaccountnumber\":\"" + request.getOtherRequestDetails().getDebitaccountnumber() + "\",\n"
                        + "           \"creditaccountnumber\":\"" + request.getOtherRequestDetails().getCreditaccountnumber() + "\",\n"
                        + "           \"amount\":\"" + request.getPrincipalAmount() + "\",\n"
                        + "           \"bank_transaction_id\":\"" + request.getTransactionId() + "\",\n"
                        + "           \"paymentdate\":\"" + dateUtil.getNowTimeAsNumbersWithSSS() + "\"\n"
                        + "      }";
                LOGGER.info("newPaymentEndpointRequest"+newPaymentEndpointRequest);
                httpCallResponse2 = webClientUtil.httpPostRequest(newPaymentFoodEndpoint,newPaymentEndpointRequest,"POST",useProxy,headers);
                Date currentDate = new Date();
                PayloadLog log = new PayloadLog();
                log.setLogCreateTime(currentDate);
                log.setLogUpdateTime(currentDate);
                log.setBiller("BUA_CEMENT");
                log.setCollectionType("NOTIFICATION_REPUSH");
                log.setAPIUrl(newPaymentCementEndpoint);
                String tranId = request.getTransactionId();
                log.setTranId(tranId);
                log.setRequestPayload(newPaymentEndpointRequest);
                log.setResponsePayload(httpCallResponse2);
                log.setProducerReqPayload(newPaymentCementEndpoint);
                log.setProducerRespPayload(httpCallResponse2);
                log.setReversalReqPayload("");
                log.setReversalRespPayload("");
                log.setTransferReqPayload("");
                log.setTransferRespPayload("");
                log.setTransactionDate(currentDate);
                LOGGER.info("log"+log);
                producerPayloadRepository.save(log);
            } else {
                String legacyEndpointRequest = "{\n"
                        + "           \"name\":\"" + request.getOtherRequestDetails().getName() + "\",\n"
                        + "           \"debitaccountnumber\":\"" + request.getOtherRequestDetails().getDebitaccountnumber() + "\",\n"
                        + "           \"creditaccountnumber\":\"" + request.getOtherRequestDetails().getCreditaccountnumber() + "\",\n"
                        + "           \"amount\":\"" + request.getPrincipalAmount() + "\",\n"
                        + "           \"bank_transaction_id\":\"" + request.getTransactionId() + "\",\n"
                        + "           \"paymentdate\":\"" + dateUtil.getNowTimeAsNumbersWithSSS() + "\"\n"
                        + "      }";
                LOGGER.info("legacyEndpointRequest"+legacyEndpointRequest);
                httpCallResponse2 = webClientUtil.httpPostRequest(legacyPaymentFoodEndpoint,legacyEndpointRequest,"POST",useProxy,headers);

                Date currentDate = new Date();
                PayloadLog log = new PayloadLog();
                log.setLogCreateTime(currentDate);
                log.setLogUpdateTime(currentDate);
                log.setBiller("BUA_CEMENT");
                log.setCollectionType("NOTIFICATION_REPUSH");
                log.setAPIUrl(legacyPaymentCementEndpoint);
                String tranId = request.getTransactionId();
                log.setTranId(tranId);
                log.setRequestPayload(legacyEndpointRequest);
                log.setResponsePayload(httpCallResponse2);
                log.setProducerReqPayload(legacyEndpointRequest);
                log.setProducerRespPayload(httpCallResponse2);
                log.setReversalReqPayload("");
                log.setReversalRespPayload("");
                log.setTransferReqPayload("");
                log.setTransferRespPayload("");
                log.setTransactionDate(currentDate);
                LOGGER.info("log"+log);
                producerPayloadRepository.save(log);
            }

            JSONObject jsonResponse = new JSONObject(httpCallResponse2);
            String status = jsonResponse.getString("status");
            String message2 = jsonResponse.getString("message");
            String name2 = jsonResponse.getString("name");
            String tranId2 = jsonResponse.getString("transactionid");

            OtherResponseDetails otherResponseDetails = new OtherResponseDetails();
            otherResponseDetails.setName(name2);
            otherResponseDetails.setTranId(tranId2);
            otherResponseDetails.setMessage(message2);

            if (httpCallResponse2 != null) {
                if (status.equals("200") || status.equals("100")) {
//                LOGGER.info("httpStatusCode2>>>>>>"+httpStatusCode2);
                    makePaymentResponse.setResponseCode("000");
                    makePaymentResponse.setResponseMessage(message2);
                    makePaymentResponse.setResponseMessage(name2);
                    makePaymentResponse.setResponseMessage(tranId2);
                    makePaymentResponse.setOtherResponseDetails(otherResponseDetails);
                } else{
                    makePaymentResponse.setResponseCode("900");
                    makePaymentResponse.setResponseMessage("FAILED");
                    makePaymentResponse.setOtherResponseDetails(otherResponseDetails);
                }
            } else {
                makePaymentResponse.setResponseCode("99");
                makePaymentResponse.setResponseMessage("Error while processing vendor response payload");
            }
        }else{
            makePaymentResponse.setResponseCode("99");
            makePaymentResponse.setResponseMessage("Transaction does not exist");
        }

        return makePaymentResponse;
    }

    public MakePaymentResponse postFoodPaymentNotificationforRepush(MakePaymentRequest request) throws JSONException, JsonProcessingException {
        MakePaymentResponse makePaymentResponse = new MakePaymentResponse();
        int checkIfTransactionExist = transactionCheckRepository.checkIfRecordExist(request.getTransactionId());
        if(checkIfTransactionExist > 0){
            String validateOrderRequest = "{\n"
                    + "           \"orderid\":\"" + request.getOtherRequestDetails().getOrderid() + "\"\n"
                    + "      }";
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + accessToken);
            String httpCallResponse = webClientUtil.httpPostRequest(validateOrderEndpoint,validateOrderRequest,"POST",useProxy,headers);
            JSONObject jsonRes = new JSONObject(httpCallResponse);
            String httpStatusCode = jsonRes.getString("status");
            LOGGER.info("validate order status code"+httpStatusCode);
            String httpCallResponse2;
            if (httpStatusCode.equals("200")) {
                String newPaymentEndpointRequest = "{\n"
                        + "           \"orderid\":\"" + request.getOtherRequestDetails().getOrderid() + "\",\n"
                        + "           \"debitaccountnumber\":\"" + request.getOtherRequestDetails().getDebitaccountnumber() + "\",\n"
                        + "           \"creditaccountnumber\":\"" + request.getOtherRequestDetails().getCreditaccountnumber() + "\",\n"
                        + "           \"amount\":\"" + request.getPrincipalAmount() + "\",\n"
                        + "           \"bank_transaction_id\":\"" + request.getTransactionId() + "\",\n"
                        + "           \"paymentdate\":\"" + dateUtil.getNowTimeAsNumbersWithSSS() + "\"\n"
                        + "      }";
                LOGGER.info("newPaymentEndpointRequest"+newPaymentEndpointRequest);
                httpCallResponse2 = webClientUtil.httpPostRequest(newPaymentFoodEndpoint,newPaymentEndpointRequest,"POST",useProxy,headers);
                Date currentDate = new Date();
                PayloadLog log = new PayloadLog();
                log.setLogCreateTime(currentDate);
                log.setLogUpdateTime(currentDate);
                log.setBiller("BUA_FOOD");
                log.setCollectionType("NOTIFICATION_REPUSH");
                log.setAPIUrl(newPaymentFoodEndpoint);
                String tranId = request.getTransactionId();
                log.setTranId(tranId);
                log.setRequestPayload(newPaymentEndpointRequest);
                log.setResponsePayload(httpCallResponse2);
                log.setProducerReqPayload(newPaymentEndpointRequest);
                log.setProducerRespPayload(httpCallResponse2);
                log.setReversalReqPayload("");
                log.setReversalRespPayload("");
                log.setTransferReqPayload("");
                log.setTransferRespPayload("");
                log.setTransactionDate(currentDate);
                LOGGER.info("log"+log);
                producerPayloadRepository.save(log);

            } else {
                System.out.println("legacy endpoint");
                String legacyEndpointRequest = "{\n"
                        + "           \"name\":\"" + request.getOtherRequestDetails().getName() + "\",\n"
                        + "           \"debitaccountnumber\":\"" + request.getOtherRequestDetails().getDebitaccountnumber() + "\",\n"
                        + "           \"creditaccountnumber\":\"" + request.getOtherRequestDetails().getCreditaccountnumber() + "\",\n"
                        + "           \"amount\":\"" + request.getPrincipalAmount() + "\",\n"
                        + "           \"bank_transaction_id\":\"" + request.getTransactionId() + "\",\n"
                        + "           \"paymentdate\":\"" + dateUtil.getNowTimeAsNumbersWithSSS() + "\"\n"
                        + "      }";
                LOGGER.info("legacyEndpointRequest"+legacyEndpointRequest);
                httpCallResponse2 = webClientUtil.httpPostRequest(legacyPaymentFoodEndpoint,legacyEndpointRequest,"POST",useProxy,headers);
                Date currentDate = new Date();
                PayloadLog log = new PayloadLog();
                log.setLogCreateTime(currentDate);
                log.setLogUpdateTime(currentDate);
                log.setBiller("BUA_FOOD");
                log.setCollectionType("NOTIFICATION_REPUSH");
                log.setAPIUrl(legacyPaymentFoodEndpoint);
                String tranId = request.getTransactionId();
                log.setTranId(tranId);
                log.setRequestPayload(legacyEndpointRequest);
                log.setResponsePayload(httpCallResponse2);
                log.setProducerReqPayload(legacyEndpointRequest);
                log.setProducerRespPayload(httpCallResponse2);
                log.setReversalReqPayload("");
                log.setReversalRespPayload("");
                log.setTransferReqPayload("");
                log.setTransferRespPayload("");
                log.setTransactionDate(currentDate);
                LOGGER.info("log"+log);
                producerPayloadRepository.save(log);
            }

            JSONObject jsonResponse = new JSONObject(httpCallResponse2);
            String status = jsonResponse.getString("status");
            String message2 = jsonResponse.getString("message");
            String name2 = jsonResponse.getString("name");
            String tranId2 = jsonResponse.getString("transactionid");

            LOGGER.info("responseBody"+httpCallResponse2);
//            MakePaymentResponse makePaymentResponse = new MakePaymentResponse();
            OtherResponseDetails otherResponseDetails = new OtherResponseDetails();
            otherResponseDetails.setName(name2);
            otherResponseDetails.setTranId(tranId2);
            otherResponseDetails.setMessage(message2);

            if (httpCallResponse2 != null) {
                if (status.equals("200") || status.equals("100")) {
                    makePaymentResponse.setResponseCode("000");
                    makePaymentResponse.setResponseMessage(message2);
                    makePaymentResponse.setResponseMessage(name2);
                    makePaymentResponse.setResponseMessage(tranId2);
                    makePaymentResponse.setOtherResponseDetails(otherResponseDetails);
                }  else {
                    makePaymentResponse.setResponseCode("900");
                    makePaymentResponse.setResponseMessage("FAILED");
                    makePaymentResponse.setOtherResponseDetails(otherResponseDetails);
                }
            } else {
                makePaymentResponse.setResponseCode("99");
                makePaymentResponse.setResponseMessage("Error while processing vendor response payload");
            }
        }else{
            makePaymentResponse.setResponseCode("99");
            makePaymentResponse.setResponseMessage("Transaction Id does not exist");
        }
        return makePaymentResponse;
    }

}
