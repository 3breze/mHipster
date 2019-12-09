package com.oul.mHipster.service;

public class InsuranceStrategyLow extends InsuranceStrategy {
    @Override
    double getWeight() {
        return 0;
    }

    @Override
    int getConstant() {
        return 0;
    }

    @Override
    int getAdjustment() {
        return 0;
    }
}
