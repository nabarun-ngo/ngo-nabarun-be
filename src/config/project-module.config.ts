import { ProjectModule } from '../modules/project/project.module';
import { FINANCE_MODULE } from './finance-module.config';
import { QUEUE_MODULE } from './queue-module.config';
import { USER_MODULE } from './user-module.config';

export const PROJECT_MODULE = ProjectModule.forRoot({
  imports: [USER_MODULE, QUEUE_MODULE, FINANCE_MODULE],
});
