import { z } from 'zod';

/** Who may approve or execute a request type (declared criteria, not expanded catalogs). */
export const PartyCriteriaSchema = z.object({
  roles: z.array(z.string().min(1)).default([]),
  permissions: z.array(z.string().min(1)).default([]),
  groups: z.array(z.string().min(1)).default([]),
});

export type PartyCriteria = z.infer<typeof PartyCriteriaSchema>;

const emptyParty = (): PartyCriteria => ({
  roles: [],
  permissions: [],
  groups: [],
});

/**
 * Accepts the nested shape and previous flat field names so older seed documents
 * keep loading until they are rewritten.
 */
function normalizeDefinitionInput(raw: unknown): unknown {
  if (!raw || typeof raw !== 'object' || Array.isArray(raw)) return raw;
  const input = { ...(raw as Record<string, unknown>) };

  const asParty = (value: unknown): PartyCriteria | null => {
    if (!value || typeof value !== 'object' || Array.isArray(value)) return null;
    const party = value as Record<string, unknown>;
    return {
      roles: Array.isArray(party.roles)
        ? party.roles.filter((item): item is string => typeof item === 'string')
        : [],
      permissions: Array.isArray(party.permissions)
        ? party.permissions.filter((item): item is string => typeof item === 'string')
        : [],
      groups: Array.isArray(party.groups)
        ? party.groups.filter((item): item is string => typeof item === 'string')
        : [],
    };
  };

  const stringArray = (value: unknown): string[] =>
    Array.isArray(value)
      ? value.filter((item): item is string => typeof item === 'string')
      : [];

  if (!input.approvers) {
    input.approvers = {
      roles: stringArray(input.approverRoles ?? input.approvalRoles),
      permissions: stringArray(input.approverPermissions ?? input.approvalPermissions),
      groups: stringArray(input.approverGroups ?? input.approvalGroups),
    };
  } else {
    input.approvers = asParty(input.approvers) ?? emptyParty();
  }

  if (!input.executors) {
    input.executors = {
      roles: stringArray(input.executorRoles ?? input.fulfillmentRoles),
      permissions: stringArray(input.executorPermissions ?? input.fulfillmentPermissions),
      groups: stringArray(input.executorGroups ?? input.fulfillmentGroups),
    };
  } else {
    input.executors = asParty(input.executors) ?? emptyParty();
  }

  if (input.executorInstructions == null && typeof input.fulfillmentInstructions === 'string') {
    input.executorInstructions = input.fulfillmentInstructions;
  }

  delete input.approverRoles;
  delete input.approverGroups;
  delete input.approverPermissions;
  delete input.approvalRoles;
  delete input.approvalGroups;
  delete input.approvalPermissions;
  delete input.executorRoles;
  delete input.executorGroups;
  delete input.executorPermissions;
  delete input.fulfillmentRoles;
  delete input.fulfillmentGroups;
  delete input.fulfillmentPermissions;
  delete input.fulfillmentInstructions;

  return input;
}

/** One request type definition stored as json-store document payload. */
export const RequestDefinitionSchema = z.preprocess(
  normalizeDefinitionInput,
  z.object({
    id: z.string().min(1),
    version: z.number().int().positive().default(1),
    name: z.string().min(1),
    description: z.string().optional(),
    formKey: z.string().min(1),
    executorInstructions: z.string().min(1),
    needApproval: z.boolean().default(false),
    approvers: PartyCriteriaSchema.default(emptyParty()),
    executors: PartyCriteriaSchema.default(emptyParty()),
  }),
);

export type RequestDefinition = z.infer<typeof RequestDefinitionSchema>;

export function definitionApprovers(def: RequestDefinition): PartyCriteria {
  return def.approvers ?? emptyParty();
}

export function definitionExecutors(def: RequestDefinition): PartyCriteria {
  return def.executors ?? emptyParty();
}
