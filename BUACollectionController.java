package com.stanbic.bua.controller;


import com.stanbic.bua.dto.*;
import com.stanbic.bua.service.BUACollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BUACollectionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BUACollectionController.class);
    @Autowired
    BUACollectionService buaCollectionService;

    @RequestMapping(path = "validatecementorder", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public ValidateCementOrderResponse validateCementOrder(@RequestBody ValidateCementOrderRequest request) throws Exception {
        return buaCollectionService.validateCementOrder(request);
    }
    @RequestMapping(path = "verifycementpayment", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public VerifyPaymentResponse verifyCementPayment(@RequestBody VerifyPaymentRequest request) throws Exception {
        return buaCollectionService.verifyCementPayment(request);
    }
    @RequestMapping(path = "cementpaymentnotification", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public MakePaymentResponse postPaymentNotification(@RequestBody MakePaymentRequest request) throws Exception {
        return buaCollectionService.postCementPaymentNotification(request);
    }
    @RequestMapping(path = "validatefoodorder", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public ValidateCementOrderResponse validateFoodOrder(@RequestBody ValidateCementOrderRequest request) throws Exception {
        return buaCollectionService.validateFoodOrder(request);
    }
    @RequestMapping(path = "verifyfoodpayment", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public VerifyPaymentResponse verifyFoodPayment(@RequestBody VerifyPaymentRequest request) throws Exception {
        return buaCollectionService.verifyFoodPayment(request);
    }
    @RequestMapping(path = "foodpaymentnotification", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public MakePaymentResponse postFoodPaymentNotification(@RequestBody MakePaymentRequest request) throws Exception {
        return buaCollectionService.postFoodPaymentNotification(request);
    }

    @RequestMapping(path = "paymentreversalnotification", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public MakePaymentResponse paymentReversalNotification(@RequestBody MakePaymentRequest request) throws Exception {
        return buaCollectionService.reversalPaymentNotification(request);
    }

    @RequestMapping(path = "paymentfoodreversalnotifications", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public MakePaymentResponse paymentFoodReversalNotification(@RequestBody MakePaymentRequest request) throws Exception {
        return buaCollectionService.reversalPaymentNotification(request);
    }

    @RequestMapping(path = "cementpaymentnotificationrepush", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public MakePaymentResponse postPaymentNotificationRepush(@RequestBody MakePaymentRequest request) throws Exception {
        return buaCollectionService.postCementPaymentNotificationforRepush(request);
    }

    @RequestMapping(path = "foodpaymentnotificationrepush", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    public MakePaymentResponse postFoodPaymentNotificationRepush(@RequestBody MakePaymentRequest request) throws Exception {
        return buaCollectionService.postFoodPaymentNotificationforRepush(request);
    }


}
