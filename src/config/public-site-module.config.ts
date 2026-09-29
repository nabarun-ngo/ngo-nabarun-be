import { PublicSiteModule } from '../modules/public-site/public-site.module';
import { PROJECT_MODULE } from './project-module.config';
import { USER_MODULE } from './user-module.config';

export const PUBLIC_SITE_MODULE = PublicSiteModule.forRoot({
  imports: [USER_MODULE, PROJECT_MODULE],
});
