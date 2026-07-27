import { ActivityDetailDto } from '../../../project/application/dtos/activity.dto';
import { GoalDetailDto } from '../../../project/application/dtos/goal.dto';
import { ProjectDetailDto } from '../../../project/application/dtos/project.dto';
import { ActivityStatus } from '../../../project/domain/enums/activity.enum';
import { GoalStatus } from '../../../project/domain/enums/goal.enum';
import { UserResponseDto } from '../../../user/application/dtos/user-response.dto';
import {
  PublicSiteDynamicContent,
  PublicSiteEvent,
  PublicSiteImpactStats,
  PublicSiteProject,
  PublicSiteProjectMetadata,
  PublicSiteTeamMember,
} from '../../public-site.schema';

export function mapPublicSiteDynamicContent(params: {
  stats: PublicSiteImpactStats;
  users: UserResponseDto[];
  projects: Array<{ project: ProjectDetailDto; goals: GoalDetailDto[] }>;
  activities: ActivityDetailDto[];
}): PublicSiteDynamicContent {
  const projects = params.projects
    .map(({ project, goals }) => mapProjectToPublicProject(project, goals))
    .sort(compareProjectsForDisplay)
    .map(stripProjectSortOrder);

  const projectNameById = new Map(
    params.projects.map(({ project }) => [project.id, project.name]),
  );

  return {
    stats: params.stats,
    team: params.users.map(mapUserToTeamMember),
    projects,
    events: params.activities
      .filter((activity) => activity.status !== ActivityStatus.CANCELLED)
      .map((activity) =>
        mapActivityToEvent(activity, projectNameById.get(activity.projectId) ?? ''),
      ),
  };
}

function mapUserToTeamMember(user: UserResponseDto): PublicSiteTeamMember {
  return {
    id: user.id,
    fullName: user.fullName,
    picture: user.picture ?? '',
    roleString: user.title ?? '',
    email: user.email,
    bio: user.about,
    socialLinks: mapSocialLinks(user.socialMediaLinks),
    active: user.isPublic,
  };
}

function mapProjectToPublicProject(
  project: ProjectDetailDto,
  goals: GoalDetailDto[],
): PublicSiteProject & { sortOrder?: number } {
  const rawMetadata = project.metadata ?? {};

  return {
    title: project.name,
    description: project.description,
    goals: mapGoals(goals),
    beneficiaryCount: Math.max(0, project.actualBeneficiaryCount ?? 0),
    metadata: mapProjectMetadata(rawMetadata, project.category),
    sortOrder: readSortOrder(rawMetadata),
  };
}

function mapGoals(goals: GoalDetailDto[]): PublicSiteProject['goals'] {
  return goals.map((goal) => ({
    name: goal.title,
    description: goal.description,
    active: goal.status !== GoalStatus.FAILED,
  }));
}

function mapProjectMetadata(
  metadata: Record<string, unknown>,
  category: string,
): PublicSiteProjectMetadata {
  const legacyOverlay = readLegacyOverlay(metadata);

  return {
    image: readOptionalString(metadata.image),
    icon: readOptionalString(metadata.icon) ?? defaultIcon(category),
    impactTitle:
      readOptionalString(metadata.impactTitle) ?? legacyOverlay?.title,
    impactLabel:
      readOptionalString(metadata.impactLabel) ?? legacyOverlay?.label,
  };
}

function readLegacyOverlay(
  metadata: Record<string, unknown>,
): { title?: string; label?: string } | undefined {
  const overlay = metadata.overlay;
  if (!overlay || typeof overlay !== 'object') {
    return undefined;
  }

  const entry = overlay as { title?: string; stat?: { label?: string } };
  return {
    title: typeof entry.title === 'string' ? entry.title : undefined,
    label:
      entry.stat && typeof entry.stat.label === 'string'
        ? entry.stat.label
        : undefined,
  };
}

function defaultIcon(category: string): string {
  return `fas fa-${category.toLowerCase().replace(/_/g, '-')}`;
}

function readOptionalString(value: unknown): string | undefined {
  return typeof value === 'string' && value.length > 0 ? value : undefined;
}

function readSortOrder(metadata: Record<string, unknown>): number | undefined {
  return typeof metadata.sortOrder === 'number' ? metadata.sortOrder : undefined;
}

function stripProjectSortOrder(
  project: PublicSiteProject & { sortOrder?: number },
): PublicSiteProject {
  const { sortOrder: _sortOrder, ...card } = project;
  return card;
}

function compareProjectsForDisplay(
  a: PublicSiteProject & { sortOrder?: number },
  b: PublicSiteProject & { sortOrder?: number },
): number {
  const orderA = a.sortOrder ?? Number.MAX_SAFE_INTEGER;
  const orderB = b.sortOrder ?? Number.MAX_SAFE_INTEGER;
  if (orderA !== orderB) {
    return orderA - orderB;
  }
  return a.title.localeCompare(b.title);
}

function mapSocialLinks(
  links: UserResponseDto['socialMediaLinks'],
): PublicSiteTeamMember['socialLinks'] {
  const social: NonNullable<PublicSiteTeamMember['socialLinks']> = {};

  for (const link of links) {
    const type = link.linkType.toLowerCase();
    if (type.includes('facebook')) social.facebook = link.linkValue;
    else if (type.includes('twitter') || type.includes('x.com')) social.twitter = link.linkValue;
    else if (type.includes('linkedin')) social.linkedin = link.linkValue;
    else if (type.includes('instagram')) social.instagram = link.linkValue;
  }

  return Object.keys(social).length > 0 ? social : undefined;
}

function mapActivityToEvent(
  activity: ActivityDetailDto,
  projectName: string,
): PublicSiteEvent {
  const metadata = activity.metadata ?? {};

  return {
    id: activity.id,
    title: activity.name,
    description: activity.description ?? '',
    date: formatDate(activity.startDate),
    endDate: activity.endDate ? formatDate(activity.endDate) : undefined,
    location: activity.location ?? activity.venue ?? '',
    projectName,
    image: typeof metadata.image === 'string' ? metadata.image : undefined,
    registrationUrl:
      typeof metadata.registrationUrl === 'string' ? metadata.registrationUrl : undefined,
    active: activity.status !== ActivityStatus.CANCELLED,
  };
}

function formatDate(value: Date | undefined): string {
  if (!value) return '';
  return value.toISOString().slice(0, 10);
}
