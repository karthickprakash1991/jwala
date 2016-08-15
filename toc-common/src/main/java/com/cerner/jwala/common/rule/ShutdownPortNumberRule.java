package com.cerner.jwala.common.rule;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;

public class ShutdownPortNumberRule extends PortNumberRule {

    public ShutdownPortNumberRule(final Integer thePort, final AemFaultType errorCode) {
        super(thePort,
             errorCode,
             false);
    }

    @Override
    public boolean isValid() {
        if(super.getPort() != null && super.getPort() == -1) {
            return true;
        }
        return super.isValid();
    }
}