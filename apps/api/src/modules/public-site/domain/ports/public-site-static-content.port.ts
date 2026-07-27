export interface IPublicSiteStaticContentPort {
  getStaticContent(): Promise<Record<string, unknown>>;
}

export const PUBLIC_SITE_STATIC_CONTENT_PORT = Symbol('IPublicSiteStaticContentPort');
