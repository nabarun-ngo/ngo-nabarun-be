import { Inject, Injectable } from '@nestjs/common';
import { JsonStoreFacade } from '@nabarun-ngo/nestjs-shared-json-store';
import { IPublicSiteStaticContentPort } from '../../domain/ports/public-site-static-content.port';
import {
  PUBLIC_SITE_OPTIONS,
  PublicSiteOptions,
} from '../../public-site.options';

@Injectable()
export class PublicSiteStaticContentAdapter implements IPublicSiteStaticContentPort {
  constructor(
    private readonly jsonStore: JsonStoreFacade,
    @Inject(PUBLIC_SITE_OPTIONS)
    private readonly options: PublicSiteOptions,
  ) { }

  async getStaticContent(): Promise<Record<string, unknown>> {
    const { namespace, key } = this.options.staticContent;
    const payload = await this.jsonStore.get(key, namespace);
    if (!payload) {
      throw new Error(
        `Public site static content is not configured (${namespace}/${key})`,
      );
    }
    return payload;
  }
}
