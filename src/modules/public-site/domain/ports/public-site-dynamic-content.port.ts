import { PublicSiteDynamicContent } from '../../public-site.schema';

export interface IPublicSiteDynamicContentPort {
  getDynamicContent(): Promise<PublicSiteDynamicContent>;
}

export const IPublicSiteDynamicContentPort = Symbol('IPublicSiteDynamicContentPort');
