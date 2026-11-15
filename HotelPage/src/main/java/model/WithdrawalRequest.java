package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WithdrawalRequest {

    private int withdrawalId;
    private int customerId;

    private BigDecimal amount;

    private String bankName;
    private String accountNumber;
    private String accountHolder;

    private String status;
    private String rejectReason;

    private Integer reviewedBy;
    private LocalDateTime reviewedAt;

    private Integer paidBy;
    private LocalDateTime paidAt;
    private String paymentReference;

    private LocalDateTime createdAt;

    public WithdrawalRequest() {
    }

    public WithdrawalRequest(
            int withdrawalId,
            int customerId,
            BigDecimal amount,
            String bankName,
            String accountNumber,
            String accountHolder,
            String status,
            String rejectReason,
            Integer reviewedBy,
            LocalDateTime reviewedAt,
            Integer paidBy,
            LocalDateTime paidAt,
            String paymentReference,
            LocalDateTime createdAt) {

        this.withdrawalId = withdrawalId;
        this.customerId = customerId;
        this.amount = amount;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.status = status;
        this.rejectReason = rejectReason;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.paidBy = paidBy;
        this.paidAt = paidAt;
        this.paymentReference = paymentReference;
        this.createdAt = createdAt;
    }

    public int getWithdrawalId() {
        return withdrawalId;
    }

    public void setWithdrawalId(int withdrawalId) {
        this.withdrawalId = withdrawalId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Integer getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Integer reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Integer getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(Integer paidBy) {
        this.paidBy = paidBy;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
