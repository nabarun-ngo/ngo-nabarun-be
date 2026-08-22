import { Injectable, Logger } from '@nestjs/common';
import { JsonStoreFacade } from '@nabarun-ngo/nestjs-shared-json-store';
import { JsonStoreNameSpace } from '../../../../shared/enums/json-store-namespaces';
import { IRequestDefinitionPort } from '../../domain/ports/request-definition.port';
import {
  RequestDefinition,
  RequestDefinitionSchema,
} from '../../request-definition.schema';

@Injectable()
export class RequestDefinitionAdapter implements IRequestDefinitionPort {
  private static readonly NAMESPACE = JsonStoreNameSpace.RequestDefinitions;
  private readonly logger = new Logger(RequestDefinitionAdapter.name);

  constructor(private readonly jsonStore: JsonStoreFacade) {}

  async listDefinitions(): Promise<RequestDefinition[]> {
    const docs = await this.jsonStore.list(RequestDefinitionAdapter.NAMESPACE);
    const definitions: RequestDefinition[] = [];
    for (const doc of docs) {
      if (!doc?.payload || doc.key?.startsWith('_')) continue;
      const parsed = RequestDefinitionSchema.safeParse(doc.payload);
      if (!parsed.success) {
        this.logger.warn(
          `Invalid request-definitions/${doc.key}: ${parsed.error.message}`,
        );
        continue;
      }
      definitions.push(parsed.data);
    }
    return definitions.sort((a, b) => a.name.localeCompare(b.name));
  }

  async getDefinition(type: string): Promise<RequestDefinition | null> {
    const payload = await this.jsonStore.get(type, RequestDefinitionAdapter.NAMESPACE);
    if (!payload) return null;
    const parsed = RequestDefinitionSchema.safeParse(payload);
    if (!parsed.success) {
      this.logger.warn(`Invalid request-definitions/${type}: ${parsed.error.message}`);
      return null;
    }
    return parsed.data;
  }
}
