import { Inject, Injectable } from '@nestjs/common';
import {
  BasePrismaService,
  PrismaCrudRepositoryBase,
} from '@nabarun-ngo/nestjs-shared-persistence';
import { PrismaClient } from '../prisma/client';
import {
  FormSubmissionWhereInput,
  FormSubmissionWhereUniqueInput,
  FormSubmissionOrderByWithRelationInput,
} from '../prisma/models/FormSubmission';
import {
  IFormSubmissionRepository,
  IFormRepository,
  FieldValueCodecService,
} from '@nabarun-ngo/nestjs-shared-custom-forms';
import type { CustomFieldValueParsed } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/value-objects/field-condition/field-condition.vo';
import { FormSubmission } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/aggregates/form-submission/form-submission.aggregate';
import { Form } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/aggregates/form/form.aggregate';
import { FormFieldDefinition } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/entities/form-field-definition/form-field-definition.entity';
import { FormFieldValue } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/entities/form-field-value/form-field-value.entity';
import { FormFieldValueHistoryEntry } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/entities/form-field-value-history-entry/form-field-value-history-entry.entity';
import {
  isStoredValueEmpty,
  storedValuesEqual,
  type FormFieldStoredValue,
} from '@nabarun-ngo/nestjs-shared-custom-forms';
import { FormSubmissionStatus } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/enums/form-submission-status.enum';
import { FormSubmissionFilter } from '@nabarun-ngo/nestjs-shared-custom-forms/domain/repositories/form-submission.repository';
import {
  parsedToStoredFieldValue,
  storedToParsedFieldValue,
} from './form-field-value-persistence.util';

type FormFieldValueHistoryRow = {
  id: string;
  formFieldValueId: string;
  entityType: string;
  entityId: string;
  formId: string;
  fieldDefId: string;
  oldValue: string[];
  newValue: string[];
  changedBy: string;
  createdAt: Date;
};

type FormFieldValueRow = {
  id: string;
  entityType: string;
  entityId: string;
  formId: string;
  formSubmissionId: string;
  fieldDefId: string;
  value: string[];
  createdAt: Date;
  updatedAt: Date;
  historyEntries?: FormFieldValueHistoryRow[];
};

type FormSubmissionRow = {
  id: string;
  entityType: string;
  entityId: string;
  formId: string;
  status: string;
  submittedAt: Date | null;
  submittedBy: string | null;
  createdAt: Date;
  updatedAt: Date;
  fieldValues?: FormFieldValueRow[];
};

const INCLUDE_FIELD_VALUES = {
  fieldValues: {
    orderBy: { createdAt: 'asc' as const },
    include: { historyEntries: { orderBy: { createdAt: 'asc' as const } } },
  },
} as const;

@Injectable()
export class FormSubmissionPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'formSubmission',
    FormSubmission,
    string,
    FormSubmissionFilter,
    FormSubmissionRow,
    FormSubmissionWhereInput,
    FormSubmissionWhereUniqueInput,
    any,
    any,
    FormSubmissionOrderByWithRelationInput
  >
  implements IFormSubmissionRepository {
  constructor(
    database: BasePrismaService<PrismaClient>,
    @Inject(IFormRepository)
    private readonly formRepo: IFormRepository,
    private readonly codec: FieldValueCodecService,
  ) {
    super(database, 'formSubmission');
  }

  async findByEntity(
    entityType: string,
    entityId: string,
    formId: string,
  ): Promise<FormSubmission | null> {
    const row = await (this.delegate).findFirst({
      where: { entityType, entityId, formId },
      include: INCLUDE_FIELD_VALUES,
    });
    if (!row) return null;

    const form = await this.formRepo.findByIdWithFields(formId);
    if (!form) return null;

    return this.toDomainWithFields(row as FormSubmissionRow, form);
  }

  async saveDraft(submission: FormSubmission, form: Form): Promise<FormSubmission> {
    const { entityType, entityId, formId } = submission;
    const existingRow = await (this.delegate).findFirst({
      where: { entityType, entityId, formId },
    });

    if (!existingRow) {
      await (this.delegate).create({
        data: {
          id: submission.id,
          entityType,
          entityId,
          formId,
          status: submission.status,
          createdAt: submission.createdAt ?? new Date(),
          updatedAt: new Date(),
        },
      });
    }

    const client = this.database.client;
    const defById = new Map(form.fields.map((f) => [f.id, f]));

    for (const fieldValue of submission.fieldValues) {
      const def = defById.get(fieldValue.fieldDefId);
      if (!def) continue;

      const stored = await parsedToStoredFieldValue(this.codec, def, fieldValue.value);

      const existing = await client.formFieldValue.findFirst({
        where: { entityType, entityId, formId, fieldDefId: fieldValue.fieldDefId },
      }) as FormFieldValueRow | null;

      const latestHistory = fieldValue.history[fieldValue.history.length - 1];
      const changedBy = latestHistory?.changedBy;

      if (existing) {
        if (!storedValuesEqual(existing.value, stored)) {
          await client.formFieldValue.update({
            where: { id: existing.id },
            data: {
              value: stored,
              updatedAt: new Date(),
              historyEntries: changedBy
                ? {
                  create: {
                    formId,
                    fieldDefId: fieldValue.fieldDefId,
                    entityType,
                    entityId,
                    oldValue: existing.value,
                    newValue: stored,
                    changedBy,
                    createdAt: new Date(),
                  },
                }
                : undefined,
            },
          });
        }
      } else {
        const initialHistory = fieldValue.history[0];
        const oldStored: FormFieldStoredValue = [];
        await client.formFieldValue.create({
          data: {
            id: fieldValue.id,
            entityType,
            entityId,
            formId,
            formSubmissionId: submission.id,
            fieldDefId: fieldValue.fieldDefId,
            value: stored,
            createdAt: fieldValue.createdAt ?? new Date(),
            updatedAt: new Date(),
            historyEntries: initialHistory && changedBy
              ? {
                create: {
                  formId,
                  fieldDefId: fieldValue.fieldDefId,
                  entityType,
                  entityId,
                  oldValue: oldStored,
                  newValue: stored,
                  changedBy,
                  createdAt: initialHistory.createdAt ?? new Date(),
                },
              }
              : undefined,
          },
        });
      }
    }

    await (this.delegate).update({
      where: { id: submission.id },
      data: { updatedAt: new Date() },
    });

    const reloaded = await this.findByEntity(entityType, entityId, formId);
    return reloaded ?? submission;
  }

  async clearByEntity(
    entityType: string,
    entityId: string,
    formId: string,
  ): Promise<void> {
    await (this.delegate).deleteMany({ where: { entityType, entityId, formId } });
  }

  async findHistoryByEntity(
    entityType: string,
    entityId: string,
    formId: string,
    fieldDefId?: string,
  ): Promise<FormFieldValueHistoryEntry[]> {
    const form = await this.formRepo.findByIdWithFields(formId);
    if (!form) return [];

    const defById = new Map(form.fields.map((f) => [f.id, f]));

    const rows = await (this.database.client).formFieldValueHistoryEntry.findMany({
      where: {
        entityType,
        entityId,
        formId,
        ...(fieldDefId ? { fieldDefId } : {}),
      },
      orderBy: { createdAt: 'asc' },
    }) as FormFieldValueHistoryRow[];

    return Promise.all(
      rows.map(async (row) => {
        const def = defById.get(row.fieldDefId);
        let oldValue: CustomFieldValueParsed = null;
        let newValue: CustomFieldValueParsed = null;
        if (def) {
          oldValue = await storedToParsedFieldValue(this.codec, def, row.oldValue);
          newValue = await storedToParsedFieldValue(this.codec, def, row.newValue);
        }
        return new FormFieldValueHistoryEntry(
          row.id,
          row.formId,
          row.fieldDefId,
          row.entityType,
          row.entityId,
          oldValue,
          newValue,
          row.changedBy,
          row.createdAt,
        );
      }),
    );
  }

  protected toDomain(row: FormSubmissionRow): FormSubmission {
    return new FormSubmission(
      row.id,
      row.entityType,
      row.entityId,
      row.formId,
      row.status as FormSubmissionStatus,
      [],
      row.createdAt,
      row.updatedAt ?? undefined,
      row.submittedAt ?? undefined,
      row.submittedBy ?? undefined,
    );
  }

  private async toDomainWithFields(row: FormSubmissionRow, form: Form): Promise<FormSubmission> {
    const defById = new Map(form.fields.map((f) => [f.id, f]));
    const fieldValues = await Promise.all(
      (row.fieldValues ?? []).map((fv) => this.toFieldValueDomainAsync(fv, defById)),
    );

    return new FormSubmission(
      row.id,
      row.entityType,
      row.entityId,
      row.formId,
      row.status as FormSubmissionStatus,
      fieldValues,
      row.createdAt,
      row.updatedAt ?? undefined,
      row.submittedAt ?? undefined,
      row.submittedBy ?? undefined,
    );
  }

  private async toFieldValueDomainAsync(
    row: FormFieldValueRow,
    defById: Map<string, FormFieldDefinition>,
  ): Promise<FormFieldValue> {
    const def = defById.get(row.fieldDefId);
    const parsed = def
      ? await storedToParsedFieldValue(this.codec, def, row.value ?? [])
      : null;

    const history = await Promise.all(
      (row.historyEntries ?? []).map(async (h) => {
        let oldValue: CustomFieldValueParsed = null;
        let newValue: CustomFieldValueParsed = null;
        if (def) {
          oldValue = await storedToParsedFieldValue(this.codec, def, h.oldValue);
          newValue = await storedToParsedFieldValue(this.codec, def, h.newValue);
        }
        return new FormFieldValueHistoryEntry(
          h.id,
          h.formId,
          h.fieldDefId,
          h.entityType,
          h.entityId,
          oldValue,
          newValue,
          h.changedBy,
          h.createdAt,
        );
      }),
    );

    return new FormFieldValue(
      row.id,
      row.entityType,
      row.entityId,
      row.formId,
      row.fieldDefId,
      parsed,
      history,
      row.createdAt,
      row.updatedAt ?? undefined,
    );
  }

  protected toCreateInput(entity: FormSubmission): any {
    return {
      id: entity.id,
      entityType: entity.entityType,
      entityId: entity.entityId,
      formId: entity.formId,
      status: entity.status,
      createdAt: entity.createdAt ?? new Date(),
      updatedAt: entity.updatedAt ?? new Date(),
    };
  }

  protected toUpdateInput(_id: string, entity: FormSubmission): any {
    return {
      status: entity.status,
      submittedAt: entity.submittedAt ?? null,
      submittedBy: entity.submittedBy ?? null,
      updatedAt: entity.updatedAt ?? new Date(),
    };
  }

  protected toUniqueWhere(id: string): FormSubmissionWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(filter?: FormSubmissionFilter): FormSubmissionWhereInput {
    return {
      ...(filter?.entityType ? { entityType: filter.entityType } : {}),
      ...(filter?.entityId ? { entityId: filter.entityId } : {}),
      ...(filter?.formId ? { formId: filter.formId } : {}),
    };
  }

  protected defaultOrderBy(): FormSubmissionOrderByWithRelationInput {
    return { createdAt: 'asc' };
  }
}
