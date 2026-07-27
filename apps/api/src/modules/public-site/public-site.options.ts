

export interface PublicSiteOptions {
  /** Public alias → workflow definition id (must exist in json-store workflow namespace). */
  publicWorkflows: Record<string, { definitionId: string }>;
  staticContent:{
    key: string;
    namespace: string;
  };
  submittedById?: string;
}

export const PUBLIC_SITE_OPTIONS = Symbol('PUBLIC_SITE_OPTIONS');

export const PUBLIC_SITE_DEFAULT_SUBMITTED_BY_ID = 'public:anonymous';
