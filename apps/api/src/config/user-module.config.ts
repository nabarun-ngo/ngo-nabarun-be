import { ConfigModule, ConfigService } from "@nestjs/config";
import { UserModule } from "../modules/user/user.module";
import { Configkey } from "../shared/enums/config-keys";

export const USER_MODULE = UserModule.forRootAsync({
    imports: [ConfigModule],
    inject: [ConfigService],
    useFactory: (config: ConfigService) => ({
        idp: {
            domain: config.getOrThrow(Configkey.AUTH0_DOMAIN),
            clientId: config.getOrThrow(Configkey.AUTH0_MANAGEMENT_CLIENT_ID),
            clientSecret: config.getOrThrow(Configkey.AUTH0_MANAGEMENT_CLIENT_SECRET),
            connections: {
                default: { name: 'Username-Password-Authentication', type: 'password' },
                passwordless_email: { name: 'email', type: 'passwordless' },
            },
        },
        spaClientId: config.getOrThrow(Configkey.SPA_CLIENT_ID),
        appFeUrl: config.getOrThrow(Configkey.APP_FE_URL),
        publicApiUrl: config.getOrThrow(Configkey.APP_BE_URL),
        organisationName: config.get(Configkey.APP_NAME) ?? 'Member',
        organisationRegistrationNumber: config.get(Configkey.IDENTITY_CARD_REGISTRATION_NUMBER),
        organisationLogoDataUrl: config.get(Configkey.IDENTITY_CARD_LOGO_DATA_URL),
        defaultRoleKeys: ['MEMBER'],
    }),
});
