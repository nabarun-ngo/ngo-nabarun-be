import { Body, Controller, Get, HttpCode, HttpStatus, Param, Patch, Post, Put, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiNoContentResponse, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { RequirePermissions, UnifiedAuthGuard } from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoPagedResponse, ApiAutoResponse, ApiUuidParam, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { CreateProjectCommand } from '../../application/commands/create-project/create-project.command';
import { UpdateProjectCommand } from '../../application/commands/update-project/update-project.command';
import { CreateActivityCommand } from '../../application/commands/create-activity/create-activity.command';
import { UpdateActivityCommand } from '../../application/commands/update-activity/update-activity.command';
import { LinkExpenseToActivityCommand } from '../../application/commands/link-expense-to-activity/link-expense-to-activity.command';
import { ListProjectsQuery } from '../../application/queries/list-projects/list-projects.query';
import { GetProjectByIdQuery } from '../../application/queries/get-project-by-id/get-project-by-id.query';
import { ListActivitiesQuery } from '../../application/queries/list-activities/list-activities.query';
import { GetProjectReferenceDataQuery } from '../../application/queries/get-project-reference-data/get-project-reference-data.query';
import { GetProjectProgressQuery } from '../../application/queries/get-project-progress/get-project-progress.query';
import { GetProjectDashboardQuery } from '../../application/queries/get-project-dashboard/get-project-dashboard.query';
import { ProjectMapper } from '../../application/mappers/project.mapper';
import { ActivityMapper } from '../../application/mappers/activity.mapper';
import {
  CreateProjectDto,
  ProjectDetailDto,
  ProjectDetailFilterDto,
  ProjectRefDataDto,
  UpdateProjectDto,
} from '../../application/dtos/project.dto';
import {
  ActivityDetailDto,
  ActivityDetailFilterDto,
  CreateActivityDto,
  LinkExpenseToActivityDto,
  UpdateActivityDto,
} from '../../application/dtos/activity.dto';
import {
  ProjectDashboardResponseDto,
  ProjectProgressResponseDto,
} from '../../application/dtos/project-progress.dto';

@ApiTags('Project')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('projects')
export class ProjectController {
  constructor(private readonly commandBus: CommandBus, private readonly queryBus: QueryBus) { }

  @Get('static/referenceData')
  @RequirePermissions('read:projects')
  @ApiAutoResponse(ProjectRefDataDto)
  getReferenceData(): Promise<ProjectRefDataDto> {
    return this.queryBus.execute(new GetProjectReferenceDataQuery());
  }

  @Get()
  @RequirePermissions('read:projects')
  @ApiAutoPagedResponse(ProjectDetailDto)
  listProjects(
    @Query() filter?: ProjectDetailFilterDto,
  ): Promise<PagedResponse<ProjectDetailDto>> {
    return this.queryBus.execute(new ListProjectsQuery(filter));
  }

  @Post('create')
  @HttpCode(HttpStatus.CREATED)
  @RequirePermissions('create:project')
  @ApiAutoResponse(ProjectDetailDto, { status: HttpStatus.CREATED })
  async createProject(@Body() dto: CreateProjectDto): Promise<ProjectDetailDto> {
    const project = await this.commandBus.execute(new CreateProjectCommand(dto));
    return ProjectMapper.toDto(project);
  }

  @Get('activities')
  @RequirePermissions('read:activities')
  @ApiAutoPagedResponse(ActivityDetailDto)
  listAllActivities(
    @Query() filter?: ActivityDetailFilterDto,
  ): Promise<PagedResponse<ActivityDetailDto>> {
    return this.queryBus.execute(new ListActivitiesQuery(filter));
  }

  @Get(':id/progress')
  @RequirePermissions('read:projects')
  @ApiUuidParam('id', 'Identifier of the project')
  @ApiAutoResponse(ProjectProgressResponseDto)
  getProgress(@Param('id') id: string): Promise<ProjectProgressResponseDto> {
    return this.queryBus.execute(new GetProjectProgressQuery(id));
  }

  @Get(':id/dashboard')
  @RequirePermissions('read:projects')
  @ApiUuidParam('id', 'Identifier of the project')
  @ApiAutoResponse(ProjectDashboardResponseDto)
  getDashboard(@Param('id') id: string): Promise<ProjectDashboardResponseDto> {
    return this.queryBus.execute(new GetProjectDashboardQuery(id));
  }

  @Get(':id')
  @RequirePermissions('read:projects')
  @ApiUuidParam('id', 'Identifier of the project')
  @ApiAutoResponse(ProjectDetailDto)
  getProjectById(@Param('id') id: string): Promise<ProjectDetailDto> {
    return this.queryBus.execute(new GetProjectByIdQuery(id));
  }

  @Patch(':id/update')
  @RequirePermissions('update:project')
  @ApiUuidParam('id', 'Identifier of the project')
  @ApiAutoResponse(ProjectDetailDto)
  async updateProject(@Param('id') id: string, @Body() dto: UpdateProjectDto): Promise<ProjectDetailDto> {
    const project = await this.commandBus.execute(new UpdateProjectCommand({ id, ...dto }));
    return ProjectMapper.toDto(project);
  }

  @Get(':id/activities')
  @RequirePermissions('read:activities')
  @ApiUuidParam('id', 'Identifier of the project')
  @ApiAutoPagedResponse(ActivityDetailDto)
  listActivities(
    @Param('id') id: string,
    @Query() filter?: ActivityDetailFilterDto,
  ): Promise<PagedResponse<ActivityDetailDto>> {
    return this.queryBus.execute(
      new ListActivitiesQuery({ ...filter, projectId: id }),
    );
  }

  @Post(':id/activity')
  @HttpCode(HttpStatus.CREATED)
  @RequirePermissions('create:activity')
  @ApiUuidParam('id', 'Identifier of the project')
  @ApiAutoResponse(ActivityDetailDto, { status: HttpStatus.CREATED })
  async createActivity(@Param('id') id: string, @Body() dto: CreateActivityDto): Promise<ActivityDetailDto> {
    const activity = await this.commandBus.execute(new CreateActivityCommand({ ...dto, projectId: id }));
    return ActivityMapper.toDto(activity);
  }

  @Patch(':id/activity/:activityId')
  @RequirePermissions('update:activity')
  @ApiUuidParam('id', 'Identifier of the project')
  @ApiUuidParam('activityId', 'Identifier of the activity')
  @ApiAutoResponse(ActivityDetailDto)
  async updateActivity(
    @Param('activityId') activityId: string,
    @Body() dto: UpdateActivityDto,
  ): Promise<ActivityDetailDto> {
    const activity = await this.commandBus.execute(new UpdateActivityCommand({ activityId, ...dto }));
    return ActivityMapper.toDto(activity);
  }

  @Post(':id/activity/:activityId/link-expense')
  @RequirePermissions('update:activity')
  @HttpCode(HttpStatus.NO_CONTENT)
  @ApiUuidParam('id', 'Identifier of the project')
  @ApiUuidParam('activityId', 'Identifier of the activity')
  @ApiNoContentResponse({ description: 'Expense linked to the activity — no response body' })
  linkExpense(@Param('activityId') activityId: string, @Body() dto: LinkExpenseToActivityDto): Promise<void> {
    return this.commandBus.execute(new LinkExpenseToActivityCommand({ activityId, expenseId: dto.expenseId }));
  }
}
