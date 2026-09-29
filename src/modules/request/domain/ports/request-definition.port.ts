import { RequestDefinition } from '../../request-definition.schema';

export const IRequestDefinitionPort = Symbol('IRequestDefinitionPort');

export interface IRequestDefinitionPort {
  listDefinitions(): Promise<RequestDefinition[]>;
  getDefinition(type: string): Promise<RequestDefinition | null>;
}
