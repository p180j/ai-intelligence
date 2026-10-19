package com.aiintelligence.config;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class TotpVerifier {
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final long TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final int ALLOWED_TIME_DRIFT_STEPS = 1;

    private final byte[] secret;
    private final Clock clock;
    private final AtomicLong lastAcceptedStep = new AtomicLong(-1);

    TotpVerifier(String encodedSecret, Clock clock) {
        this.secret = decodeSecret(encodedSecret);
        this.clock = clock;
        if (secret.length < 20) {
            throw new IllegalStateException("ADMIN_TOTP_SECRET must contain at least 160 bits of Base32 data");
        }
    }

    boolean verify(String code) {
        if (code == null || !code.matches("\\d{" + CODE_DIGITS + "}")) {
            return false;
        }
        long currentStep = clock.instant().getEpochSecond() / TIME_STEP_SECONDS;
        for (int offset = -ALLOWED_TIME_DRIFT_STEPS; offset <= ALLOWED_TIME_DRIFT_STEPS; offset++) {
            long candidateStep = currentStep + offset;
            if (constantTimeEquals(code, codeAt(candidateStep)) && markAsUsed(candidateStep)) {
                return true;
            }
        }
        return false;
    }

    String currentCode() {
        return codeAt(clock.instant().getEpochSecond() / TIME_STEP_SECONDS);
    }

    private String codeAt(long timeStep) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(timeStep).array());
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            int value = binary % 1_000_000;
            return String.format(Locale.ROOT, "%0" + CODE_DIGITS + "d", value);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("TOTP verification is unavailable", exception);
        }
    }

    private boolean markAsUsed(long timeStep) {
        long previous = lastAcceptedStep.get();
        while (timeStep > previous) {
            if (lastAcceptedStep.compareAndSet(previous, timeStep)) {
                return true;
            }
            previous = lastAcceptedStep.get();
        }
        return false;
    }

    private boolean constantTimeEquals(String actual, String expected) {
        return MessageDigest.isEqual(actual.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                expected.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }

    private static byte[] decodeSecret(String encodedSecret) {
        if (encodedSecret == null || encodedSecret.trim().isEmpty()) {
            throw new IllegalStateException("ADMIN_TOTP_SECRET is not configured");
        }
        String normalized = encodedSecret.replace(" ", "").replace("-", "").replace("=", "")
                .toUpperCase(Locale.ROOT);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int buffer = 0;
        int bitsInBuffer = 0;
        for (int index = 0; index < normalized.length(); index++) {
            int value = BASE32_ALPHABET.indexOf(normalized.charAt(index));
            if (value < 0) {
                throw new IllegalStateException("ADMIN_TOTP_SECRET must be valid Base32 data");
            }
            buffer = (buffer << 5) | value;
            bitsInBuffer += 5;
            if (bitsInBuffer >= 8) {
                bitsInBuffer -= 8;
                output.write((buffer >> bitsInBuffer) & 0xff);
                buffer &= (1 << bitsInBuffer) - 1;
            }
        }
        return output.toByteArray();
    }
}
