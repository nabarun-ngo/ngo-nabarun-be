import { z } from 'zod';

export const PublicSiteProjectMetadataSchema = z.object({
  image: z.string().optional(),
  icon: z.string().optional(),
  impactTitle: z.string().optional(),
  impactLabel: z.string().optional(),
});

export const PublicSiteDynamicContentSchema = z.object({
  stats: z.object({
    /** Active beneficiaries across public projects — hero "Lives Touched". */
    beneficiaryCount: z.number().int().nonnegative(),
    /** Active public-tagged projects — hero "Projects". */
    projectCount: z.number().int().nonnegative(),
  }),
  team: z.array(
    z.object({
      id: z.string(),
      fullName: z.string(),
      picture: z.string(),
      roleString: z.string(),
      email: z.string(),
      bio: z.string().optional(),
      socialLinks: z
        .object({
          facebook: z.string().optional(),
          twitter: z.string().optional(),
          linkedin: z.string().optional(),
          instagram: z.string().optional(),
        })
        .optional(),
      active: z.boolean().optional(),
    }),
  ),
  projects: z.array(
    z.object({
      title: z.string(),
      description: z.string(),
      goals: z.array(
        z.object({
          name: z.string(),
          description: z.string().optional(),
          active: z.boolean(),
        }),
      ),
      beneficiaryCount: z.number().int().nonnegative(),
      metadata: PublicSiteProjectMetadataSchema,
    }),
  ),
  events: z.array(
    z.object({
      id: z.string(),
      title: z.string(),
      description: z.string(),
      date: z.string(),
      endDate: z.string().optional(),
      location: z.string(),
      projectName: z.string(),
      image: z.string().optional(),
      registrationUrl: z.string().optional(),
      active: z.boolean().optional(),
    }),
  ),
});

export const PublicSiteStaticContentSchema = z.record(z.string(), z.unknown());

export type PublicSiteDynamicContent = z.infer<typeof PublicSiteDynamicContentSchema>;
export type PublicSiteStaticContent = z.infer<typeof PublicSiteStaticContentSchema>;

export type PublicSiteImpactStats = PublicSiteDynamicContent['stats'];

export type PublicSiteTeamMember = PublicSiteDynamicContent['team'][number];
export type PublicSiteProject = PublicSiteDynamicContent['projects'][number];
export type PublicSiteProjectMetadata = z.infer<typeof PublicSiteProjectMetadataSchema>;
export type PublicSiteEvent = PublicSiteDynamicContent['events'][number];
