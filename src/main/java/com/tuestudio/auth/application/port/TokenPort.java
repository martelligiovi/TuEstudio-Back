package com.tuestudio.auth.application.port;

import com.tuestudio.auth.domain.User;

public interface TokenPort {
    String generate(User user);
}
