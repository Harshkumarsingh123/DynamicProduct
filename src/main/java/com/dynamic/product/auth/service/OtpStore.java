package com.dynamic.product.auth.service;

import com.dynamic.product.auth.entity.OtpData;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Component
public class OtpStore {

    private final ConcurrentHashMap<String, OtpData> otpMap =
            new ConcurrentHashMap<>();

    public OtpData get(String email) {
        return otpMap.get(email);
    }

    public void save(String email, OtpData otpData) {
        otpMap.put(email, otpData);
    }

    public boolean remove(String email, OtpData otpData) {
        return otpMap.remove(email, otpData);
    }

    public OtpData compute(
            String email,
            BiFunction<String, OtpData, OtpData> function) {

        return otpMap.compute(email, function);
    }
}
