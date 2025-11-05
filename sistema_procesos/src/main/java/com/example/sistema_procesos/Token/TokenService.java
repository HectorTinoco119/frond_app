package com.example.sistema_procesos.Token;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private final Set<String> invalidTokens = ConcurrentHashMap.newKeySet();
    private final Map<String, Set<String>> activeTokensPerUser = new ConcurrentHashMap<>();

    public void registerToken(String username, String token) {
        activeTokensPerUser.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(token);
    }

    public void invalidateToken(String token) {
        invalidTokens.add(token);
        activeTokensPerUser.values().forEach(set -> set.remove(token));
    }

    public boolean isActiveForUser(String username, String token) {
        if (token == null) return false;
        if (invalidTokens.contains(token)) return false;
        return activeTokensPerUser.getOrDefault(username, Set.of()).contains(token);
    }
}
