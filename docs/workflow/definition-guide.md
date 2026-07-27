# Workflow Definition Guide

This guide explains how to **author a workflow definition** — the JSON blueprint that tells the engine what steps to run, who acts on them, and how decisions route the process.

It is written for **product owners, business analysts, and developers** who design processes. For API integration, handlers, and local setup, see [developer-guide.md](./developer-guide.md).

---

## What is a workflow definition?

A workflow definition is a JSON document that describes a business process as a **directed graph**:

- **Elements** — steps in the process (start, tasks, decisions, end)
- **Flows** — arrows connecting elements (with optional conditions)

When someone starts a workflow, the engine creates an **instance** and walks the graph until it reaches a human task, an automated task, or a terminal end.

```
  [Start] → [Validate] → [Review task] → {Resolved?}
                              ↑              │
                              └──── No ──────┘
                                             Yes → [End]
```

Definitions are stored in **JsonStore** under the `workflow` namespace and validated before use.

| Storage key | Meaning |
|-------------|---------|
| `workflow/MY_TYPE` | Latest published definition (used when starting instances) |
| `workflow/MY_TYPE@v2` | Immutable version snapshot (optional) |
| `workflow/MY_TYPE@draft` | Work in progress — **cannot** start instances |

**Seed location (development):**  
`apps/api/src/shared/seeds/json-store/data/workflow/MY_TYPE.json`

---

## Definition structure

Every definition has this top-level shape:

```json
{
  "id": "MY_WORKFLOW",
  "version": 1,
  "name": "Human-readable title",
  "description": "Optional longer description",
  "elements": [ /* ... */ ],
  "flows": [ /* ... */ ]
}
```

| Field | Required | Rules |
|-------|----------|-------|
| `id` | Yes | Uppercase identifier, unique across the system (e.g. `CONTACT_REQUEST`). Becomes `definitionId` in context. |
| `version` | Yes | Positive integer. Bump when publishing breaking changes. |
| `name` | Yes | Display name shown in admin UI and instance lists. |
| `description` | No | Supports template placeholders like `{{formatDate startDate 'dd/MM/yyyy'}}` for display. |
| `elements` | Yes | At least 2 elements. Exactly **one** `startEvent`. At least **one** `endEvent`. |
| `flows` | Yes | At least 1 flow. Every `sourceRef` / `targetRef` must match an element `id`. |

---

## Element types

### Overview

| Type | Purpose | Human involvement |
|------|---------|-------------------|
| `startEvent` | Entry point when a workflow is started | Whoever submits the request |
| `userTask` | Waits for a person to complete a form | Yes — appears in inbox |
| `serviceTask` | Runs automated code asynchronously | No — runs in background |
| `exclusiveGateway` | Chooses **one** outgoing path (XOR) | No — evaluated automatically |
| `parallelGateway` | Splits into parallel branches or joins them | Depends on branch tasks |
| `inclusiveGateway` | Same fork/join semantics as parallel in current engine | Depends on branch tasks |
| `subProcess` | Starts a child workflow | Depends on child definition |
| `endEvent` | Terminal — workflow completes (or branch ends) | No |

Every element shares these optional fields:

| Field | Description |
|-------|-------------|
| `id` | Unique within the definition. Use a short prefix (`start`, `ut_`, `svc_`, `xg_`, `pg_`, `end_`). |
| `name` | Label for timeline and admin views. |
| `documentation` | Free-text notes for authors (not shown to end users). |

---

### startEvent

Exactly one per workflow. The engine begins here when an instance is created.

```json
{
  "id": "start",
  "type": "startEvent",
  "name": "Start",
  "formKey": "CONTACT_REQUEST:request"
}
```

| Field | Description |
|-------|-------------|
| `formKey` | Optional. If set, initial `formValues` on start are saved to Custom Forms under this key. Use pattern `{TYPE}:request`. |

**Tip:** Put validation (`ValidateInputs` service task) immediately after start so bad submissions fail fast.

---

### userTask

Pauses the workflow until a user completes a task form. Creates an **inbox item**.

```json
{
  "id": "ut_support",
  "type": "userTask",
  "name": "Provide Support",
  "formKey": "CONTACT_REQUEST_SUPPORT",
  "candidateRoles": ["SECRETARY", "ASSISTANT_SECRETARY"],
  "slaHours": 48
}
```

| Field | Description |
|-------|-------------|
| `formKey` | Form the user fills when completing the task. Must exist in Custom Forms. |
| `candidateRoles` | Auth roles that may claim/complete this task when unassigned. |
| `assigneeExpression` | Optional expr-eval expression resolving to a user id (direct assignment). |
| `slaHours` | Optional. Schedules an escalation timer; deadline stored on the inbox row. |

**Who sees the task in inbox?**

- Users **directly assigned** via `assigneeExpression`
- Users in any listed **candidate role**
- Users with global `update:task` permission

**Loop-back:** If a flow returns to the same `userTask` element (e.g. “try again”), the engine **reopens** the existing inbox row instead of creating a duplicate.

---

### serviceTask

Runs a registered handler in the background (via queue). The workflow waits until the handler finishes.

```json
{
  "id": "svc_validate",
  "type": "serviceTask",
  "name": "Validate Inputs",
  "handler": "ValidateInputs"
}
```

| Field | Description |
|-------|-------------|
| `handler` | Name of a `@WorkflowTaskHandler('…')` class registered in the host app. |
| `inputMapping` | Optional map of handler input keys → context paths (see [Context & input mapping](#context--input-mapping)). |
| `compensationHandler` | Reserved for future saga rollbacks (not executed today). |

**Built-in handlers:**

| Handler | Purpose |
|---------|---------|
| `ValidateInputs` | Checks required fields per workflow type |
| `UserNotRegisteredTaskHandler` | Duplicate email check (onboarding) |

Other handlers (donation updates, Auth0 creation, etc.) are registered per module — coordinate with a developer before referencing a new handler name.

---

### exclusiveGateway

Routes to **exactly one** outgoing flow based on conditions.

```json
{
  "id": "xg_support_route",
  "type": "exclusiveGateway",
  "name": "Support Routing"
}
```

Connect multiple outgoing flows with `condition` or `isDefault`:

```json
{ "id": "f_done",  "sourceRef": "xg_support_route", "targetRef": "end_completed", "condition": "isResolved == 'Yes'" },
{ "id": "f_loop",  "sourceRef": "xg_support_route", "targetRef": "ut_support",    "isDefault": true }
```

**Evaluation order:**

1. Flows **with** `condition` — first match wins
2. Flow marked `isDefault: true`
3. First unconditional flow (no `condition`)
4. Error if nothing matches

---

### parallelGateway

Runs multiple branches **at the same time**, then waits for **all** branches before continuing past a join gateway.

Always use **two separate gateway elements** — one fork, one join:

```json
{ "id": "pg_fork", "type": "parallelGateway", "name": "Verification Fork", "gatewayDirection": "fork" },
{ "id": "pg_join", "type": "parallelGateway", "name": "Verification Join", "gatewayDirection": "join" }
```

```
                    ┌→ [Verify data] ────┐
[Fork] ─────────────┤                    ├→ [Join] → [Next step]
                    └→ [Policy accept] ──┘
```

**Rules:**

- Fork gateway: set `gatewayDirection: "fork"` (or omit — fork is the default)
- Join gateway: set `gatewayDirection: "join"`
- Join gateway must have **exactly one** outgoing flow
- Every incoming branch to the join must complete before the workflow continues
- Each parallel branch gets its own inbox task if it contains a `userTask`

See `JOIN_REQUEST.json` for a full example.

---

### inclusiveGateway

Same fork/join behaviour as `parallelGateway` in the current engine. On fork, only flows whose `condition` is true (or the default) are taken. Use when branches are **conditionally** parallel rather than always parallel.

---

### subProcess

Starts a nested workflow.

```json
{
  "id": "sub_onboarding",
  "type": "subProcess",
  "name": "Background check",
  "definitionId": "BACKGROUND_CHECK",
  "definitionVersion": 1
}
```

| Field | Description |
|-------|-------------|
| `definitionId` | Child workflow type to start |
| `definitionVersion` | Optional pinned version |
| `embedded` | Optional inline nested definition (advanced — rarely needed) |

Non-embedded sub-processes emit an outbox event; the host must create and link the child instance.

---

### endEvent

Marks completion. Use at least one per workflow; multiple ends are allowed (approved vs rejected).

```json
{ "id": "end_completed", "type": "endEvent", "name": "Completed", "terminateAll": true }
```

| Field | Description |
|-------|-------------|
| `terminateAll` | When `true`, cancels all remaining parallel branches and closes the instance immediately. Use on final approved/rejected ends. |

---

## Sequence flows

Flows connect elements. They are **not** visual lines — each is an explicit JSON object:

```json
{
  "id": "f_support_done",
  "sourceRef": "xg_support_route",
  "targetRef": "end_completed",
  "condition": "isResolved == 'Yes'",
  "name": "Resolved"
}
```

| Field | Required | Description |
|-------|----------|-------------|
| `id` | Yes | Unique within the definition |
| `sourceRef` | Yes | Element id where the flow starts |
| `targetRef` | Yes | Element id where the flow ends |
| `condition` | No | Expression evaluated against instance context (exclusive/inclusive gateways) |
| `isDefault` | No | Fallback when no condition matches (exclusive gateway) |
| `name` | No | Documentation label |

**Linear flows** (most steps) need no condition — one outgoing flow per element is enough.

---

## Conditions and context

### How context works

When a workflow runs, the engine maintains a **context** object — a flat JSON bag of data:

- Fields passed at **start** (`context` in the API)
- **`definitionId`** injected automatically
- **Form values** merged when each task is completed

Example context after a few steps:

```json
{
  "definitionId": "CONTACT_REQUEST",
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "isResolved": "Yes",
  "resolutionRemarks": "Called member.",
  "form": {
    "isResolved": "Yes",
    "resolutionRemarks": "Called member."
  }
}
```

Gateway conditions read **top-level keys** (e.g. `isResolved`, not `form.isResolved`).

### Writing conditions

Conditions use [expr-eval](https://www.npmjs.com/package/expr-eval) syntax:

| Pattern | Example |
|---------|---------|
| Equality | `decision == 'Approve'` |
| Inequality | `status != 'Rejected'` |
| Logical AND | `correctionNeeded == 'Yes' and rulesAccepted == 'Yes'` |
| Logical OR | `decision == 'Approve' or decision == 'Escalate'` |

**Important conventions:**

- Compare select/radio values as **strings**: `'Yes'`, `'No'`, `'Approve'`, `'Decline'`
- Match form option values **exactly** (case-sensitive)
- Cover all meaningful branches explicitly; use `isDefault: true` as a safe fallback (loop-back, catch-all)

### Common routing patterns

**Approve / reject:**

```json
{ "condition": "decision == 'Approve'", "targetRef": "svc_next_step" },
{ "condition": "decision == 'Decline'", "targetRef": "end_rejected" },
{ "isDefault": true, "targetRef": "end_rejected" }
```

**Retry loop:**

```json
{ "condition": "isCorrected == 'Yes'", "targetRef": "ut_approval" },
{ "isDefault": true, "targetRef": "ut_correction" }
```

**Resolved / needs more work:**

```json
{ "condition": "isResolved == 'Yes'", "targetRef": "end_completed" },
{ "isDefault": true, "targetRef": "ut_support" }
```

---

## Forms

Workflows use **Custom Forms** (`entityType: workflow`). Every `formKey` in your definition must have a matching published form.

### Form key naming

| When | Pattern | Example |
|------|---------|---------|
| Initial submission (start) | `{TYPE}:request` | `CONTACT_REQUEST:request` |
| User task completion | `{TYPE}_{PURPOSE}` | `CONTACT_REQUEST_SUPPORT` |

Longer auto-generated names are fine (see `DONATION_PAUSE_REQUEST` seeds).

### Field design tips

- Use `select` fields with explicit options (`Yes` / `No`, `Approve` / `Decline`)
- **Field names become context keys** — name them exactly as used in gateway conditions
- Required fields on start should match `ValidateInputs` rules (see below)

### Where forms live

Development seeds: `apps/api/src/shared/seeds/` (workflow form definitions).  
After adding forms, run `npx prisma db seed`.

---

## Context & input mapping

By default, service tasks receive the **full instance context** as handler input.

To pass only specific fields, use `inputMapping`:

```json
{
  "id": "svc_notify",
  "type": "serviceTask",
  "handler": "SendNotificationHandler",
  "inputMapping": {
    "email": "context.email",
    "subject": "context.subject"
  }
}
```

Mapping values:

- `context.fieldName` — nested path under context
- `fieldName` — top-level context key shorthand

Handler **return values** are merged back into context (same as form values).

---

## Naming conventions

Consistent ids make definitions readable and diff-friendly.

| Prefix | Element type | Example |
|--------|--------------|---------|
| `start` | startEvent | `start` |
| `ut_` | userTask | `ut_support`, `ut_approval` |
| `svc_` | serviceTask | `svc_validate`, `svc_auth0` |
| `xg_` | exclusiveGateway | `xg_support_route` |
| `pg_` | parallelGateway | `pg_fork`, `pg_join` |
| `ig_` | inclusiveGateway | `ig_fork`, `ig_join` |
| `end_` | endEvent | `end_completed`, `end_rejected` |
| `f_` | flow | `f_support_done` |

Flow ids: `f_{source}_{target}` or `f_{source}_{purpose}`.

---

## Step-by-step: authoring a new workflow

### 1. Sketch the process

On paper or a whiteboard, identify:

- Who submits the request?
- What automated checks run?
- Who approves or acts?
- What decisions branch the flow?
- What are the success and failure end states?
- Are any steps parallel?

### 2. List elements

Convert each box in your diagram to an element with a unique `id`.

### 3. Wire flows

Add flows from start → … → end(s). Add gateways where decisions happen.

### 4. Define forms

For each `startEvent.formKey` and `userTask.formKey`, plan form fields. Align field names with condition expressions.

### 5. Choose handlers

Every `serviceTask` needs a registered handler name. Default validation:

```json
{ "id": "svc_validate", "type": "serviceTask", "handler": "ValidateInputs" }
```

Add required start fields in `validate-inputs.handler.ts`:

```typescript
const REQUIRED_BY_DEFINITION: Record<string, string[]> = {
  MY_WORKFLOW: ['fieldA', 'fieldB'],
};
```

### 6. Write the JSON file

Create `apps/api/src/shared/seeds/json-store/data/workflow/MY_WORKFLOW.json`.

### 7. Validate

```bash
npm test -- --testPathPattern=workflow-seed
```

Or in Node:

```typescript
import { parseWorkflowDefinition } from '@nabarun-ngo/nestjs-shared-workflow';
parseWorkflowDefinition(require('./MY_WORKFLOW.json'));
```

### 8. Seed and smoke-test

```bash
cd apps/api
npx prisma db seed
npm run start
```

Start an instance via `POST /workflows`, complete tasks, inspect `GET /workflows/:id/timeline`.

---

## Complete examples

### Example A — Simple linear + one decision (CONTACT_REQUEST)

Support ticket: validate → staff resolves → done or loop.

```json
{
  "id": "CONTACT_REQUEST",
  "version": 1,
  "name": "Contact & Support Request",
  "description": "Handle contact and support requests",
  "elements": [
    { "id": "start", "type": "startEvent", "name": "Start", "formKey": "CONTACT_REQUEST:request" },
    { "id": "svc_validate", "type": "serviceTask", "name": "Validate Inputs", "handler": "ValidateInputs" },
    {
      "id": "ut_support",
      "type": "userTask",
      "name": "Provide Support",
      "formKey": "CONTACT_REQUEST_SUPPORT",
      "candidateRoles": ["SECRETARY", "ASSISTANT_SECRETARY"],
      "slaHours": 48
    },
    { "id": "xg_support_route", "type": "exclusiveGateway", "name": "Support Routing" },
    { "id": "end_completed", "type": "endEvent", "name": "Completed", "terminateAll": true }
  ],
  "flows": [
    { "id": "f_start_validate", "sourceRef": "start", "targetRef": "svc_validate" },
    { "id": "f_validate_support", "sourceRef": "svc_validate", "targetRef": "ut_support" },
    { "id": "f_support_route", "sourceRef": "ut_support", "targetRef": "xg_support_route" },
    {
      "id": "f_support_done",
      "sourceRef": "xg_support_route",
      "targetRef": "end_completed",
      "condition": "isResolved == 'Yes'"
    },
    {
      "id": "f_support_loop",
      "sourceRef": "xg_support_route",
      "targetRef": "ut_support",
      "isDefault": true
    }
  ]
}
```

### Example B — Approval with approve/reject ends (DONATION_PAUSE_REQUEST)

Treasurer approves → system updates → completed, or default path to end.

Pattern: `userTask` → `exclusiveGateway` → `serviceTask` or `endEvent`.

### Example C — Parallel verification (JOIN_REQUEST)

Two staff tasks run in parallel after validation; both must finish before routing continues.

Key structure:

```
start → validate → check duplicate → pg_fork
                                        ├→ verify member (userTask) ──┐
                                        └→ policy acceptance (userTask) ┴→ pg_join → route…
```

Always pair `pg_fork` with `pg_join`. Do not use one gateway for both.

---

## Design checklist

Before publishing, verify:

- [ ] Exactly one `startEvent`
- [ ] At least one `endEvent`; terminal ends use `terminateAll: true` where appropriate
- [ ] Every element id referenced in flows exists
- [ ] No duplicate element or flow ids
- [ ] Every `userTask.formKey` has a published Custom Form
- [ ] Every `serviceTask.handler` is registered in the host app
- [ ] Gateway conditions use field names that match form fields
- [ ] Exclusive gateways have a clear default or full condition coverage
- [ ] Parallel fork has a matching join with the same number of incoming branches
- [ ] Join gateway has exactly one outgoing flow
- [ ] Required start fields added to `ValidateInputs` (if used)
- [ ] `candidateRoles` use valid auth role names
- [ ] JSON parses via `parseWorkflowDefinition` / seed tests pass

---

## Common mistakes

| Mistake | Symptom | Fix |
|---------|---------|-----|
| Condition value mismatch (`Yes` vs `yes`) | Wrong branch or default taken | Match form option values exactly |
| Missing default on gateway | Engine error: no outgoing flow | Add `isDefault: true` fallback |
| Same gateway for fork and join | Parallel branches never merge | Use separate `pg_fork` and `pg_join` elements |
| `formKey` without published form | Form save fails at runtime | Add form to seeds before testing |
| Handler name typo | Service task stuck / handler not found | Match `@WorkflowTaskHandler('Name')` exactly |
| Draft definition (`id` ending `@draft`) | Start rejected | Publish to `workflow/MY_TYPE` |
| Loop without expecting reopen | (Fixed in engine) Revisit same `userTask` id — engine reopens inbox row | Ensure form/roles still correct on reopen |

---

## Publishing and versioning

| Action | How |
|--------|-----|
| Development seed | Add/update JSON under `shared/seeds/json-store/data/workflow/` |
| Publish via API | `POST /workflows/admin/definitions/publish` (`manage:workflow-definitions`) |
| Version bump | Increment `version` when changing published definitions |
| Draft editing | Store as `workflow/MY_TYPE@draft` — safe to iterate without affecting live instances |

Running instances keep their **original** `definitionVersion`; only new starts pick up the latest published definition.

---

## Reference: published workflow types

| `id` | Summary |
|------|---------|
| `CONTACT_REQUEST` | Support form → staff resolution loop |
| `JOIN_REQUEST` | Member onboarding with parallel verification |
| `DONATION_REQUEST` | Guest donation collection |
| `DONATION_PAUSE_REQUEST` | Treasurer-approved pause |
| `DONATION_AMT_CHANGE_REQUEST` | Treasurer-approved amount change |
| `TERMINATION_REQUEST` | Member offboarding |
| `ACCOUNT_ADJUSTMENT` | Treasurer account create/close |
| `SOCIAL_MEDIA_CAMPAIGN` | Poster → publish → email |
| `SOCIAL_MEDIA_CAMPAIGN_CRON_UPDATE` | Annual cron maintenance |
| `REPORT_REVIEW` | Report approval |

Use these seed files as templates when designing new processes.

---

## Related documentation

- [Developer Guide](./developer-guide.md) — APIs, handlers, module wiring, testing
- [Runbooks](./runbooks.md) — Production troubleshooting

---

## Quick reference card

```
Definition = { id, version, name, elements[], flows[] }

Elements:  startEvent | userTask | serviceTask | exclusiveGateway
           parallelGateway | inclusiveGateway | subProcess | endEvent

Flow:      sourceRef → targetRef  (+ condition | isDefault)

Context:   start data + form values on each task complete
Conditions: top-level keys, string compares, expr-eval syntax

Forms:     start → {TYPE}:request
           tasks → {TYPE}_{STEP}

Validate:  parseWorkflowDefinition(json)
           npm test -- --testPathPattern=workflow-seed
```
