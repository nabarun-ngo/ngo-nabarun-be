import { HealthModule } from "@ssdev-toolkit/nestjs-health";
import { ConfigModule, ConfigService } from "@nestjs/config";
import { isProd } from "@ssdev-toolkit/nestjs-core";
import { Configkey } from "../shared/enums/config-keys";

export const HEALTH_MODULE = HealthModule.forRootAsync({
    imports: [ConfigModule],
    inject: [ConfigService],
    useFactory: (config: ConfigService) => ({
        serviceName: config.get<string>(Configkey.APP_NAME),
        // Failure messages can carry hostnames and connection strings, and the
        // probes are public — keep them out of production responses.
        exposeCheckDetails: !isProd(config.getOrThrow<string>(Configkey.NODE_ENV)),
    }),
});
