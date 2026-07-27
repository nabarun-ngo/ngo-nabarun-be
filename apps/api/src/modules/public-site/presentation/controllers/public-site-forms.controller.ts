import { Body, Controller, Get, Param, Post } from '@nestjs/common';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { ApiOperation, ApiSecurity, ApiTags } from '@nestjs/swagger';
import {
  ExpectedRecaptchaAction,
  Public,
  UseApiKey,
} from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoResponse, BypassSuccessEnvelope } from '@nabarun-ngo/nestjs-shared-core';
import { SubmitPublicWorkflowFormCommand } from '../../application/commands/submit-public-workflow-form/submit-public-workflow-form.command';
import { SubmitDynamicPublicFormCommand } from '../../application/commands/submit-dynamic-public-form/submit-dynamic-public-form.command';
import { GetPublicWorkflowFormDefinitionQuery } from '../../application/queries/get-public-workflow-form-definition/get-public-workflow-form-definition.query';
import { GetDynamicPublicFormDefinitionQuery } from '../../application/queries/get-dynamic-public-form-definition/get-dynamic-public-form-definition.query';
import { PublicFormSubmitResponseDto } from '../../application/dtos/public-form-submit-response.dto';
import { PublicFormDefinitionDto } from '../../application/mappers/public-form-definition.mapper';

@ApiTags('PublicSiteForms')
@Controller('public-site')
@Public()
export class PublicSiteFormsController {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) {}

  @Post('workflow-forms/:workflowName/submit')
  @ExpectedRecaptchaAction('submit_public_workflow_form')
  @ApiOperation({ summary: 'Submit a public workflow-backed form' })
  @ApiAutoResponse(PublicFormSubmitResponseDto)
  submitWorkflowForm(
    @Param('workflowName') workflowName: string,
    @Body() body: Record<string, unknown>,
  ): Promise<PublicFormSubmitResponseDto> {
    return this.commandBus.execute(
      new SubmitPublicWorkflowFormCommand(workflowName, body),
    );
  }

  @Get('workflow-forms/:workflowName/form-defination')
  @UseApiKey()
  @ApiSecurity('api-key')
  @BypassSuccessEnvelope()
  @ApiOperation({ summary: 'Get field definitions for a public workflow form' })
  getWorkflowFormDefinition(
    @Param('workflowName') workflowName: string,
  ): Promise<PublicFormDefinitionDto> {
    return this.queryBus.execute(
      new GetPublicWorkflowFormDefinitionQuery(workflowName),
    );
  }

  @Post('dynamic-forms/:publicFormKey/submit')
  @ApiOperation({ summary: 'Submit a standalone dynamic public form' })
  @ApiAutoResponse(PublicFormSubmitResponseDto)
  submitDynamicForm(
    @Param('publicFormKey') publicFormKey: string,
    @Body() body: Record<string, unknown>,
  ): Promise<PublicFormSubmitResponseDto> {
    return this.commandBus.execute(
      new SubmitDynamicPublicFormCommand(publicFormKey, body),
    );
  }

  @Get('dynamic-forms/:publicFormKey/form-defination')
  @UseApiKey()
  @ApiSecurity('api-key')
  @BypassSuccessEnvelope()
  @ApiOperation({ summary: 'Get field definitions for a dynamic public form' })
  getDynamicFormDefinition(
    @Param('publicFormKey') publicFormKey: string,
  ): Promise<PublicFormDefinitionDto> {
    return this.queryBus.execute(
      new GetDynamicPublicFormDefinitionQuery(publicFormKey),
    );
  }
}
