export class ListTeamMembersQuery {
  constructor(public readonly projectId: string) {}
}

export class AddTeamMemberCommand {
  constructor(
    public readonly params: {
      projectId: string;
      userId: string;
      role: string;
      startDate: Date;
      responsibilities?: string;
      hoursAllocated?: number;
    },
  ) {}
}

export class UpdateTeamMemberCommand {
  constructor(
    public readonly params: {
      id: string;
      role?: string;
      responsibilities?: string;
      hoursAllocated?: number;
    },
  ) {}
}

export class DeactivateTeamMemberCommand {
  constructor(public readonly id: string) {}
}

export class ListProjectRisksQuery {
  constructor(public readonly projectId: string) {}
}

export class CreateProjectRiskCommand {
  constructor(public readonly params: Record<string, unknown>) {}
}

export class UpdateProjectRiskCommand {
  constructor(public readonly params: Record<string, unknown>) {}
}

export class ResolveProjectRiskCommand {
  constructor(public readonly id: string) {}
}

export class ListMilestonesQuery {
  constructor(public readonly projectId: string) {}
}

export class CreateMilestoneCommand {
  constructor(public readonly params: Record<string, unknown>) {}
}

export class UpdateMilestoneCommand {
  constructor(public readonly params: Record<string, unknown>) {}
}

export class CompleteMilestoneCommand {
  constructor(public readonly id: string) {}
}
