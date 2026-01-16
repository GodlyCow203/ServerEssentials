package com.serveressentials.api.economy;


public final class EconomyPaymentSettings {
    private final boolean paymentsDisabled;
    private final boolean payConfirmDisabled;

    public EconomyPaymentSettings(boolean paymentsDisabled, boolean payConfirmDisabled) {
        this.paymentsDisabled = paymentsDisabled;
        this.payConfirmDisabled = payConfirmDisabled;
    }

    public boolean isPaymentsDisabled() { return paymentsDisabled; }
    public boolean isPayConfirmDisabled() { return payConfirmDisabled; }

    @Override
    public String toString() {
        return "EconomyPaymentSettings{" +
                "paymentsDisabled=" + paymentsDisabled +
                ", payConfirmDisabled=" + payConfirmDisabled +
                '}';
    }
}