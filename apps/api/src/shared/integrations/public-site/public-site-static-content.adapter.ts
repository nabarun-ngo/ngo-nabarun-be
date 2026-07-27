import { Injectable } from '@nestjs/common';
import { JsonStoreFacade } from '@nabarun-ngo/nestjs-shared-json-store';
import { IPublicSiteStaticContentPort } from '../../../modules/public-site/domain/ports/public-site-static-content.port';
import { JsonStoreNameSpace } from '../../enums/json-store-namespaces';

const STATIC_CONTENT_KEY = 'static-content';

@Injectable()
export class PublicSiteStaticContentAdapter implements IPublicSiteStaticContentPort {
  constructor(
    private readonly jsonStore: JsonStoreFacade,
  ) { }

  async getStaticContent(): Promise<Record<string, unknown>> {
    const payload = await this.jsonStore.get(STATIC_CONTENT_KEY, JsonStoreNameSpace.PublicSite);
    if (!payload) {
      throw new Error(
        `Public site static content is not configured (${JsonStoreNameSpace.PublicSite}/${STATIC_CONTENT_KEY})`,
      );
    }
    return payload;
  }
}
