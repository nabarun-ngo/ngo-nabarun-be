import { join } from 'path';
import { loadJsonStoreSeedFromDir } from '../../seeds/json-store/json-store-seed.loader';
import { validateJsonStoreSeedData } from '../../seeds/json-store/validate-json-store-seed-data';
import { ZodJsonDocumentPayloadValidatorAdapter } from './json-document-payload-validator.adapter';

const jsonStoreSeedDir = join(__dirname, '../../seeds/json-store/data');

describe('json-store seed validation', () => {
  it('validates all loaded seed documents against the schema registry', () => {
    const data = loadJsonStoreSeedFromDir(jsonStoreSeedDir);

    expect(() =>
      validateJsonStoreSeedData(data, new ZodJsonDocumentPayloadValidatorAdapter()),
    ).not.toThrow();
  });
});
