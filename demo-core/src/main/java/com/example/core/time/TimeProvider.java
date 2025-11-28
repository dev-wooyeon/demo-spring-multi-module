package com.example.core.time;

import java.time.Instant;

public interface TimeProvider {
    Instant now();
}
