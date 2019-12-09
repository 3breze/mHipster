package com.oul.mHipster.service;

public abstract class InsuranceStrategy {
    double calculate(double income) {
        return (income - getAdjustment()) * getWeight() + getConstant();
    }

    abstract double getWeight();

    abstract int getConstant();

    abstract int getAdjustment();
}
