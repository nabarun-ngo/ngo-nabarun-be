import { Body, Controller, Get, HttpCode, HttpStatus, Param, Patch, Post, Put, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { RequirePermissions, UnifiedAuthGuard } from '@nabarun-ngo/nestjs-shared-auth';
import { RiskCategory, RiskProbability, RiskSeverity, RiskStatus } from '../../domain/enums/risk.enum';
import { ProjectFacade } from '../../application/services/project.facade';

@ApiTags('ProjectRisk')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('projects/:projectId/risks')
export class ProjectRiskController {
  constructor(private readonly projectFacade: ProjectFacade) {}

  @Get()
  @RequirePermissions('read:risk')
  list(@Param('projectId') projectId: string) {
    return this.projectFacade.listProjectRisks(projectId);
  }

  @Post('create')
  @HttpCode(HttpStatus.CREATED)
  @RequirePermissions('create:risk')
  create(
    @Param('projectId') projectId: string,
    @Body()
    dto: {
      title: string;
      category: RiskCategory;
      severity: RiskSeverity;
      probability: RiskProbability;
      identifiedDate: Date;
      description?: string;
      impact?: string;
      mitigationPlan?: string;
      ownerId?: string;
    },
  ) {
    return this.projectFacade.createProjectRisk(projectId, dto);
  }

  @Put(':id/update')
  @RequirePermissions('update:risk')
  update(
    @Param('id') id: string,
    @Body() dto: { title?: string; severity?: RiskSeverity; probability?: RiskProbability; mitigationPlan?: string; status?: RiskStatus },
  ) {
    return this.projectFacade.updateProjectRisk(id, dto);
  }

  @Patch(':id/resolve')
  @RequirePermissions('update:risk')
  resolve(@Param('id') id: string) {
    return this.projectFacade.resolveProjectRisk(id);
  }
}
