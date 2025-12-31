package com.serveressentials.api.tpa;

import java.util.Objects;

public final class TPACosts {
    private final double costTpa;
    private final double costTpahere;
    private final double costTpall;
    private final boolean refundOnDeny;
    private final boolean refundOnExpire;

    public TPACosts(double costTpa, double costTpahere, double costTpall,
                    boolean refundOnDeny, boolean refundOnExpire) {
        this.costTpa = costTpa;
        this.costTpahere = costTpahere;
        this.costTpall = costTpall;
        this.refundOnDeny = refundOnDeny;
        this.refundOnExpire = refundOnExpire;
    }

    public double getCostTpa() {
        return costTpa;
    }

    public double getCostTpahere() {
        return costTpahere;
    }

    public double getCostTpall() {
        return costTpall;
    }

    public boolean isRefundOnDeny() {
        return refundOnDeny;
    }

    public boolean isRefundOnExpire() {
        return refundOnExpire;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TPACosts that = (TPACosts) o;
        return Double.compare(that.costTpa, costTpa) == 0 && Double.compare(that.costTpahere, costTpahere) == 0 &&
                Double.compare(that.costTpall, costTpall) == 0 && refundOnDeny == that.refundOnDeny &&
                refundOnExpire == that.refundOnExpire;
    }

    @Override
    public int hashCode() {
        return Objects.hash(costTpa, costTpahere, costTpall, refundOnDeny, refundOnExpire);
    }

    @Override
    public String toString() {
        return "TPACosts{" + "costTpa=" + costTpa + ", costTpahere=" + costTpahere +
                ", costTpall=" + costTpall + ", refundOnDeny=" + refundOnDeny +
                ", refundOnExpire=" + refundOnExpire + '}';
    }
}