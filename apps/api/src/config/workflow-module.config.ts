import { WorkflowModule } from "@nabarun-ngo/nestjs-shared-workflow";
import { QUEUE_MODULE } from "./queue-module.config";

export const WORKFLOW_MODULE = WorkflowModule.forRoot(
    { defaultTimezone: 'Asia/Kolkata' },
    { queueModule: QUEUE_MODULE },
);
