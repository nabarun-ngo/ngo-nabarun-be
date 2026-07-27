import { Body, Controller, Get, HttpCode, HttpStatus, Param, Patch, Post, Put, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { RequirePermissions, UnifiedAuthGuard } from '@nabarun-ngo/nestjs-shared-auth';
import { MilestoneImportance } from '../../domain/enums/milestone.enum';
import { ProjectFacade } from '../../application/services/project.facade';

@ApiTags('Milestone')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('projects/:projectId/milestones')
export class MilestoneController {
  constructor(private readonly projectFacade: ProjectFacade) {}

  @Get()
  @RequirePermissions('read:milestone')
  list(@Param('projectId') projectId: string) {
    return this.projectFacade.listMilestones(projectId);
  }

  @Post('create')
  @HttpCode(HttpStatus.CREATED)
  @RequirePermissions('create:milestone')
  create(
    @Param('projectId') projectId: string,
    @Body() dto: { name: string; targetDate: Date; importance: MilestoneImportance; description?: string },
  ) {
    return this.projectFacade.createMilestone(projectId, dto);
  }

  @Put(':id/update')
  @RequirePermissions('update:milestone')
  update(
    @Param('id') id: string,
    @Body() dto: { name?: string; targetDate?: Date; importance?: MilestoneImportance; description?: string },
  ) {
    return this.projectFacade.updateMilestone(id, dto);
  }

  @Patch(':id/complete')
  @RequirePermissions('update:milestone')
  complete(@Param('id') id: string) {
    return this.projectFacade.completeMilestone(id);
  }
}
