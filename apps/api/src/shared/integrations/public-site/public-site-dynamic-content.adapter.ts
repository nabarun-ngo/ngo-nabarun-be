import { Injectable } from '@nestjs/common';
import { ActivityScale } from '../../../modules/project/domain/enums/activity.enum';
import { BeneficiaryStatus } from '../../../modules/project/domain/enums/beneficiary.enum';
import { ProjectStatus } from '../../../modules/project/domain/enums/project.enum';
import { ProjectFacade } from '../../../modules/project/application/services/project.facade';
import { UserStatus } from '../../../modules/user/domain/enums/user-status.enum';
import { UserFacade } from '../../../modules/user/application/services/user.facade';
import { mapPublicSiteDynamicContent } from '../../../modules/public-site/application/mappers/public-site-dynamic-content.mapper';
import { IPublicSiteDynamicContentPort } from '../../../modules/public-site/domain/ports/public-site-dynamic-content.port';
import { PublicSiteDynamicContent } from '../../../modules/public-site/public-site.schema';

const PUBLIC_PROJECT_FILTER = {
  isPublic: true,
  status: ProjectStatus.ACTIVE,
} as const;

const PUBLIC_TEAM_FILTER = {
  isPublic: true,
  status: UserStatus.ACTIVE,
} as const;

const PUBLIC_BENEFICIARY_FILTER = { status: BeneficiaryStatus.ACTIVE } as const;

const PUBLIC_SITE_LIST_PAGE = { pageIndex: 0, pageSize: 200 } as const;

@Injectable()
export class PublicSiteDynamicContentAdapter implements IPublicSiteDynamicContentPort {
  constructor(
    private readonly userFacade: UserFacade,
    private readonly projectFacade: ProjectFacade,
  ) { }

  async getDynamicContent(): Promise<PublicSiteDynamicContent> {
    const [usersPage, projectsPage, beneficiaryCount] = await Promise.all([
      this.userFacade.listUsers(PUBLIC_TEAM_FILTER, PUBLIC_SITE_LIST_PAGE),
      this.projectFacade.listProjects(PUBLIC_PROJECT_FILTER, PUBLIC_SITE_LIST_PAGE),
      this.projectFacade.countBeneficiaries(PUBLIC_PROJECT_FILTER, PUBLIC_BENEFICIARY_FILTER),
    ]);

    const projectIds = projectsPage.items.map((project) => project.id);
    const [events, goalsByProjectId] = await Promise.all([
      this.loadEventsForProjects(projectIds),
      this.loadGoalsByProjectId(projectIds),
    ]);

    return mapPublicSiteDynamicContent({
      stats: {
        projectCount: projectsPage.total,
        beneficiaryCount,
      },
      users: usersPage.items,
      projects: projectsPage.items.map((project) => ({
        project,
        goals: goalsByProjectId.get(project.id) ?? [],
      })),
      activities: events,
    });
  }

  private async loadGoalsByProjectId(projectIds: string[]) {
    const entries = await Promise.all(
      projectIds.map(async (projectId) => {
        const page = await this.projectFacade.listProjectGoals({ projectId });
        return [projectId, page.items] as const;
      }),
    );

    return new Map(entries);
  }

  private async loadEventsForProjects(projectIds: string[]) {
    if (projectIds.length === 0) {
      return [];
    }

    const pages = await Promise.all(
      projectIds.map((projectId) =>
        this.projectFacade.listActivities(
          { projectId, scale: ActivityScale.EVENT },
          PUBLIC_SITE_LIST_PAGE,
        ),
      ),
    );

    return pages.flatMap((page) => page.items);
  }
}
