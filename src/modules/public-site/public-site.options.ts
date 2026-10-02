export interface PublicSiteOptions {
  submittedById?: string;
}

export const PUBLIC_SITE_OPTIONS = Symbol('PUBLIC_SITE_OPTIONS');

export const PUBLIC_SITE_DEFAULT_SUBMITTED_BY_ID = 'public:anonymous';
