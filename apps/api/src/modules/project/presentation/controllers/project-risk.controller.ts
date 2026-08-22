import { Body, Controller, Get, HttpStatus, Param, Patch, Post, Put, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { RequirePermissions, UnifiedAuthGuard } from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoResponse, ApiUuidParam } from '@nabarun-ngo/nestjs-shared-core';
import {
  CreateProjectRiskDto,
  ProjectRiskDetailDto,
  ProjectRiskListResponseDto,
  UpdateProjectRiskDto,
} from '../../application/dtos/project-risk.dto';
import { ProjectFacade } from '../../application/services/project.facade';

@ApiTags('ProjectRisk')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('projects/:projectId/risks')
export class ProjectRiskController {
  constructor(private readonly projectFacade: ProjectFacade) {}

  @Get()
  @RequirePermissions('read:risks')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiAutoResponse(ProjectRiskListResponseDto)
  list(@Param('projectId') projectId: string): Promise<ProjectRiskListResponseDto> {
    return this.projectFacade.listProjectRisks(projectId);
  }

  @Post('create')
  @RequirePermissions('create:risk')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiAutoResponse(ProjectRiskDetailDto, { status: HttpStatus.CREATED })
  create(
    @Param('projectId') projectId: string,
    @Body() dto: CreateProjectRiskDto,
  ): Promise<ProjectRiskDetailDto> {
    return this.projectFacade.createProjectRisk(projectId, dto);
  }

  @Put(':id/update')
  @RequirePermissions('update:risk')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiUuidParam('id', 'Identifier of the risk')
  @ApiAutoResponse(ProjectRiskDetailDto)
  update(
    @Param('id') id: string,
    @Body() dto: UpdateProjectRiskDto,
  ): Promise<ProjectRiskDetailDto> {
    return this.projectFacade.updateProjectRisk(id, dto);
  }

  @Patch(':id/resolve')
  @RequirePermissions('update:risk')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiUuidParam('id', 'Identifier of the risk')
  @ApiAutoResponse(ProjectRiskDetailDto)
  resolve(@Param('id') id: string): Promise<ProjectRiskDetailDto> {
    return this.projectFacade.resolveProjectRisk(id);
  }
}
