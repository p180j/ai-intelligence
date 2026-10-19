package com.aiintelligence.config;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TotpVerifierTest {
    private static final String RFC_TEST_SECRET = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

    @Test
    void verifiesStandardTotpAndRejectsReplay() {
        TotpVerifier verifier = new TotpVerifier(RFC_TEST_SECRET,
                Clock.fixed(Instant.ofEpochSecond(59), ZoneOffset.UTC));

        assertTrue(verifier.verify("287082"));
        assertFalse(verifier.verify("287082"));
        assertFalse(verifier.verify("000000"));
    }

    @Test
    void rejectsMissingOrWeakSecrets() {
        assertThrows(IllegalStateException.class, () -> new TotpVerifier("", Clock.systemUTC()));
        assertThrows(IllegalStateException.class, () -> new TotpVerifier("JBSWY3DPEHPK3PXP", Clock.systemUTC()));
        assertThrows(IllegalStateException.class, () -> new TotpVerifier("not-base32!", Clock.systemUTC()));
    }
}
