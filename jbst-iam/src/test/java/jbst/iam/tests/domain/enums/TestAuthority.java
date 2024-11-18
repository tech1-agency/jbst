package jbst.iam.tests.domain.enums;

import jbst.foundation.domain.base.AbstractAuthority;

public enum TestAuthority implements AbstractAuthority {
    USER("user"),
    ADMIN("admin"),
    TESTS_INVITATIONS_READ("invitations:read"),
    TESTS_INVITATIONS_WRITE("invitations:write");

    private final String value;

    TestAuthority(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
