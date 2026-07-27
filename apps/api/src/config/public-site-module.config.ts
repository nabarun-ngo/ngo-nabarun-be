import { PublicSiteModule } from '../modules/public-site/public-site.module';
import { PROJECT_MODULE } from './project-module.config';
import { USER_MODULE } from './user-module.config';
import { WORKFLOW_MODULE } from './workflow-module.config';

export const PUBLIC_SITE_MODULE = PublicSiteModule.forRoot({
  imports: [WORKFLOW_MODULE, USER_MODULE, PROJECT_MODULE],
  publicWorkflows: {
    contact: { definitionId: 'CONTACT_REQUEST' },
    membership: { definitionId: 'JOIN_REQUEST' },
  }
});
