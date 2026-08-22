import { Body, Controller, Get, HttpStatus, Param, Patch, Post, Put, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { RequirePermissions, UnifiedAuthGuard } from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoResponse, ApiUuidParam } from '@nabarun-ngo/nestjs-shared-core';
import {
  AddTeamMemberDto,
  TeamMemberDetailDto,
  TeamMemberListResponseDto,
  UpdateTeamMemberDto,
} from '../../application/dtos/team-member.dto';
import { ProjectFacade } from '../../application/services/project.facade';

@ApiTags('ProjectTeam')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('projects/:projectId/team')
export class ProjectTeamController {
  constructor(private readonly projectFacade: ProjectFacade) {}

  @Get()
  @RequirePermissions('read:project_teams')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiAutoResponse(TeamMemberListResponseDto)
  list(@Param('projectId') projectId: string): Promise<TeamMemberListResponseDto> {
    return this.projectFacade.listTeamMembers(projectId);
  }

  @Post('add')
  @RequirePermissions('create:project_team')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiAutoResponse(TeamMemberDetailDto, { status: HttpStatus.CREATED })
  add(
    @Param('projectId') projectId: string,
    @Body() dto: AddTeamMemberDto,
  ): Promise<TeamMemberDetailDto> {
    return this.projectFacade.addTeamMember(projectId, dto);
  }

  @Put(':id/update')
  @RequirePermissions('update:project_team')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiUuidParam('id', 'Identifier of the team membership')
  @ApiAutoResponse(TeamMemberDetailDto)
  update(
    @Param('id') id: string,
    @Body() dto: UpdateTeamMemberDto,
  ): Promise<TeamMemberDetailDto> {
    return this.projectFacade.updateTeamMember(id, dto);
  }

  @Patch(':id/deactivate')
  @RequirePermissions('update:project_team')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiUuidParam('id', 'Identifier of the team membership')
  @ApiAutoResponse(TeamMemberDetailDto)
  deactivate(@Param('id') id: string): Promise<TeamMemberDetailDto> {
    return this.projectFacade.deactivateTeamMember(id);
  }
}
