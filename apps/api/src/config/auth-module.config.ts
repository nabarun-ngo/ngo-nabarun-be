import { AuthModule } from "@nabarun-ngo/nestjs-shared-auth";
import { ConfigModule, ConfigService } from "@nestjs/config";
import { Configkey } from "../shared/enums/config-keys";
import { USER_MODULE } from "./user-module.config";

function throttleProfile(
    config: ConfigService,
    limitKey: Configkey,
    ttlKey: Configkey,
    defaultLimit: number,
    defaultTtlMs: number,
) {
    return {
        limit: config.get<number>(limitKey) ?? defaultLimit,
        ttlMs: config.get<number>(ttlKey) ?? defaultTtlMs,
    };
}

export const AUTH_MODULE = AuthModule.forRootAsync({
    imports: [ConfigModule, USER_MODULE],
    inject: [ConfigService],
    useFactory: (config: ConfigService) => ({
        jwt: {
            jwksUri: config.getOrThrow(Configkey.JWT_JWKS_URI),
            issuer: config.getOrThrow(Configkey.JWT_ISSUER),
            audience: config.getOrThrow(Configkey.JWT_AUDIENCE),
        },
        recaptcha: {
            secretKey: config.getOrThrow(Configkey.GOOGLE_RECAPTCHA_SECURITY_KEY),
            minScore: config.get<number>(Configkey.RECAPTCHA_MIN_SCORE) ?? 0.65,
        },
        apiKey: {
            headerName: 'X-API-KEY',
        },
        cache: {
            userAccessTtlMs: 10 * 24 * 60 * 60 * 1000,
            emailVerificationTtlMs: 10 * 24 * 60 * 60 * 1000,
        },
        throttler: {
            storageRedisUrl: config.getOrThrow<string>(Configkey.REDIS_URL),
            skipPathPrefixes: ['/health', '/ready', '/metrics'],
            profiles: {
                default: throttleProfile(config, Configkey.THROTTLE_DEFAULT_LIMIT, Configkey.THROTTLE_DEFAULT_TTL_MS, 600, 60_000),
                open: throttleProfile(config, Configkey.THROTTLE_OPEN_LIMIT, Configkey.THROTTLE_OPEN_TTL_MS, 10, 60_000),
                protected: throttleProfile(config, Configkey.THROTTLE_PROTECTED_LIMIT, Configkey.THROTTLE_PROTECTED_TTL_MS, 300, 60_000),
            },
            enabled: true
        },
    }),
});
