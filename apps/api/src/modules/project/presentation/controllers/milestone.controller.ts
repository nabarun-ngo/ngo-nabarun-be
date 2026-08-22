import { Body, Controller, Get, HttpStatus, Param, Patch, Post, Put, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { RequirePermissions, UnifiedAuthGuard } from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoResponse, ApiUuidParam } from '@nabarun-ngo/nestjs-shared-core';
import {
  CreateMilestoneDto,
  MilestoneDetailDto,
  MilestoneListResponseDto,
  UpdateMilestoneDto,
} from '../../application/dtos/milestone.dto';
import { ProjectFacade } from '../../application/services/project.facade';

@ApiTags('Milestone')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('projects/:projectId/milestones')
export class MilestoneController {
  constructor(private readonly projectFacade: ProjectFacade) {}

  @Get()
  @RequirePermissions('read:milestones')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiAutoResponse(MilestoneListResponseDto)
  list(@Param('projectId') projectId: string): Promise<MilestoneListResponseDto> {
    return this.projectFacade.listMilestones(projectId);
  }

  @Post('create')
  @RequirePermissions('create:milestone')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiAutoResponse(MilestoneDetailDto, { status: HttpStatus.CREATED })
  create(
    @Param('projectId') projectId: string,
    @Body() dto: CreateMilestoneDto,
  ): Promise<MilestoneDetailDto> {
    return this.projectFacade.createMilestone(projectId, dto);
  }

  @Put(':id/update')
  @RequirePermissions('update:milestone')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiUuidParam('id', 'Identifier of the milestone')
  @ApiAutoResponse(MilestoneDetailDto)
  update(
    @Param('id') id: string,
    @Body() dto: UpdateMilestoneDto,
  ): Promise<MilestoneDetailDto> {
    return this.projectFacade.updateMilestone(id, dto);
  }

  @Patch(':id/complete')
  @RequirePermissions('update:milestone')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiUuidParam('id', 'Identifier of the milestone')
  @ApiAutoResponse(MilestoneDetailDto)
  complete(@Param('id') id: string): Promise<MilestoneDetailDto> {
    return this.projectFacade.completeMilestone(id);
  }
}
