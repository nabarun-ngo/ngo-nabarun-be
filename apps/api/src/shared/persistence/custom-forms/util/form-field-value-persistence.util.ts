import { FormFieldDefinition } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/entities/form-field-definition/form-field-definition.entity';
import {
  isStoredValueEmpty,
  type FormFieldStoredValue,
} from '@nabarun-ngo/nestjs-shared-custom-forms';
import { isParsedValueEmpty } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/utilities/form-field-parsed-value.util';
import type { CustomFieldValueParsed } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/value-objects/field-condition/field-condition.vo';
import { FieldValueCodecService } from '@nabarun-ngo/nestjs-shared-custom-forms/infrastructure/services/field-value-codec.service';

export async function parsedToStoredFieldValue(
  codec: FieldValueCodecService,
  def: FormFieldDefinition,
  parsed: CustomFieldValueParsed,
): Promise<FormFieldStoredValue> {
  if (isParsedValueEmpty(parsed)) {
    return [];
  }
  const serialised = codec.serialise(def.fieldType, parsed);
  if (isStoredValueEmpty(serialised)) {
    return [];
  }
  return codec.encryptIfNeeded(serialised, def.key, def.isEncrypted);
}

export async function storedToParsedFieldValue(
  codec: FieldValueCodecService,
  def: FormFieldDefinition,
  stored: FormFieldStoredValue,
): Promise<CustomFieldValueParsed> {
  if (isStoredValueEmpty(stored)) {
    return null;
  }
  const raw = await codec.decryptIfNeeded(stored, def.key, def.isEncrypted);
  return codec.parse(def.fieldType, raw, def.key);
}
