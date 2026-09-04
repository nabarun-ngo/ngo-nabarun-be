import { Body, Controller, Get, HttpCode, HttpStatus, Param, Patch, Post, Put, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { RequirePermissions, UnifiedAuthGuard } from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoPagedResponse, ApiAutoResponse, ApiUuidParam, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { CreateGoalCommand } from '../../application/commands/create-goal/create-goal.command';
import { UpdateGoalCommand } from '../../application/commands/update-goal/update-goal.command';
import { UpdateGoalProgressCommand } from '../../application/commands/update-goal-progress/update-goal-progress.command';
import { ListGoalsQuery } from '../../application/queries/list-goals/list-goals.query';
import { GoalMapper } from '../../application/mappers/goal.mapper';
import { CreateGoalDto, GoalDetailDto, GoalDetailFilterDto, UpdateGoalDto, UpdateGoalProgressDto } from '../../application/dtos/goal.dto';

@ApiTags('Goal')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('projects/:projectId/goals')
export class GoalController {
  constructor(private readonly commandBus: CommandBus, private readonly queryBus: QueryBus) { }

  @Get()
  @RequirePermissions('read:goals')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiAutoPagedResponse(GoalDetailDto)
  list(
    @Param('projectId') projectId: string,
    @Query() filter?: GoalDetailFilterDto,
  ): Promise<PagedResponse<GoalDetailDto>> {
    return this.queryBus.execute(new ListGoalsQuery(projectId, filter));
  }

  @Post('create')
  @HttpCode(HttpStatus.CREATED)
  @RequirePermissions('create:goal')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiAutoResponse(GoalDetailDto, { status: HttpStatus.CREATED })
  async create(@Param('projectId') projectId: string, @Body() dto: CreateGoalDto): Promise<GoalDetailDto> {
    return GoalMapper.toDto(await this.commandBus.execute(new CreateGoalCommand({ ...dto, projectId })));
  }

  @Put(':id/update')
  @RequirePermissions('update:goal')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiUuidParam('id', 'Identifier of the goal')
  @ApiAutoResponse(GoalDetailDto)
  async update(@Param('id') id: string, @Body() dto: UpdateGoalDto): Promise<GoalDetailDto> {
    return GoalMapper.toDto(await this.commandBus.execute(new UpdateGoalCommand({ id, ...dto })));
  }

  @Patch(':id/progress')
  @RequirePermissions('update:goal')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiUuidParam('id', 'Identifier of the goal')
  @ApiAutoResponse(GoalDetailDto, { description: 'Goal returned with its recalculated progress' })
  async progress(@Param('id') id: string, @Body() dto: UpdateGoalProgressDto): Promise<GoalDetailDto> {
    return GoalMapper.toDto(await this.commandBus.execute(new UpdateGoalProgressCommand({ id, currentValue: dto.currentValue })));
  }
}
