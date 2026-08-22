package com.irtech.brokerinfrastructure.redis.models.session;

import com.irtech.brokerinfrastructure.config.redis.AbstractRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LoginRedisService extends AbstractRedisService {

    private final RedisTemplate<String, LoginRedisModel> redis;

    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    public void save(LoginRedisModel login) {

        String key = LoginRedisKeys.loginInfoKeyBuilder(login.loginName());

        set(
                redis,
                key,
                login,
                SESSION_TTL
        );
    }

    public LoginRedisModel find(String loginName) {

        String key = LoginRedisKeys.loginInfoKeyBuilder(loginName);

        return get(
                redis,
                key
        );
    }

    public List<LoginRedisModel> findAll() {

        Set<String> keys = redis.keys(LoginRedisKeys.loginInfoPattern());

        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        List<LoginRedisModel> sessions = new ArrayList<>();

        for (String key : keys) {
            LoginRedisModel model =
                    get(
                            redis,
                            key
                    );

            if (model != null) {
                sessions.add(model);
            }
        }
        return sessions;
    }

    public void delete(String loginName) {

        String key = LoginRedisKeys.loginInfoKeyBuilder(
                loginName
        );

        redis.delete(key);
    }

    public void refreshTTL(String loginName) {
        String key = LoginRedisKeys.loginInfoKeyBuilder(loginName);

        redis.expire(
                key,
                SESSION_TTL
        );

    }
}
