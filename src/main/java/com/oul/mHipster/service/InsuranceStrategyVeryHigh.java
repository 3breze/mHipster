package com.oul.mHipster.service;

public class InsuranceStrategyVeryHigh extends InsuranceStrategy {

    public InsuranceStrategyVeryHigh() {
    }

    @Override
    double getWeight() {
        return 0.02;
    }

    @Override
    int getConstant() {
        return 105600;
    }

    @Override
    int getAdjustment() {
        return 60000;
    }
}