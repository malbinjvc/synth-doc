package com.synthdoc.clients;

public interface ClaudeClient {

    String generate(String prompt, int maxTokens);

    boolean isAvailable();
}
