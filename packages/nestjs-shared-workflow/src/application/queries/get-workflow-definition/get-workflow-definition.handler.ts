import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { WorkflowDefinitionNotFoundError } from '../../../domain/errors/workflow.errors';
import {
  IWorkflowDefinitionPort,
  WORKFLOW_DEFINITION_PORT,
} from '../../../domain/ports/workflow-definition.port';
import type { WorkflowDefinition } from '../../../dsl/workflow-definition.schema';
import { GetWorkflowDefinitionQuery } from './get-workflow-definition.query';

@QueryHandler(GetWorkflowDefinitionQuery)
@Injectable()
export class GetWorkflowDefinitionHandler
  implements IQueryHandler<GetWorkflowDefinitionQuery, WorkflowDefinition>
{
  constructor(
    @Inject(WORKFLOW_DEFINITION_PORT)
    private readonly definitionPort: IWorkflowDefinitionPort,
  ) {}

  async execute(query: GetWorkflowDefinitionQuery): Promise<WorkflowDefinition> {
    const definition = await this.definitionPort.getDefinition(
      query.definitionId,
      query.version,
    );
    if (!definition) {
      throw new WorkflowDefinitionNotFoundError(query.definitionId, query.version);
    }
    return definition;
  }
}
