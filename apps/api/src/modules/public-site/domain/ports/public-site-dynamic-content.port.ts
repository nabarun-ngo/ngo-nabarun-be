import { PublicSiteDynamicContent } from '../../public-site.schema';

export interface IPublicSiteDynamicContentPort {
  getDynamicContent(): Promise<PublicSiteDynamicContent>;
}

export const PUBLIC_SITE_DYNAMIC_CONTENT_PORT = Symbol('IPublicSiteDynamicContentPort');
