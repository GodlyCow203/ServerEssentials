package com.serveressentials.api.economy;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;


public final class EconomyResponse {
    private final double amount;
    private final double balance;
    private final boolean success;
    private final @Nullable String errorMessage;

    public EconomyResponse(double amount, double balance, boolean success, @Nullable String errorMessage) {
        this.amount = amount;
        this.balance = balance;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public double getAmount() { return amount; }
    public double getBalance() { return balance; }
    public boolean isSuccess() { return success; }
    public @Nullable String getErrorMessage() { return errorMessage; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EconomyResponse)) return false;
        EconomyResponse that = (EconomyResponse) o;
        return Double.compare(that.amount, amount) == 0 &&
                Double.compare(that.balance, balance) == 0 &&
                success == that.success &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, balance, success, errorMessage);
    }

    @Override
    public String toString() {
        return success ?
                String.format("EconomyResponse{amount=%.2f, balance=%.2f, success=true}", amount, balance) :
                String.format("EconomyResponse{amount=%.2f, success=false, error='%s'}", amount, errorMessage);
    }
}