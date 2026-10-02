export interface IPublicSiteStaticContentPort {
  getStaticContent(): Promise<Record<string, unknown>>;
}

export const IPublicSiteStaticContentPort = Symbol('IPublicSiteStaticContentPort');
