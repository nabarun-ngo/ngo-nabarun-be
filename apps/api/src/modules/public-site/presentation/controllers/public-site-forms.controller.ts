import { Body, Controller, Get, Param, Post } from '@nestjs/common';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { ApiOperation, ApiSecurity, ApiTags } from '@nestjs/swagger';
import {
  Public,
  UseApiKey,
} from '@nabarun-ngo/nestjs-shared-auth';
import {
  ApiAutoResponse,
  ApiKeyParam,
  BypassSuccessEnvelope,
} from '@nabarun-ngo/nestjs-shared-core';
import { SubmitDynamicPublicFormCommand } from '../../application/commands/submit-dynamic-public-form/submit-dynamic-public-form.command';
import { GetDynamicPublicFormDefinitionQuery } from '../../application/queries/get-dynamic-public-form-definition/get-dynamic-public-form-definition.query';
import { PublicFormSubmitResponseDto } from '../../application/dtos/public-form-submit-response.dto';
import { PublicFormDefinitionResponseDto } from '../../application/dtos/public-form-definition-response.dto';
import { PublicFormDefinitionDto } from '../../application/mappers/public-form-definition.mapper';

@ApiTags('PublicSiteForms')
@Controller('public-site')
@Public()
export class PublicSiteFormsController {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) {}

  @Post('dynamic-forms/:publicFormKey/submit')
  @ApiOperation({ summary: 'Submit a standalone dynamic public form' })
  @ApiKeyParam('publicFormKey', 'volunteer-intake')
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
  @ApiKeyParam('publicFormKey', 'volunteer-intake')
  @ApiAutoResponse(PublicFormDefinitionResponseDto, {
    wrapInSuccessResponse: false,
    description: 'Published form definition — returned without the success envelope',
  })
  getDynamicFormDefinition(
    @Param('publicFormKey') publicFormKey: string,
  ): Promise<PublicFormDefinitionDto> {
    return this.queryBus.execute(
      new GetDynamicPublicFormDefinitionQuery(publicFormKey),
    );
  }
}
