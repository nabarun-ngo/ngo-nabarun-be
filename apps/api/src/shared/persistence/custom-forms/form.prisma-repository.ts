import { randomUUID } from 'crypto';
import { Injectable } from '@nestjs/common';
import {
  BasePrismaService,
  PrismaCrudRepositoryBase,
} from '@nabarun-ngo/nestjs-shared-persistence';
import { PrismaClient, Prisma } from '../prisma/client';
import {
  FormWhereInput,
  FormWhereUniqueInput,
  FormOrderByWithRelationInput,
  FormCreateInput,
  FormUpdateInput,
} from '../prisma/models/Form';
import { IFormRepository } from '@nabarun-ngo/nestjs-shared-custom-forms';
import { Form } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/aggregates/form/form.aggregate';
import { FormFieldDefinition } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/entities/form-field-definition/form-field-definition.entity';
import { FormStatus } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/enums/form-status.enum';
import { CustomFieldType } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/enums/custom-field-type.enum';
import { FieldOption } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/value-objects/field-option/field-option.vo';
import { FieldCondition } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/value-objects/field-condition/field-condition.vo';
import { DependentOptions } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/value-objects/dependent-options/dependent-options.vo';
import { FieldValidationRules } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/value-objects/field-validation-rules/field-validation-rules.vo';
import type { FieldValidationRulesPersistedJson } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/value-objects/field-validation-rules/field-validation-rules.vo';
import { FormFilter } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/repositories/form.repository';
import {
  CUSTOM_FORMS_FIELD_OPTIONS_JSON_STORE_NAMESPACE,
  CustomFormFieldOptionsPayloadSchema,
} from './custom-form-field-options.schema';

type FormFieldDefinitionRow = {
  id: string;
  formId: string;
  key: string;
  label: string;
  fieldType: string;
  mandatory: boolean;
  isHidden: boolean;
  isEncrypted: boolean;
  enabled: boolean;
  sortOrder: number;
  stepId: string | null;
  stepName: string | null;
  conditionJson: Prisma.JsonValue | null;
  dependentOptionsJson: Prisma.JsonValue | null;
  viewPermissions: string[];
  validationRulesJson: Prisma.JsonValue | null;
  createdBy: string | null;
  disabledBy: string | null;
  createdAt: Date;
  updatedAt: Date;
};

type FormRow = {
  id: string;
  entityType: string;
  key: string;
  label: string;
  description: string | null;
  status: string;
  managePermissions: string[];
  readPermissions: string[];
  writePermissions: string[];
  createdBy: string | null;
  publishedBy: string | null;
  disabledBy: string | null;
  createdAt: Date;
  updatedAt: Date;
  fields?: FormFieldDefinitionRow[];
};

const INCLUDE_FIELDS = {
  fields: {
    orderBy: { sortOrder: 'asc' as const },
  },
} as const;

@Injectable()
export class FormPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'form',
    Form,
    string,
    FormFilter,
    FormRow,
    FormWhereInput,
    FormWhereUniqueInput,
    FormCreateInput,
    FormUpdateInput,
    FormOrderByWithRelationInput
  >
  implements IFormRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'form');
  }

  // ── IFormRepository ───────────────────────────────────────────────────────

  async findByKey(entityType: string, key: string): Promise<Form | null> {
    const row = await (this.delegate).findFirst({
      where: { entityType, key },
    });
    return row ? this.toDomain(row as FormRow) : null;
  }

  async findByEntityType(
    entityType: string,
    options?: { status?: FormStatus; includeDisabled?: boolean },
  ): Promise<Form[]> {
    const where: FormWhereInput = { entityType };

    if (options?.status) {
      where.status = options.status;
    } else if (options?.includeDisabled === false) {
      where.status = { not: FormStatus.Disabled };
    }

    const rows = await (this.delegate).findMany({
      where,
      orderBy: { createdAt: 'asc' },
    });
    return (rows as FormRow[]).map((row) => this.toDomain(row));
  }

  async findByIdWithFields(formId: string): Promise<Form | null> {
    const row = await (this.delegate).findUnique({
      where: { id: formId },
      include: INCLUDE_FIELDS,
    });
    if (!row) return null;
    return this.toDomainWithFields(row as unknown as FormRow);
  }

  async findById(id: string): Promise<Form | null> {
    const row = await (this.delegate).findUnique({
      where: { id },
    });
    return row ? this.toDomain(row as FormRow) : null;
  }

  async create(entity: Form): Promise<Form> {
    const data = this.toCreateInput(entity) as Record<string, unknown>;
    const row = await (this.delegate).create({ data: data as any });
    return this.toDomain(row as FormRow);
  }

  async update(id: string, entity: Form): Promise<Form> {
    const client = this.database.client;

    await client.$transaction(async (tx) => {
      await tx.form.update({
        where: { id },
        data: this.toUpdateInput(id, entity),
      });

      for (const field of entity.fields) {
        await tx.formFieldDefinition.upsert({
          where: { id: field.id },
          create: this.toFieldCreateInput(field),
          update: this.toFieldUpdateInput(field),
        });
        await this.syncFieldOptionsInTransaction(tx, field);
      }
    });

    const updated = await this.findByIdWithFields(id);
    return updated!;
  }

  async delete(_id: string): Promise<void> {
    throw new Error('Forms cannot be deleted');
  }

  // ── PrismaCrudRepositoryBase mapping hooks ───────────────────────────────

  protected toDomain(row: FormRow): Form {
    return new Form(
      row.id,
      row.entityType,
      row.key,
      row.label,
      row.description,
      row.status as FormStatus,
      row.managePermissions,
      row.readPermissions,
      row.writePermissions,
      [],
      row.createdAt,
      row.updatedAt ?? undefined,
      row.createdBy ?? undefined,
      row.publishedBy ?? undefined,
      row.disabledBy ?? undefined,
    );
  }

  private async toDomainWithFields(row: FormRow): Promise<Form> {
    const fieldRows = row.fields ?? [];
    const fieldOptionsByFieldId = await this.loadFieldOptionsByFieldIds(fieldRows.map((f) => f.id));
    const fields = fieldRows.map((fieldRow) =>
      this.toFieldDomain(fieldRow, fieldOptionsByFieldId.get(fieldRow.id) ?? []),
    );

    return new Form(
      row.id,
      row.entityType,
      row.key,
      row.label,
      row.description,
      row.status as FormStatus,
      row.managePermissions,
      row.readPermissions,
      row.writePermissions,
      fields,
      row.createdAt,
      row.updatedAt ?? undefined,
      row.createdBy ?? undefined,
      row.publishedBy ?? undefined,
      row.disabledBy ?? undefined,
    );
  }

  private toFieldDomain(
    row: FormFieldDefinitionRow,
    fieldOptions: FieldOption[],
  ): FormFieldDefinition {
    const condition = FormPrismaRepository.parseConditionJson(row.conditionJson);

    const dependentOptions = FormPrismaRepository.parseDependentOptionsJson(
      row.dependentOptionsJson,
    );

    const validationRules = FormPrismaRepository.parseValidationRulesJson(
      row.validationRulesJson,
    );

    const viewPermissions: string[] = row.viewPermissions ?? [];

    return new FormFieldDefinition(
      row.id,
      row.formId,
      row.key,
      row.label,
      row.fieldType as CustomFieldType,
      row.mandatory,
      fieldOptions,
      row.isHidden,
      row.isEncrypted,
      row.enabled,
      row.sortOrder,
      condition,
      dependentOptions,
      row.createdAt,
      row.updatedAt ?? undefined,
      row.createdBy ?? undefined,
      row.disabledBy ?? undefined,
      viewPermissions,
      validationRules,
      row.stepId,
      row.stepName,
    );
  }

  protected toCreateInput(entity: Form): FormCreateInput {
    return {
      id: entity.id,
      entityType: entity.entityType,
      key: entity.key,
      label: entity.label,
      description: entity.description,
      status: entity.status,
      managePermissions: [...entity.managePermissions],
      readPermissions: [...entity.readPermissions],
      writePermissions: [...entity.writePermissions],
      createdBy: entity.createdBy ?? null,
      createdAt: entity.createdAt ?? new Date(),
      updatedAt: entity.updatedAt ?? new Date(),
    };
  }

  protected toUpdateInput(_id: string, entity: Form): FormUpdateInput {
    return {
      label: entity.label,
      description: entity.description,
      status: entity.status,
      managePermissions: [...entity.managePermissions],
      readPermissions: [...entity.readPermissions],
      writePermissions: [...entity.writePermissions],
      publishedBy: entity.publishedBy ?? null,
      disabledBy: entity.disabledBy ?? null,
      updatedAt: entity.updatedAt ?? new Date(),
    };
  }

  private toFieldCreateInput(field: FormFieldDefinition): any {
    return {
      id: field.id,
      formId: field.formId,
      key: field.key,
      label: field.label,
      fieldType: field.fieldType,
      mandatory: field.mandatory,
      isHidden: field.isHidden,
      isEncrypted: field.isEncrypted,
      enabled: field.enabled,
      sortOrder: field.sortOrder,
      stepId: field.stepId,
      stepName: field.stepName,
      conditionJson: this.serialiseCondition(field),
      dependentOptionsJson: this.serialiseDependentOptions(field),
      validationRulesJson: this.serialiseValidationRules(field),
      viewPermissions: [...field.viewPermissions],
      createdBy: field.createdBy ?? null,
      createdAt: field.createdAt ?? new Date(),
      updatedAt: field.updatedAt ?? new Date(),
    };
  }

  private toFieldUpdateInput(field: FormFieldDefinition): any {
    return {
      label: field.label,
      fieldType: field.fieldType,
      mandatory: field.mandatory,
      isHidden: field.isHidden,
      isEncrypted: field.isEncrypted,
      enabled: field.enabled,
      sortOrder: field.sortOrder,
      stepId: field.stepId,
      stepName: field.stepName,
      conditionJson: this.serialiseCondition(field),
      dependentOptionsJson: this.serialiseDependentOptions(field),
      validationRulesJson: this.serialiseValidationRules(field),
      viewPermissions: [...field.viewPermissions],
      disabledBy: field.disabledBy ?? null,
      updatedAt: field.updatedAt ?? new Date(),
    };
  }

  private async loadFieldOptionsByFieldIds(fieldIds: string[]): Promise<Map<string, FieldOption[]>> {
    const map = new Map<string, FieldOption[]>();
    if (!fieldIds.length) return map;

    const rows = await this.client.jsonStoreDocument.findMany({
      where: {
        namespace: CUSTOM_FORMS_FIELD_OPTIONS_JSON_STORE_NAMESPACE,
        key: { in: fieldIds },
      },
    });

    for (const row of rows) {
      map.set(row.key, FormPrismaRepository.parseFieldOptionsPayload(row.payload));
    }

    return map;
  }

  private static parseFieldOptionsPayload(payload: unknown): FieldOption[] {
    const parsed = CustomFormFieldOptionsPayloadSchema.safeParse(payload);
    if (!parsed.success) return [];
    return parsed.data.options.map((o) => FieldOption.of(o.key, o.label));
  }

  private fieldOptionsPayload(field: FormFieldDefinition): Record<string, unknown> {
    return {
      options: [...field.fieldOptions].map((o) => ({ key: o.key, label: o.label })),
    };
  }

  private toInputJson(payload: Record<string, unknown>): Prisma.InputJsonValue {
    return JSON.parse(JSON.stringify(payload)) as Prisma.InputJsonValue;
  }

  private async syncFieldOptionsInTransaction(
    tx: Pick<PrismaClient, 'jsonStoreDocument'>,
    field: FormFieldDefinition,
  ): Promise<void> {
    const where = {
      json_store_key_namespace_unique: {
        key: field.id,
        namespace: CUSTOM_FORMS_FIELD_OPTIONS_JSON_STORE_NAMESPACE,
      },
    };

    if (!field.fieldOptions.length) {
      await tx.jsonStoreDocument.deleteMany({
        where: {
          key: field.id,
          namespace: CUSTOM_FORMS_FIELD_OPTIONS_JSON_STORE_NAMESPACE,
        },
      });
      return;
    }

    const payload = this.toInputJson(this.fieldOptionsPayload(field));
    const now = new Date();

    await tx.jsonStoreDocument.upsert({
      where,
      create: {
        id: randomUUID(),
        key: field.id,
        namespace: CUSTOM_FORMS_FIELD_OPTIONS_JSON_STORE_NAMESPACE,
        payload,
        createdAt: now,
        updatedAt: now,
      },
      update: {
        payload,
        updatedAt: now,
      },
    });
  }

  private static parseJsonValue(raw: Prisma.JsonValue | null): unknown {
    if (raw === null || raw === undefined) return null;
    if (typeof raw === 'string') {
      return JSON.parse(raw) as unknown;
    }
    return raw;
  }

  private static parseConditionJson(raw: Prisma.JsonValue | null): FieldCondition | null {
    const parsed = FormPrismaRepository.parseJsonValue(raw);
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) return null;
    const c = parsed as {
      dependsOnKey: string;
      operator: 'equals' | 'not_equals' | 'in' | 'not_in';
      value: string | number | boolean | string[];
    };
    return FieldCondition.of(c.dependsOnKey, c.operator, c.value);
  }

  private static parseDependentOptionsJson(
    raw: Prisma.JsonValue | null,
  ): DependentOptions | null {
    const parsed = FormPrismaRepository.parseJsonValue(raw);
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) return null;
    const d = parsed as {
      dependsOnKey: string;
      optionMap: Record<string, Array<{ key: string; label: string }>>;
    };
    const optionMap = Object.fromEntries(
      Object.entries(d.optionMap).map(([k, opts]) => [
        k,
        opts.map((o) => FieldOption.of(o.key, o.label)),
      ]),
    );
    return DependentOptions.of(d.dependsOnKey, optionMap);
  }

  private static parseValidationRulesJson(
    raw: Prisma.JsonValue | null,
  ): FieldValidationRules | null {
    const parsed = FormPrismaRepository.parseJsonValue(raw);
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) return null;
    try {
      return FieldValidationRules.fromPersistedJson(
        parsed as FieldValidationRulesPersistedJson,
      );
    } catch {
      return null;
    }
  }

  private serialiseCondition(field: FormFieldDefinition): Prisma.InputJsonValue | typeof Prisma.JsonNull {
    if (!field.condition) return Prisma.JsonNull;
    return this.toInputJson({
      dependsOnKey: field.condition.dependsOnKey,
      operator: field.condition.operator,
      value: field.condition.value,
    });
  }

  private serialiseDependentOptions(
    field: FormFieldDefinition,
  ): Prisma.InputJsonValue | typeof Prisma.JsonNull {
    if (!field.dependentOptions) return Prisma.JsonNull;
    return this.toInputJson({
      dependsOnKey: field.dependentOptions.dependsOnKey,
      optionMap: Object.fromEntries(
        Object.entries(field.dependentOptions.optionMap).map(
          ([k, opts]: [string, ReadonlyArray<FieldOption>]) => [
            k,
            [...opts].map((o) => ({ key: o.key, label: o.label })),
          ],
        ),
      ),
    });
  }

  private serialiseValidationRules(
    field: FormFieldDefinition,
  ): Prisma.InputJsonValue | typeof Prisma.JsonNull {
    if (!field.validationRules?.patterns.length) return Prisma.JsonNull;
    return this.toInputJson(field.validationRules.toPersistedJson());
  }

  protected toUniqueWhere(id: string): FormWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(filter?: FormFilter): FormWhereInput {
    return {
      ...(filter?.entityType ? { entityType: filter.entityType } : {}),
      ...(filter?.status ? { status: filter.status } : {}),
      ...(filter?.key ? { key: filter.key } : {}),
    };
  }

  protected defaultOrderBy(): FormOrderByWithRelationInput {
    return { createdAt: 'asc' };
  }
}