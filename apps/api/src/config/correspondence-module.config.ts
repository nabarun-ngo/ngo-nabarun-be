import { QUEUE_MODULE } from "./queue-module.config";
import { CorrespondenceModule } from "@nabarun-ngo/nestjs-shared-correspondence";
import { ConfigModule, ConfigService } from "@nestjs/config";
import { Configkey } from "../shared/enums/config-keys";
import { isProd } from "@nabarun-ngo/nestjs-shared-core";
import { USER_MODULE } from "./user-module.config";
import { buildEmailTheme } from "./email-theme.config";

export const CORRESPONDENCE_MODULE = CorrespondenceModule.forRootAsync(
    {
        imports: [ConfigModule, USER_MODULE],
        inject: [ConfigService],
        useFactory: (config: ConfigService) => ({
            appName: config.get(Configkey.APP_NAME),
            environment: config.getOrThrow<string>(Configkey.NODE_ENV),
            allowedResourceTypes: [
                { resourceType: 'project' },
                { resourceType: 'request' },
            ],
            email: {
                fromName: config.getOrThrow(Configkey.APP_NAME),
                fromAddress: config.get(Configkey.EMAIL_SENDER),
                enableMocking: config.getOrThrow(Configkey.ENABLE_EMAIL_MOCKING),
                mockedAddress: config.getOrThrow(Configkey.MOCKED_EMAIL_ADDRESS),
                enableProdMode:
                    config.get<boolean>(Configkey.ENABLE_PROD_MODE) ??
                    isProd(config.getOrThrow<string>(Configkey.NODE_ENV)) ??
                    false,
                theme: buildEmailTheme(),
            },
            push: {
                oneSignal: {
                    appId: config.getOrThrow<string>(Configkey.ONESIGNAL_APP_ID),
                    apiKey: config.getOrThrow<string>(Configkey.ONESIGNAL_REST_API_KEY),
                },
            },
        }),
    },
    { queueModule: QUEUE_MODULE },
)