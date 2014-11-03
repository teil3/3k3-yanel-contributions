/*
 * Copyright 2010 Litwan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0 
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.litwan.yanel.impl.resources.ipnlistener;

import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.impl.resources.BasicXMLResource;
import org.wyona.yanel.impl.resources.usecase.ExecutableUsecaseResource;
import org.wyona.yanel.impl.resources.usecase.UsecaseException;
import org.wyona.yanel.impl.resources.usecase.UsecaseResource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;


import org.apache.log4j.Logger;


/**
 * @author simon litwan
 * a resource type implementing an IPN listener as described here: https://cms.paypal.com/us/cgi-bin/?&cmd=_render-content&content_ID=developer/e_howto_admin_IPNIntro
 * code is based on following sample code: https://cms.paypal.com/cms_content/US/en_US/files/developer/IPN_JAVA_JSP.txt 
 */
public class IPNListener extends UsecaseResource {

    private static Logger log = Logger.getLogger(IPNListener.class);

    // paypal request fields
    //
    // Information about you:
    protected String paypalMessage_receiver_email;
    protected String paypalMessage_receiver_id;
    protected String paypalMessage_residence_country;
    // Information about the transaction:
    protected String paypalMessage_test_ipn;
    protected String paypalMessage_transaction_subject;
    protected String paypalMessage_txn_id;
    protected String paypalMessage_txn_type;
    // Information about your buyer:
    protected String paypalMessage_payer_email;
    protected String paypalMessage_payer_id;
    protected String paypalMessage_payer_status;
    protected String paypalMessage_first_name;
    protected String paypalMessage_last_name;
    protected String paypalMessage_address_city;
    protected String paypalMessage_address_country;
    protected String paypalMessage_address_country_code;
    protected String paypalMessage_address_name;
    protected String paypalMessage_address_state;
    protected String paypalMessage_address_status;
    protected String paypalMessage_address_street;
    protected String paypalMessage_address_zip;
    // Information about the payment:
    protected String paypalMessage_custom;
    protected String paypalMessage_handling_amount;
    protected String paypalMessage_item_name;
    protected String paypalMessage_item_number;
    protected String paypalMessage_mc_currency;
    protected String paypalMessage_mc_fee;
    protected String paypalMessage_mc_gross;
    protected String paypalMessage_mc_gross_1;
    protected String paypalMessage_payment_date;
    protected String paypalMessage_payment_fee;
    protected String paypalMessage_payment_gross;
    protected String paypalMessage_payment_status;
    protected String paypalMessage_payment_type;
    protected String paypalMessage_protection_eligibility;
    protected String paypalMessage_quantity;
    protected String paypalMessage_shipping;
    protected String paypalMessage_tax;
    // Other information about the transaction:
    protected String paypalMessage_notify_version;
    protected String paypalMessage_charset;
    protected String paypalMessage_verify_sign;
    protected String paypalMessage_invoice;
    
    
    protected void init() {

        //String paymentStatus = getParameterAsString("payment_status");
        paypalMessage_receiver_email = getParameterAsString("receiver_email");
        paypalMessage_receiver_id = getParameterAsString("receiver_id");
        paypalMessage_residence_country = getParameterAsString("residence_country");
        paypalMessage_test_ipn = getParameterAsString("test_ipn");
        paypalMessage_transaction_subject = getParameterAsString("transaction_subject");
        paypalMessage_txn_id = getParameterAsString("txn_id");
        paypalMessage_txn_type = getParameterAsString("txn_type");
        paypalMessage_payer_email = getParameterAsString("payer_email");
        paypalMessage_payer_id = getParameterAsString("payer_id");
        paypalMessage_payer_status = getParameterAsString("payer_status");
        paypalMessage_first_name = getParameterAsString("first_name");
        paypalMessage_last_name = getParameterAsString("last_name");
        paypalMessage_address_city = getParameterAsString("address_city");
        paypalMessage_address_country = getParameterAsString("address_country");
        paypalMessage_address_country_code = getParameterAsString("address_country_code");
        paypalMessage_address_name = getParameterAsString("address_name");
        paypalMessage_address_state = getParameterAsString("address_state");
        paypalMessage_address_status = getParameterAsString("address_status");
        paypalMessage_address_street = getParameterAsString("address_street");
        paypalMessage_address_zip = getParameterAsString("address_zip");
        paypalMessage_custom = getParameterAsString("custom");
        paypalMessage_handling_amount = getParameterAsString("handling_amount");
        paypalMessage_item_name = getParameterAsString("item_name");
        paypalMessage_item_number = getParameterAsString("item_number");
        paypalMessage_mc_currency = getParameterAsString("mc_currency");
        paypalMessage_mc_fee = getParameterAsString("mc_fee");
        paypalMessage_mc_gross = getParameterAsString("mc_gross");
        paypalMessage_mc_gross_1 = getParameterAsString("mc_gross_1");
        paypalMessage_payment_date = getParameterAsString("payment_date");
        paypalMessage_payment_fee = getParameterAsString("payment_fee");
        paypalMessage_payment_gross = getParameterAsString("payment_gross");
        paypalMessage_payment_status = getParameterAsString("payment_status");
        paypalMessage_payment_type = getParameterAsString("payment_type");
        paypalMessage_protection_eligibility = getParameterAsString("protection_eligibility");
        paypalMessage_quantity = getParameterAsString("quantity");
        paypalMessage_shipping = getParameterAsString("shipping");
        paypalMessage_tax = getParameterAsString("tax");
        paypalMessage_notify_version = getParameterAsString("notify_version");
        paypalMessage_charset = getParameterAsString("charset");
        paypalMessage_verify_sign = getParameterAsString("verify_sign");
        paypalMessage_invoice = getParameterAsString("invoice");
        
        if (checkPreconditions(verifyRequest())) {
            execute();
            postExecute();
        }        
    }
    
    /**
     * check if the transaction should be executed or not.
     * override in subclass if an other test is wished 
     * 
     * @param String response. 
     * @return boolean. true if the response matches the expectation otherwise false 
     */
    protected boolean checkPreconditions(String response) {
        if (response == null) {
            return false;
        }
        
        try {
            double paymentAmount = 0;
            if (paypalMessage_mc_gross != null) {
                paymentAmount = Double.parseDouble(paypalMessage_mc_gross);
            }
            if (paypalMessage_mc_gross_1 != null) {
                paymentAmount = Double.parseDouble(paypalMessage_mc_gross_1);
            }
            String expectedRecieverEmail = getResourceConfigProperty("expected-reciever-email");
            String expectedPaymentCurrency = getResourceConfigProperty("expected-payment-currency");
            
            //check notification validation
            if(response.equals("VERIFIED")) {
                // check that paymentStatus=Completed
                if (!paypalMessage_payment_status.equals("Completed")) {
                    if (log.isInfoEnabled()) log.info("IPN Status: not Completed: " + paypalMessage_payment_status);
                    return false;
                }
                // check that txnId has not been previously processed
                if (transactionIdExists(paypalMessage_txn_id)) {
                    log.error("IPN Status: txnId ("+ paypalMessage_txn_id +") already exists.");
                    return false;
                }
                // check that receiverEmail is your Primary PayPal email
                if (!expectedRecieverEmail.equals(paypalMessage_receiver_email)) {
                    log.error("IPN Status: our paypal email is not the recivers email. expected: " + expectedRecieverEmail + ", sent by paypal" + paypalMessage_receiver_email);
                    return false;
                }
                // check that paymentAmount/paymentCurrency are correct
                if (getExpectedPaymentAmount() != paymentAmount) {
                    log.error("IPN Status: the order total (" + getExpectedPaymentAmount() + ") is not equals the payment amount (" + paymentAmount + ").");
                    return false;
                }
                if (!paypalMessage_mc_currency.equals(expectedPaymentCurrency)) {
                    log.error("IPN Status: the currency (" + paypalMessage_mc_currency + ") is not the expected "+ expectedPaymentCurrency +".");
                    return false;
                }
                //if everthing is ok
                if (log.isInfoEnabled()) log.info("seems to be alright, executig and setting transaction id as proceded");
                return true;
            }
            else if(response.equals("INVALID")) {
                log.error("PayPal: INVALID ipn request. response was: " + response);
                return false;
            }
            else {
                log.error("PayPal: no valid response for ipn verification. response was: " + response);
                return false;
            }      
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * check if the transaction id already exists or not.
     * implement in subclass! 
     * 
     * @param String txnId. the transaction id to check.
     * @return boolean. true if transaction already exits.
     */
    protected boolean transactionIdExists(String txnId) {
        log.error("not implemented yet. please implement in sublcass");
        return false;
    }

    /**
     * method to receive the expected payment amount.
     * implement in subclass!
     *  
     * @return double. expected paymend amount
     */
    protected double getExpectedPaymentAmount() {
        log.error("not implemented yet. please implement in sublcass");
        return 0.0;
    }
    
    /**
     * 
     */
    protected void execute() {
        log.error("not implemented yet. please implement in sublcass");
    }    
    
    /**
     * this method allows for example to set the transaction id to exist
     * implement in subclass!
     * 
     */
    protected void postExecute() {
        log.error("not implemented yet. please implement in sublcass");
    }
    
//    @Override
//    public InputStream getContentXML(String viewID) throws Exception {
//        init();
//        return super.getContentXML(viewID);
//    }

 
    
    /**
     * verify the IPN request.
     * we will send the complete unaltered message back to PayPal; 
     * the message must contain the same fields in the same order and be encoded in the same way as the original message
     * 
     * @return String with the content of the response: "VERIFIED", "INVALID" or an error message
     */
    protected String verifyRequest() {
        try {
            HttpServletRequest request = getEnvironment().getRequest();
            String verfyingQueryString = "cmd=_notify-validate";
            String verificationResponse = "";
            String paypalNotifyValidateUrl = getResourceConfigProperty("paypal-notify-validate-url");
            
            Enumeration en = request.getParameterNames();
            while(en.hasMoreElements()){
                String paramName = (String)en.nextElement();
                String paramValue = request.getParameter(paramName);
                verfyingQueryString = verfyingQueryString + "&" + paramName + "=" + URLEncoder.encode(paramValue, "UTF-8");
            }
            if(log.isDebugEnabled())log.debug("Send Back Query String: " + verfyingQueryString);
            
            // post back to PayPal system to validate
            // NOTE: change http: to https: in the following URL to verify using SSL (for increased security).
            // using HTTPS requires either Java 1.4 or greater, or Java Secure Socket Extension (JSSE)
            // and configured for older versions.
            URL url = new URL(paypalNotifyValidateUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            PrintWriter printWriter = new PrintWriter(urlConnection.getOutputStream());
            printWriter.println(verfyingQueryString);
            printWriter.close();
            if (log.isDebugEnabled()) log.debug("sended querystring back to paypal-notify-validate-url: " + paypalNotifyValidateUrl);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            if(log.isDebugEnabled()){
                // this reads and logs all lines of the response.
                // if the response has more than one line there is something wrong and hopefully all the lines will give you an hint 
                String line = null;
                while ((line = in.readLine()) != null) {
                    verificationResponse += line;
                }
            } else {
                // normally paypal send just one line saying 'VERIFIED' or 'INVALID' let's read just one line
                verificationResponse = in.readLine();
            }
            in.close();
            if (log.isDebugEnabled()) log.debug("IPN response: " + verificationResponse);
            return verificationResponse;
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * allows configuring the document path
     * @see org.wyona.yanel.core.Resource#getPath()
     */
    public String getPath() {
        try {
            String yanelPath = getResourceConfigProperty("yanel-path");
            if (yanelPath != null && yanelPath.length() > 0) {
                return yanelPath;
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        return super.getPath();
    }
}
