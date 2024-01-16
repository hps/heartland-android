package com.heartlandpaymentsystems.library.terminals.transactions;

import com.heartlandpaymentsystems.library.terminals.IDevice;
import com.tsys.payments.library.domain.AutoSubstantiation;
import com.tsys.payments.library.domain.TransactionRequest;
import com.tsys.payments.library.utils.LibraryConfigHelper;
import java.math.BigDecimal;
import java.util.HashMap;

public abstract class BaseBuilder {
    private IDevice device;
    private boolean allowDuplicates;

    private String merchantVerificationValue;
    private boolean realTimeSubstantiation;
    private HashMap<String, BigDecimal> amounts;

    public BaseBuilder(IDevice device) {
        this.device = device;
    }

    public void execute() throws Exception {
        if (device == null) {
            throw new Exception("Missing configured device");
        }

        device.doTransaction(buildRequest());
    }

    protected TransactionRequest buildRequest() {
        TransactionRequest request = new TransactionRequest();

        if (amounts != null && canRequestHaveAutoSubstantiation()) {
            AutoSubstantiation autoSubstantiation = new AutoSubstantiation();
            autoSubstantiation.setMerchantVerificationValue(merchantVerificationValue);
            autoSubstantiation.setRealTimeSubstantiation(realTimeSubstantiation);
            autoSubstantiation.setClinicSubTotal(getClinicSubTotal());
            autoSubstantiation.setDentalSubTotal(getDentalSubTotal());
            autoSubstantiation.setVisionSubTotal(getVisionSubTotal());
            autoSubstantiation.setPrescriptionSubTotal(getPrescriptionSubTotal());
            request.setAutoSubstantiation(autoSubstantiation);
        }

        request.setAllowDuplicates(allowDuplicates);

        return request;
    }

    private boolean canRequestHaveAutoSubstantiation() {
        return (this instanceof CreditAuthBuilder || this instanceof CreditSaleBuilder);
    }

    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }

    public void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }

    // AMOUNTS
    public BigDecimal getClinicSubTotal() {
        if (amounts == null) {
            return null;
        }
        return amounts.get("SUBTOTAL_CLINIC_OR_OTHER_AMT");
    }
    public void setClinicSubTotal(BigDecimal value) {
        if (amounts == null) {
            initializeAmounts();
        }
        //convert value to pennies
        value = value.divide(BigDecimal.valueOf(100));
        amounts.put("SUBTOTAL_CLINIC_OR_OTHER_AMT", value);
        amounts.put("TOTAL_HEALTHCARE_AMT", amounts.get("TOTAL_HEALTHCARE_AMT").add(value));
    }
    public BigDecimal getDentalSubTotal() {
        if (amounts == null) {
            return null;
        }
        return amounts.get("SUBTOTAL_DENTAL_AMT");
    }
    public void setDentalSubTotal(BigDecimal value) {
        if (amounts == null) {
            initializeAmounts();
        }
        //convert value to pennies
        value = value.divide(BigDecimal.valueOf(100));
        amounts.put("SUBTOTAL_DENTAL_AMT", value);
        amounts.put("TOTAL_HEALTHCARE_AMT", amounts.get("TOTAL_HEALTHCARE_AMT").add(value));
    }
    public BigDecimal getPrescriptionSubTotal() {
        if (amounts == null) {
            return null;
        }
        return amounts.get("SUBTOTAL_PRESCRIPTION_AMT");
    }
    public void setPrescriptionSubTotal(BigDecimal value) {
        if (amounts == null) {
            initializeAmounts();
        }
        //convert value to pennies
        value = value.divide(BigDecimal.valueOf(100));
        amounts.put("SUBTOTAL_PRESCRIPTION_AMT", value);
        amounts.put("TOTAL_HEALTHCARE_AMT", amounts.get("TOTAL_HEALTHCARE_AMT").add(value));
    }
    public BigDecimal getTotalHealthcareAmount() {
        if (amounts == null) {
            return null;
        }
        return amounts.get("TOTAL_HEALTHCARE_AMT");
    }
    public BigDecimal getVisionSubTotal() {
        if (amounts == null) {
            return null;
        }
        return amounts.get("SUBTOTAL_VISION__OPTICAL_AMT");
    }
    public void setVisionSubTotal(BigDecimal value) {
        if (amounts == null) {
            initializeAmounts();
        }
        //convert value to pennies
        value = value.divide(BigDecimal.valueOf(100));
        amounts.put("SUBTOTAL_VISION__OPTICAL_AMT", value);
        amounts.put("TOTAL_HEALTHCARE_AMT", amounts.get("TOTAL_HEALTHCARE_AMT").add(value));
    }

    private void initializeAmounts() {
        amounts = new HashMap<String, BigDecimal>();
        amounts.put("TOTAL_HEALTHCARE_AMT", new BigDecimal("0"));
        amounts.put("SUBTOTAL_PRESCRIPTION_AMT", new BigDecimal("0"));
        amounts.put("SUBTOTAL_VISION__OPTICAL_AMT", new BigDecimal("0"));
        amounts.put("SUBTOTAL_CLINIC_OR_OTHER_AMT", new BigDecimal("0"));
        amounts.put("SUBTOTAL_DENTAL_AMT", new BigDecimal("0"));
    }
}
