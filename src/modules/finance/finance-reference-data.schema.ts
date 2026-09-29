import { z } from 'zod';

const KeyValueOptionSchema = z.object({
  key: z.string().min(1),
  value: z.string().min(1),
});

const KeyValuePayloadSchema = z
  .object({
    items: z.array(KeyValueOptionSchema).min(1),
  })
  .passthrough();

const StatusGroupsPayloadSchema = z.object({
  outstanding: z.array(z.string().min(1)),
  closed: z.array(z.string().min(1)),
  excluded: z.array(z.string()),
});

const TransferMatrixPayloadSchema = z.object({
  rows: z.array(z.object({
    fromAccountType: z.string().min(1),
    reference: z.string().min(1),
    toAccountTypes: z.array(z.string().min(1)).min(1),
  })).min(1),
});

const DonorStatusRulesPayloadSchema = z.object({
  statusesRequiringEndDate: z.array(z.string().min(1)).min(1),
});

/** Payload shape for finance-reference-data documents stored in json-store. */
export const FinanceReferenceDataPayloadSchema = z.union([
  KeyValuePayloadSchema,
  StatusGroupsPayloadSchema,
  TransferMatrixPayloadSchema,
  DonorStatusRulesPayloadSchema,
]);

export type FinanceReferenceDataPayload = z.infer<typeof FinanceReferenceDataPayloadSchema>;
