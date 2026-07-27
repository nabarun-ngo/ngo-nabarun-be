import { CustomFormsModule } from "@nabarun-ngo/nestjs-shared-custom-forms";
import { ConfigModule, ConfigService } from "@nestjs/config";
import { Configkey } from "../shared/config-keys";

import { EntityType } from '../shared/entity-type.enum';

export const CUSTOM_FORM_MODULE = CustomFormsModule.forRootAsync({
    imports: [ConfigModule],
    inject: [ConfigService],
    useFactory: (config: ConfigService) => ({
        allowedEntityTypes: [
            { entityType: EntityType.Donation },
            { entityType: EntityType.Workflow, displayName: 'Workflow' },
            { entityType: EntityType.PublicSite, displayName: 'Public Site' },
        ],
        encryptionKey: config.get<string>(Configkey.APP_SECRET),
    }),
})
