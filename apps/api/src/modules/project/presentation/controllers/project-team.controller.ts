import { Body, Controller, Get, HttpCode, HttpStatus, Param, Patch, Post, Put, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { RequirePermissions, UnifiedAuthGuard } from '@nabarun-ngo/nestjs-shared-auth';
import { TeamMemberRole } from '../../domain/enums/team-member.enum';
import { ProjectFacade } from '../../application/services/project.facade';

@ApiTags('ProjectTeam')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('projects/:projectId/team')
export class ProjectTeamController {
  constructor(private readonly projectFacade: ProjectFacade) {}

  @Get()
  @RequirePermissions('read:project_team')
  list(@Param('projectId') projectId: string) {
    return this.projectFacade.listTeamMembers(projectId);
  }

  @Post('add')
  @HttpCode(HttpStatus.CREATED)
  @RequirePermissions('create:project_team')
  add(
    @Param('projectId') projectId: string,
    @Body() dto: { userId: string; role: TeamMemberRole; startDate: Date; responsibilities?: string; hoursAllocated?: number },
  ) {
    return this.projectFacade.addTeamMember(projectId, dto);
  }

  @Put(':id/update')
  @RequirePermissions('update:project_team')
  update(
    @Param('id') id: string,
    @Body() dto: { role?: TeamMemberRole; responsibilities?: string; hoursAllocated?: number },
  ) {
    return this.projectFacade.updateTeamMember(id, dto);
  }

  @Patch(':id/deactivate')
  @RequirePermissions('update:project_team')
  deactivate(@Param('id') id: string) {
    return this.projectFacade.deactivateTeamMember(id);
  }
}
