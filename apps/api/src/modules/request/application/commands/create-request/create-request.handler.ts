import { randomUUID } from 'crypto';
import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { requireUserId } from '@nabarun-ngo/nestjs-shared-auth';
import { CustomFormsFacade } from '@nabarun-ngo/nestjs-shared-custom-forms';
import { EntityType } from '../../../../../shared/enums/entity-type.enum';
import { RequestEventType } from '../../../domain/enums/request-event-type.enum';
import { RequestStatus } from '../../../domain/enums/request-status.enum';
import { RequestDefinitionNotFoundError } from '../../../domain/errors/request.errors';
import { IRequestDefinitionPort } from '../../../domain/ports/request-definition.port';
import { IRequestRepository } from '../../../domain/repositories/request.repository';
import { RequestDto } from '../../dtos/request.dto';
import { toRequestDto } from '../../mappers/request-response.mapper';
import { definitionApprovers, definitionExecutors } from '../../../request-definition.schema';
import { CreateRequestCommand } from './create-request.command';

const FORM_SERVICE_PERMISSIONS = [
  'admin:workflows',
  'read:custom_forms',
  'read:form_submissions',
  'write:form_submissions',
  'submit:form_submissions',
];

@CommandHandler(CreateRequestCommand)
@Injectable()
export class CreateRequestHandler
  implements ICommandHandler<CreateRequestCommand, RequestDto>
{
  constructor(
    @Inject(IRequestDefinitionPort)
    private readonly definitions: IRequestDefinitionPort,
    @Inject(IRequestRepository)
    private readonly requests: IRequestRepository,
    private readonly customForms: CustomFormsFacade,
  ) {}

  async execute(command: CreateRequestCommand): Promise<RequestDto> {
    const userId = requireUserId(command.user);
    const definition = await this.definitions.getDefinition(command.dto.type);
    if (!definition) {
      throw new RequestDefinitionNotFoundError(command.dto.type);
    }

    const requestId = randomUUID();
    const formId = await this.customForms.resolveFormIdForKey(
      EntityType.Workflow,
      definition.formKey,
      FORM_SERVICE_PERMISSIONS,
      userId,
    );

    await this.customForms.submitForm({
      formId,
      entityType: EntityType.Workflow,
      entityId: requestId,
      values: command.dto.formValues ?? {},
      userId,
      submittedById: userId,
      userPermissions: FORM_SERVICE_PERMISSIONS,
    });

    const needApproval = definition.needApproval ?? false;
    const approvers = definitionApprovers(definition);
    const executors = definitionExecutors(definition);

    const created = await this.requests.create({
      id: requestId,
      type: definition.id,
      name: definition.name,
      formKey: definition.formKey,
      formSubmissionId: formId,
      status: needApproval ? RequestStatus.PendingForApproval : RequestStatus.YetToStart,
      initiatedById: userId,
      initiatedForId: command.dto.initiatedForId ?? userId,
      executorRoles: executors.roles,
      executorGroups: executors.groups,
      executorPermissions: executors.permissions,
      approverRoles: approvers.roles,
      approverGroups: approvers.groups,
      approverPermissions: approvers.permissions,
      needApproval,
    });

    await this.requests.appendEvent({
      requestId: created.id,
      type: RequestEventType.Created,
      actorId: userId,
      payload: { type: definition.id },
    });

    return toRequestDto(created, {
      executorInstructions: definition.executorInstructions,
    });
  }
}
