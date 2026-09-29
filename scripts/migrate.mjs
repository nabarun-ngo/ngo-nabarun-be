#!/usr/bin/env node
/**
 * Legacy → CE API ETL (self-contained).
 *
 * Extract:  SOURCE_DATABASE_URL via Postgres cursor (FETCH chunks)
 * Transform: in-memory per chunk (plus guest-donor map and open user groups)
 * Load:     DATABASE_URL bulk INSERT … ON CONFLICT
 * Check:    SQL aggregates; exit 1 on FAIL
 *
 * Usage (apps/api):
 *   DATABASE_URL=… SOURCE_DATABASE_URL=… npm run migrate:legacy
 *   ETL_DRY_RUN=true ETL_CHUNK_SIZE=1000 npm run migrate:legacy
 */

import { randomUUID } from 'node:crypto';
import 'dotenv/config';
import pg from 'pg';

const { Client } = pg;

const CHUNK_SIZE = Math.max(1, Number.parseInt(process.env.ETL_CHUNK_SIZE || '1000', 10) || 1000);
const UPSERT_SIZE = 250;
const DRY_RUN = ['1', 'true', 'yes', 'on'].includes(String(process.env.ETL_DRY_RUN || '').toLowerCase());

function envUrl(name) {
  const value = process.env[name];
  if (!value) throw new Error(`${name} is required`);
  return value;
}

function quoteIdent(name) {
  return `"${String(name).replaceAll('"', '""')}"`;
}

function asText(value, fallback = '') {
  if (value == null || value === '') return fallback;
  return String(value);
}

function parseJson(value) {
  if (value == null || value === '') return null;
  if (typeof value === 'object') return value;
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function newId() {
  return randomUUID();
}

function fullName(first, middle, last) {
  return [first, middle, last].filter((p) => p != null && String(p).trim() !== '').join(' ').trim() || null;
}

function guestDonorId(email, phone, name) {
  const seed = `${email ?? ''}|${phone ?? ''}|${name ?? ''}`;
  const hex = Buffer.from(seed, 'utf8').toString('hex').padEnd(32, '0').slice(0, 32);
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20, 32)}`;
}

function mapAccountType(type) {
  switch (String(type ?? '').toUpperCase()) {
    case 'INVESTMENT':
      return 'INVESTMENT';
    case 'WALLET':
      return 'WALLET';
    default:
      return 'BANK';
  }
}

function logStep(name) {
  console.log(`\n[legacy-etl] ${name}`);
}

function logCount(label, count) {
  console.log(`[legacy-etl]   ${label}: ${count}`);
}

async function tableExists(client, tableName) {
  const { rows } = await client.query(
    `SELECT 1 FROM information_schema.tables
     WHERE table_schema = 'public' AND table_name = $1 LIMIT 1`,
    [tableName],
  );
  return rows.length > 0;
}

async function resolveLegacyTable(client, candidates) {
  for (const name of candidates) {
    if (await tableExists(client, name)) return name;
  }
  return null;
}

async function countTable(client, tableName) {
  if (!(await tableExists(client, tableName))) return null;
  const { rows } = await client.query(`SELECT COUNT(*)::bigint AS c FROM ${quoteIdent(tableName)}`);
  return Number(rows[0].c);
}

async function scalarCount(client, sql) {
  const { rows } = await client.query(sql);
  return Number(rows[0]?.c ?? rows[0]?.v ?? 0);
}

/**
 * @param {import('pg').Client} source
 * @param {string} selectSql SELECT without trailing semicolon
 */
async function withCursor(source, selectSql, onChunk) {
  await source.query('BEGIN');
  try {
    await source.query(`DECLARE etl_cur NO SCROLL CURSOR FOR ${selectSql}`);
    for (;;) {
      const { rows } = await source.query(`FETCH ${CHUNK_SIZE} FROM etl_cur`);
      if (!rows.length) break;
      await onChunk(rows);
    }
    await source.query('CLOSE etl_cur');
    await source.query('COMMIT');
  } catch (error) {
    try {
      await source.query('ROLLBACK');
    } catch {
      /* ignore */
    }
    throw error;
  }
}

async function bulkUpsert(client, { table, columns, rows, conflict, updateCols }) {
  if (DRY_RUN || !rows.length) return rows.length;
  const identCols = columns.map(quoteIdent);
  const conflictList = (Array.isArray(conflict) ? conflict : [conflict]).map(quoteIdent).join(', ');
  const skip = new Set(Array.isArray(conflict) ? conflict : [conflict]);
  const updates = (updateCols ?? columns.filter((c) => !skip.has(c))).map(
    (c) => `${quoteIdent(c)} = EXCLUDED.${quoteIdent(c)}`,
  );
  const setClause = updates.length ? `DO UPDATE SET ${updates.join(', ')}` : 'DO NOTHING';

  let affected = 0;
  for (let i = 0; i < rows.length; i += UPSERT_SIZE) {
    const chunk = rows.slice(i, i + UPSERT_SIZE);
    const params = [];
    const tuples = [];
    let n = 1;
    for (const row of chunk) {
      const ph = [];
      for (const col of columns) {
        params.push(row[col] === undefined ? null : row[col]);
        ph.push(`$${n++}`);
      }
      tuples.push(`(${ph.join(',')})`);
    }
    const sql = `INSERT INTO ${quoteIdent(table)} (${identCols.join(',')}) VALUES ${tuples.join(',')}
      ON CONFLICT (${conflictList}) ${setClause}`;
    const result = await client.query(sql, params);
    affected += result.rowCount ?? 0;
  }
  return affected;
}

async function insertMany(client, { table, columns, rows }) {
  if (DRY_RUN || !rows.length) return rows.length;
  let affected = 0;
  for (let i = 0; i < rows.length; i += UPSERT_SIZE) {
    const chunk = rows.slice(i, i + UPSERT_SIZE);
    const params = [];
    const tuples = [];
    let n = 1;
    for (const row of chunk) {
      const ph = [];
      for (const col of columns) {
        params.push(row[col] === undefined ? null : row[col]);
        ph.push(`$${n++}`);
      }
      tuples.push(`(${ph.join(',')})`);
    }
    const sql = `INSERT INTO ${quoteIdent(table)} (${columns.map(quoteIdent).join(',')}) VALUES ${tuples.join(',')}`;
    const result = await client.query(sql, params);
    affected += result.rowCount ?? 0;
  }
  return affected;
}

async function migrateSimple(source, target, { label, sql, table, columns, conflict, updateCols, mapRow }) {
  logStep(label);
  let count = 0;
  const pending = [];
  const flush = async () => {
    if (!pending.length) return;
    await bulkUpsert(target, { table, columns, rows: pending.splice(0), conflict, updateCols });
  };
  await withCursor(source, sql, async (chunk) => {
    for (const row of chunk) {
      const mapped = mapRow(row);
      if (mapped) pending.push(mapped);
      if (pending.length >= UPSERT_SIZE) await flush();
    }
    count += chunk.length;
  });
  await flush();
  logCount(table, count);
  return count;
}

async function migrateGrouped(source, target, { label, sql, flushGroup }) {
  logStep(label);
  let openId;
  let openRows = [];
  let groups = 0;
  const closeGroup = async () => {
    if (openId === undefined) return;
    await flushGroup(openId, openRows);
    groups += 1;
    openRows = [];
    openId = undefined;
  };
  await withCursor(source, sql, async (chunk) => {
    for (const row of chunk) {
      const uid = row.userId;
      if (uid == null) continue;
      if (openId !== undefined && uid !== openId) {
        await closeGroup();
      }
      openId = uid;
      openRows.push(row);
    }
  });
  await closeGroup();
  logCount(label, groups);
}

function mapUser(r) {
  return {
    id: r.id,
    email: r.email,
    idpSub: r.authUserId ?? r.idpSub ?? null,
    title: r.title,
    firstName: r.firstName,
    middleName: r.middleName,
    lastName: r.lastName,
    dateOfBirth: r.dateOfBirth,
    gender: r.gender,
    about: r.about,
    picture: r.picture,
    uniqueMemberId: null,
    roleKeys: [],
    status: r.status,
    isPublic: r.isPublic ?? true,
    isSameAddress: r.isSameAddress,
    isProfileComplete: !r.isTemporary,
    version: r.version ?? 0,
    createdAt: r.createdAt,
    updatedAt: r.updatedAt,
    deletedAt: r.deletedAt,
  };
}

function mapMemberDonorFromProfile(u) {
  return {
    id: u.id,
    type: 'MEMBER',
    status: u.donationPauseStart || u.donationPauseEnd ? 'PAUSED' : 'ACTIVE',
    preferredAmount: u.donationAmount ?? null,
    statusEndDate: u.donationPauseEnd ?? u.donationPauseStart ?? null,
    fullName: fullName(u.firstName, u.middleName, u.lastName),
    email: u.email,
    phoneCode: null,
    phoneNumber: null,
    userProfileId: u.id,
    createdAt: u.createdAt,
    updatedAt: u.updatedAt,
    version: u.version ?? 0,
    deletedAt: u.deletedAt,
  };
}

const USER_COLS = [
  'id', 'email', 'idpSub', 'title', 'firstName', 'middleName', 'lastName',
  'dateOfBirth', 'gender', 'about', 'picture', 'uniqueMemberId', 'roleKeys',
  'status', 'isPublic', 'isSameAddress', 'isProfileComplete', 'version',
  'createdAt', 'updatedAt', 'deletedAt',
];

const DONOR_COLS = [
  'id', 'type', 'status', 'preferredAmount', 'statusEndDate', 'fullName', 'email',
  'phoneCode', 'phoneNumber', 'userProfileId', 'createdAt', 'updatedAt', 'version', 'deletedAt',
];

async function migrateUsers(source, target) {
  return migrateSimple(source, target, {
    label: 'user_profile',
    sql: `SELECT * FROM user_profiles ORDER BY "createdAt", id`,
    table: 'user_profile',
    columns: USER_COLS,
    conflict: 'id',
    mapRow: mapUser,
  });
}

async function migratePhones(source, target) {
  const pending = [];
  const flush = async () => {
    if (!pending.length) return;
    await bulkUpsert(target, {
      table: 'user_phone_number',
      columns: ['id', 'userId', 'phoneCode', 'phoneNumber', 'hidden', 'isPrimary'],
      rows: pending.splice(0),
      conflict: ['userId', 'isPrimary'],
    });
  };
  await migrateGrouped(source, target, {
    label: 'user_phone_number',
    sql: `SELECT * FROM phone_numbers WHERE "userId" IS NOT NULL ORDER BY "userId", "primary" DESC, id`,
    flushGroup: async (_userId, rows) => {
      const primary = rows.find((p) => p.primary) ?? rows[0];
      const secondary = rows.find((p) => p !== primary);
      if (primary) {
        pending.push({
          id: primary.id ?? newId(),
          userId: primary.userId,
          phoneCode: asText(primary.phoneCode, '+91'),
          phoneNumber: asText(primary.phoneNumber, '0000000000'),
          hidden: primary.hidden ?? false,
          isPrimary: true,
        });
      }
      if (secondary) {
        pending.push({
          id: secondary.id ?? newId(),
          userId: secondary.userId,
          phoneCode: asText(secondary.phoneCode, '+91'),
          phoneNumber: asText(secondary.phoneNumber, '0000000000'),
          hidden: secondary.hidden ?? false,
          isPrimary: false,
        });
      }
      if (pending.length >= UPSERT_SIZE) await flush();
    },
  });
  await flush();
}

async function migrateAddresses(source, target) {
  const pending = [];
  const flush = async () => {
    if (!pending.length) return;
    await bulkUpsert(target, {
      table: 'user_address',
      columns: [
        'id', 'userId', 'addressType', 'addressLine1', 'addressLine2', 'addressLine3',
        'hometown', 'zipCode', 'state', 'district', 'country',
      ],
      rows: pending.splice(0),
      conflict: ['userId', 'addressType'],
    });
  };
  await migrateGrouped(source, target, {
    label: 'user_address',
    sql: `SELECT * FROM addresses WHERE "userId" IS NOT NULL ORDER BY "userId", "addressType", id`,
    flushGroup: async (_userId, rows) => {
      const seen = new Set();
      for (const r of rows) {
        const type = asText(r.addressType, 'PRESENT');
        if (seen.has(type)) continue;
        seen.add(type);
        pending.push({
          id: r.id,
          userId: r.userId,
          addressType: type,
          addressLine1: asText(r.addressLine1, 'N/A'),
          addressLine2: r.addressLine2,
          addressLine3: r.addressLine3,
          hometown: asText(r.hometown, 'N/A'),
          zipCode: asText(r.zipCode, '000000'),
          state: asText(r.state, 'N/A'),
          district: asText(r.district, 'N/A'),
          country: asText(r.country, 'India'),
        });
      }
      if (pending.length >= UPSERT_SIZE) await flush();
    },
  });
  await flush();
}

async function migrateLinks(source, target) {
  const pending = [];
  const flush = async () => {
    if (!pending.length) return;
    await bulkUpsert(target, {
      table: 'user_social_link',
      columns: ['id', 'userId', 'linkName', 'linkType', 'linkValue', 'createdAt', 'updatedAt'],
      rows: pending.splice(0),
      conflict: ['userId', 'linkType'],
    });
  };
  await migrateGrouped(source, target, {
    label: 'user_social_link',
    sql: `SELECT * FROM links WHERE "userId" IS NOT NULL ORDER BY "userId", "linkType", "updatedAt" DESC NULLS LAST, id`,
    flushGroup: async (_userId, rows) => {
      const seen = new Set();
      for (const r of rows) {
        if (seen.has(r.linkType)) continue;
        seen.add(r.linkType);
        pending.push({
          id: r.id,
          userId: r.userId,
          linkName: asText(r.linkName, r.linkType),
          linkType: r.linkType,
          linkValue: r.linkValue,
          createdAt: r.createdAt,
          updatedAt: r.updatedAt,
        });
      }
      if (pending.length >= UPSERT_SIZE) await flush();
    },
  });
  await flush();
}

async function migrateProjects(source, target) {
  const projectsTable = await resolveLegacyTable(source, ['legacy_projects', 'projects']);
  if (!projectsTable) {
    logStep('projects (skipped — no source table)');
    return;
  }
  await migrateSimple(source, target, {
    label: `projects from ${projectsTable}`,
    sql: `SELECT * FROM ${quoteIdent(projectsTable)} ORDER BY "createdAt", id`,
    table: 'projects',
    columns: [
      'id', 'name', 'description', 'code', 'category', 'status', 'phase',
      'startDate', 'endDate', 'actualEndDate', 'budget', 'spentAmount', 'currency',
      'location', 'targetBeneficiaryCount', 'actualBeneficiaryCount',
      'managerId', 'sponsorId', 'tags', 'isPublic', 'metadata',
      'createdAt', 'updatedAt', 'version', 'deletedAt',
    ],
    conflict: 'id',
    mapRow: (r) => ({
      id: r.id,
      name: r.name,
      description: r.description,
      code: r.code,
      category: r.category,
      status: r.status,
      phase: r.phase,
      startDate: r.startDate,
      endDate: r.endDate,
      actualEndDate: r.actualEndDate,
      budget: r.budget,
      spentAmount: r.spentAmount ?? 0,
      currency: r.currency,
      location: r.location,
      targetBeneficiaryCount: r.targetBeneficiaryCount,
      actualBeneficiaryCount: r.actualBeneficiaryCount,
      managerId: r.managerId,
      sponsorId: r.sponsorId,
      tags: r.tags ?? [],
      isPublic: false,
      metadata: r.metadata ?? null,
      createdAt: r.createdAt,
      updatedAt: r.updatedAt,
      version: r.version ?? 0,
      deletedAt: r.deletedAt,
    }),
  });

  const benTable = await resolveLegacyTable(source, ['legacy_beneficiaries', 'beneficiaries']);
  if (benTable) {
    await migrateSimple(source, target, {
      label: `project_beneficiaries from ${benTable}`,
      sql: `SELECT * FROM ${quoteIdent(benTable)} ORDER BY "enrollmentDate", id`,
      table: 'project_beneficiaries',
      columns: [
        'id', 'projectId', 'name', 'type', 'gender', 'age', 'dateOfBirth',
        'contactNumber', 'email', 'address', 'location', 'category',
        'enrollmentDate', 'exitDate', 'status', 'benefitsReceived', 'notes', 'metadata',
        'createdAt', 'updatedAt', 'deletedAt',
      ],
      conflict: 'id',
      mapRow: (b) => ({
        id: b.id,
        projectId: b.projectId,
        name: b.name,
        type: b.type,
        gender: b.gender,
        age: b.age,
        dateOfBirth: b.dateOfBirth,
        contactNumber: b.contactNumber,
        email: b.email,
        address: b.address,
        location: b.location,
        category: b.category,
        enrollmentDate: b.enrollmentDate,
        exitDate: b.exitDate,
        status: b.status,
        benefitsReceived: b.benefitsReceived ?? [],
        notes: b.notes,
        metadata: b.metadata ?? null,
        createdAt: b.createdAt,
        updatedAt: b.updatedAt,
        deletedAt: b.deletedAt,
      }),
    });
  }

  if (await tableExists(source, 'activities')) {
    await migrateSimple(source, target, {
      label: 'activities',
      sql: `SELECT * FROM activities ORDER BY "createdAt", id`,
      table: 'activities',
      columns: [
        'id', 'projectId', 'name', 'description', 'scale', 'type', 'status', 'priority',
        'startDate', 'endDate', 'actualStartDate', 'actualEndDate', 'location', 'venue',
        'assignedTo', 'organizerId', 'parentActivityId', 'expectedParticipants', 'actualParticipants',
        'estimatedCost', 'actualCost', 'currency', 'tags', 'metadata',
        'createdAt', 'updatedAt', 'version', 'deletedAt',
      ],
      conflict: 'id',
      mapRow: (a) => ({
        id: a.id,
        projectId: a.projectId,
        name: a.name,
        description: a.description,
        scale: a.scale,
        type: a.type,
        status: a.status,
        priority: a.priority,
        startDate: a.startDate,
        endDate: a.endDate,
        actualStartDate: a.actualStartDate,
        actualEndDate: a.actualEndDate,
        location: a.location,
        venue: a.venue,
        assignedTo: a.assignedTo,
        organizerId: a.organizerId,
        parentActivityId: a.parentActivityId,
        expectedParticipants: a.expectedParticipants,
        actualParticipants: a.actualParticipants,
        estimatedCost: a.estimatedCost,
        actualCost: a.actualCost,
        currency: a.currency,
        tags: a.tags ?? [],
        metadata: a.metadata ?? null,
        createdAt: a.createdAt,
        updatedAt: a.updatedAt,
        version: a.version ?? 0,
        deletedAt: a.deletedAt,
      }),
    });
  }
}

function bankRowFromAccount(a) {
  const bank = parseJson(a.bankDetail);
  if (!bank || typeof bank !== 'object') return null;
  const holder = bank.bankAccountHolderName ?? bank.bankAccountHolder ?? null;
  const ifsc = bank.IFSCNumber ?? bank.ifscNumber ?? null;
  if (!bank.bankName && !bank.bankAccountNumber && !ifsc) return null;
  return {
    id: newId(),
    accountId: a.id,
    bankAccountHolderName: holder,
    bankName: bank.bankName ?? null,
    bankBranch: bank.bankBranch ?? null,
    bankAccountNumber: bank.bankAccountNumber ?? null,
    bankAccountType: bank.bankAccountType ?? null,
    IFSCNumber: ifsc,
    maturityDate: bank.maturityDate ?? null,
    maturityAmount: bank.maturityAmount ?? null,
    investmentAmount: bank.investmentAmount ?? null,
    sourceAccountId: bank.sourceAccountId ?? null,
    dematId: bank.dematId ?? null,
    interestRate: bank.interestRate ?? null,
    interestPayingTerm: bank.interestPayingTerm ?? null,
    createdAt: a.createdAt,
    updatedAt: a.updatedAt,
  };
}

function upiRowFromAccount(a) {
  const upi = parseJson(a.upiDetail);
  if (!upi || typeof upi !== 'object') return null;
  if (!upi.upiId && !upi.payeeName && !upi.qrData) return null;
  return {
    id: newId(),
    accountId: a.id,
    payeeName: upi.payeeName ?? null,
    upiId: upi.upiId ?? null,
    mobileNumber: upi.mobileNumber ?? null,
    qrData: upi.qrData ?? null,
    label: upi.label ?? null,
    isPrimary: true,
    createdAt: a.createdAt,
    updatedAt: a.updatedAt,
  };
}

async function migrateAccounts(source, target) {
  logStep('finance_accounts');
  const acctCols = [
    'id', 'name', 'type', 'ownerType', 'currency', 'status', 'description', 'balance',
    'accountHolderName', 'accountHolderId', 'custodianUserIds', 'activatedOn',
    'createdById', 'createdAt', 'updatedAt', 'version', 'deletedAt',
  ];
  const bankCols = [
    'id', 'accountId', 'bankAccountHolderName', 'bankName', 'bankBranch',
    'bankAccountNumber', 'bankAccountType', 'IFSCNumber', 'maturityDate',
    'maturityAmount', 'investmentAmount', 'sourceAccountId', 'dematId',
    'interestRate', 'interestPayingTerm', 'createdAt', 'updatedAt',
  ];
  const upiCols = [
    'id', 'accountId', 'payeeName', 'upiId', 'mobileNumber', 'qrData',
    'label', 'isPrimary', 'createdAt', 'updatedAt',
  ];
  let count = 0;
  await withCursor(source, `SELECT * FROM accounts ORDER BY "createdAt", id`, async (chunk) => {
    const accounts = [];
    const banks = [];
    const upis = [];
    for (const a of chunk) {
      accounts.push({
        id: a.id,
        name: a.name,
        type: mapAccountType(a.type),
        ownerType: a.accountHolderId ? 'INDIVIDUAL' : 'ORG',
        currency: a.currency,
        status: a.status,
        description: a.description,
        balance: a.balance ?? 0,
        accountHolderName: a.accountHolderName,
        accountHolderId: a.accountHolderId,
        custodianUserIds: [],
        activatedOn: a.activatedOn,
        createdById: a.createdById,
        createdAt: a.createdAt,
        updatedAt: a.updatedAt,
        version: a.version ?? 0,
        deletedAt: a.deletedAt,
      });
      const bank = bankRowFromAccount(a);
      if (bank) banks.push(bank);
      const upi = upiRowFromAccount(a);
      if (upi) upis.push(upi);
    }
    if (!DRY_RUN) {
      await target.query('BEGIN');
      try {
        await bulkUpsert(target, { table: 'finance_accounts', columns: acctCols, rows: accounts, conflict: 'id' });
        await bulkUpsert(target, {
          table: 'finance_account_bank_invest_details',
          columns: bankCols,
          rows: banks,
          conflict: 'accountId',
        });
        if (upis.length) {
          await target.query(`DELETE FROM finance_account_upi_detail WHERE "accountId" = ANY($1::text[])`, [
            upis.map((u) => u.accountId),
          ]);
          await insertMany(target, { table: 'finance_account_upi_detail', columns: upiCols, rows: upis });
        }
        await target.query('COMMIT');
      } catch (error) {
        await target.query('ROLLBACK');
        throw error;
      }
    }
    count += chunk.length;
  });
  logCount('finance_accounts', count);
}

async function migrateMemberDonors(source, target) {
  logStep('finance_donors (MEMBER — id = user_profile.id)');
  const sql = `
    SELECT * FROM user_profiles up
    WHERE up."donationAmount" IS NOT NULL
       OR up."donationPauseStart" IS NOT NULL
       OR up."donationPauseEnd" IS NOT NULL
       OR up.id IN (
         SELECT d."donorId" FROM donations d
         WHERE d."donorId" IS NOT NULL AND COALESCE(d."isGuest", false) = false
       )
    ORDER BY up.id
  `;
  let count = 0;
  const pending = [];
  const flush = async () => {
    if (!pending.length) return;
    await bulkUpsert(target, { table: 'finance_donors', columns: DONOR_COLS, rows: pending.splice(0), conflict: 'id' });
  };
  await withCursor(source, sql, async (chunk) => {
    for (const u of chunk) pending.push(mapMemberDonorFromProfile(u));
    count += chunk.length;
    if (pending.length >= UPSERT_SIZE) await flush();
  });
  await flush();
  logCount('finance_donors MEMBER', count);
}

function isGuestDonation(d) {
  return Boolean(d.isGuest) || (!d.donorId && (d.donorEmail || d.donorName));
}

async function migrateDonations(source, target) {
  logStep('finance_donations');
  const guestDonorByKey = new Map();
  const donationCols = [
    'id', 'type', 'amount', 'currency', 'status', 'donorId',
    'startDate', 'endDate', 'raisedOn', 'paidOn',
    'confirmedById', 'confirmedOn', 'paymentMethod', 'paidToAccountId', 'forEventId',
    'paidUsingUPI', 'isPaymentNotified', 'transactionRef',
    'remarks', 'cancelletionReason', 'laterPaymentReason', 'paymentFailureDetail',
    'additionalFields', 'createdAt', 'updatedAt', 'version', 'deletedAt',
  ];
  let count = 0;
  await withCursor(source, `SELECT * FROM donations ORDER BY "raisedOn", id`, async (chunk) => {
    const guests = [];
    const donations = [];
    for (const d of chunk) {
      let donorId = d.donorId ?? null;
      if (isGuestDonation(d)) {
        const key = `${d.donorEmail ?? ''}|${d.donorPhone ?? ''}|${d.donorName ?? ''}`;
        let gid = guestDonorByKey.get(key);
        if (!gid) {
          gid = guestDonorId(d.donorEmail, d.donorPhone, d.donorName);
          guestDonorByKey.set(key, gid);
          guests.push({
            id: gid,
            type: 'GUEST',
            status: 'ACTIVE',
            preferredAmount: null,
            statusEndDate: null,
            fullName: d.donorName ?? null,
            email: d.donorEmail ?? null,
            phoneCode: null,
            phoneNumber: d.donorPhone ?? null,
            userProfileId: null,
            createdAt: d.createdAt,
            updatedAt: d.updatedAt,
            version: d.version ?? 0,
            deletedAt: d.deletedAt,
          });
        }
        donorId = gid;
      }
      donations.push({
        id: d.id,
        type: d.type,
        amount: d.amount,
        currency: d.currency,
        status: d.status,
        donorId,
        startDate: d.startDate,
        endDate: d.endDate,
        raisedOn: d.raisedOn,
        paidOn: d.paidOn,
        confirmedById: d.confirmedById,
        confirmedOn: d.confirmedOn,
        paymentMethod: d.paymentMethod,
        paidToAccountId: d.paidToAccountId,
        forEventId: d.forEventId,
        paidUsingUPI: d.paidUsingUPI,
        isPaymentNotified: d.isPaymentNotified ?? false,
        transactionRef: d.transactionRef,
        remarks: d.remarks,
        cancelletionReason: d.cancelletionReason,
        laterPaymentReason: d.laterPaymentReason,
        paymentFailureDetail: d.paymentFailureDetail,
        additionalFields: d.additionalFields ?? null,
        createdAt: d.createdAt,
        updatedAt: d.updatedAt,
        version: d.version ?? 0,
        deletedAt: d.deletedAt,
      });
    }
    if (!DRY_RUN) {
      await target.query('BEGIN');
      try {
        await bulkUpsert(target, {
          table: 'finance_donors',
          columns: DONOR_COLS,
          rows: guests,
          conflict: 'id',
          updateCols: [],
        });
        await bulkUpsert(target, {
          table: 'finance_donations',
          columns: donationCols,
          rows: donations,
          conflict: 'id',
        });
        await target.query('COMMIT');
      } catch (error) {
        await target.query('ROLLBACK');
        throw error;
      }
    }
    count += chunk.length;
  });
  logCount('finance_donations', count);
  logCount('guest donors (unique)', guestDonorByKey.size);
}

async function migrateTxnsExpensesEarnings(source, target) {
  if (await tableExists(source, 'transactions')) {
    await migrateSimple(source, target, {
      label: 'finance_transactions',
      sql: `SELECT * FROM transactions ORDER BY "transactionDate", id`,
      table: 'finance_transactions',
      columns: [
        'id', 'transactionRef', 'type', 'amount', 'currency', 'status',
        'referenceId', 'referenceType', 'description', 'metadata',
        'transactionDate', 'particulars', 'createdById',
        'createdAt', 'updatedAt', 'version', 'deletedAt', 'refAccountId', 'accountId',
      ],
      conflict: 'id',
      mapRow: (t) => ({
        id: t.id,
        transactionRef: t.transactionRef,
        type: t.type,
        amount: t.amount,
        currency: t.currency,
        status: t.status,
        referenceId: t.referenceId,
        referenceType: t.referenceType,
        description: t.description,
        metadata: t.metadata ?? null,
        transactionDate: t.transactionDate,
        particulars: t.particulars,
        createdById: t.createdById,
        createdAt: t.createdAt,
        updatedAt: t.updatedAt,
        version: t.version ?? 0,
        deletedAt: t.deletedAt,
        refAccountId: t.refAccountId,
        accountId: t.accountId,
      }),
    });
  }

  if (await tableExists(source, 'expenses')) {
    await migrateSimple(source, target, {
      label: 'finance_expenses',
      sql: `SELECT * FROM expenses ORDER BY "expenseDate", id`,
      table: 'finance_expenses',
      columns: [
        'id', 'title', 'items', 'amount', 'currency', 'status', 'description',
        'referenceId', 'referenceType', 'isDelegated',
        'createdById', 'paidById', 'expenseDate', 'submittedById', 'submittedOn',
        'finalizedById', 'finalizedOn', 'settledById', 'settledOn',
        'rejectedById', 'rejectedOn', 'updatedById', 'updatedOn',
        'accountId', 'accountName', 'transactionRef', 'remarks',
        'createdAt', 'updatedAt', 'version', 'deletedAt', 'userProfileId',
      ],
      conflict: 'id',
      mapRow: (e) => ({
        id: e.id,
        title: e.title,
        items: e.items,
        amount: e.amount,
        currency: e.currency ?? 'INR',
        status: e.status,
        description: e.description,
        referenceId: e.referenceId,
        referenceType: e.referenceType,
        isDelegated: e.isDelegated ?? false,
        createdById: e.createdById,
        paidById: e.paidById,
        expenseDate: e.expenseDate,
        submittedById: e.submittedById ?? e.createdById,
        submittedOn: e.submittedOn,
        finalizedById: e.finalizedById,
        finalizedOn: e.finalizedOn,
        settledById: e.settledById,
        settledOn: e.settledOn,
        rejectedById: e.rejectedById,
        rejectedOn: e.rejectedOn,
        updatedById: e.updatedById,
        updatedOn: e.updatedOn,
        accountId: e.accountId,
        accountName: e.accountName,
        transactionRef: e.transactionRef,
        remarks: e.remarks,
        createdAt: e.createdAt,
        updatedAt: e.updatedAt,
        version: e.version ?? 0,
        deletedAt: e.deletedAt,
        userProfileId: e.userProfileId,
      }),
    });
  }

  if (await tableExists(source, 'earnings')) {
    await migrateSimple(source, target, {
      label: 'finance_earnings',
      sql: `SELECT * FROM earnings ORDER BY "createdAt", id`,
      table: 'finance_earnings',
      columns: [
        'id', 'category', 'amount', 'currency', 'status', 'description', 'source',
        'referenceId', 'referenceType', 'accountId', 'transactionId', 'earningDate',
        'createdById', 'receivedById', 'createdAt', 'updatedAt', 'version', 'deletedAt',
      ],
      conflict: 'id',
      mapRow: (e) => ({
        id: e.id,
        category: e.category,
        amount: e.amount,
        currency: e.currency,
        status: e.status,
        description: e.description,
        source: e.source,
        referenceId: e.referenceId,
        referenceType: e.referenceType,
        accountId: e.accountId,
        transactionId: e.transactionId,
        earningDate: e.earningDate,
        createdById: e.createdById,
        receivedById: e.receivedById,
        createdAt: e.createdAt,
        updatedAt: e.updatedAt,
        version: e.version ?? 0,
        deletedAt: e.deletedAt,
      }),
    });
  }
}

function mapMeeting(m) {
  const legacy = m.meetingSummary != null;
  if (legacy) {
    const start = m.meetingStartTime ?? m.createdAt;
    return {
      id: m.id,
      extMeetingId: m.extMeetingId,
      summary: m.meetingSummary,
      description: m.meetingDescription,
      type: m.meetingType,
      status: m.status,
      location: m.meetingLocation,
      startTime: start,
      endTime: m.meetingEndTime ?? start,
      agenda: m.meetingAgenda,
      outcomes: m.meetingOutcomes,
      attendees: m.attendees,
      hostEmail: m.creatorEmail,
      meetLink: m.extVideoConferenceLink,
      calendarLink: m.extHtmlLink,
      createdById: m.createdById,
      createdAt: m.createdAt,
      updatedAt: m.updatedAt,
      version: m.version ?? 0,
      deletedAt: m.deletedAt,
    };
  }
  return {
    id: m.id,
    extMeetingId: m.extMeetingId,
    summary: m.summary,
    description: m.description,
    type: m.type,
    status: m.status,
    location: m.location,
    startTime: m.startTime,
    endTime: m.endTime,
    agenda: m.agenda,
    outcomes: m.outcomes,
    attendees: m.attendees,
    hostEmail: m.hostEmail,
    meetLink: m.meetLink,
    calendarLink: m.calendarLink,
    createdById: m.createdById,
    createdAt: m.createdAt,
    updatedAt: m.updatedAt,
    version: m.version ?? 0,
    deletedAt: m.deletedAt,
  };
}

async function migrateMeetingsReportsDmsComments(source, target) {
  const meetingTable = await resolveLegacyTable(source, ['legacy_meetings', 'meetings']);
  if (meetingTable) {
    await migrateSimple(source, target, {
      label: `meetings from ${meetingTable}`,
      sql: `SELECT * FROM ${quoteIdent(meetingTable)} ORDER BY "createdAt", id`,
      table: 'meetings',
      columns: [
        'id', 'extMeetingId', 'summary', 'description', 'type', 'status', 'location',
        'startTime', 'endTime', 'agenda', 'outcomes', 'attendees', 'hostEmail',
        'meetLink', 'calendarLink', 'createdById', 'createdAt', 'updatedAt', 'version', 'deletedAt',
      ],
      conflict: 'id',
      mapRow: mapMeeting,
    });
  }

  const reportTable = await resolveLegacyTable(source, ['legacy_reports', 'reports']);
  if (reportTable) {
    await migrateSimple(source, target, {
      label: `reports from ${reportTable}`,
      sql: `SELECT * FROM ${quoteIdent(reportTable)} ORDER BY "createdAt", id`,
      table: 'reports',
      columns: [
        'id', 'reportCode', 'reportName', 'requestedById', 'status', 'parameters', 'needApproval',
        'approvedById', 'approvedAt', 'approverRoles', 'viewerRoles', 'docId', 'docVersion', 'workflowId',
        'createdAt', 'updatedAt',
      ],
      conflict: 'id',
      mapRow: (r) => ({
        id: String(r.id).slice(0, 20),
        reportCode: r.reportCode,
        reportName: r.reportName ?? '',
        requestedById: r.requestedById,
        status: r.status,
        parameters: r.parameters ?? null,
        needApproval: r.needApproval ?? false,
        approvedById: r.approvedById,
        approvedAt: r.approvedAt,
        approverRoles: r.approverRoles ?? r.approvers ?? [],
        viewerRoles: r.viewerRoles ?? r.viewers ?? [],
        docId: r.docId,
        docVersion: r.docVersion ?? 1,
        workflowId: r.workflowId,
        createdAt: r.createdAt,
        updatedAt: r.updatedAt,
      }),
    });
  }

  if (await tableExists(source, 'document_references')) {
    await migrateSimple(source, target, {
      label: 'dms_document_reference',
      sql: `SELECT * FROM document_references ORDER BY "createdAt", id`,
      table: 'dms_document_reference',
      columns: [
        'id', 'fileName', 'remotePath', 'publicToken', 'contentType', 'fileSize',
        'isPublic', 'uploadedById', 'createdAt', 'updatedAt',
      ],
      conflict: 'id',
      mapRow: (r) => ({
        id: r.id,
        fileName: r.fileName,
        remotePath: r.remotePath,
        publicToken: r.publicToken,
        contentType: r.contentType,
        fileSize: r.fileSize,
        isPublic: r.isPublic ?? false,
        uploadedById: r.uploadedById,
        createdAt: r.createdAt,
        updatedAt: r.createdAt,
      }),
    });
  }

  if (await tableExists(source, 'document_mappings')) {
    await migrateSimple(source, target, {
      label: 'dms_document_mapping',
      sql: `SELECT * FROM document_mappings ORDER BY "createdAt", id`,
      table: 'dms_document_mapping',
      columns: ['id', 'documentReferenceId', 'entityId', 'entityType', 'createdAt'],
      conflict: 'id',
      updateCols: [],
      mapRow: (m) => ({
        id: m.id,
        documentReferenceId: m.documentReferenceId,
        entityId: m.entityId,
        entityType: m.entityType,
        createdAt: m.createdAt,
      }),
    });
  }

  if (await tableExists(source, 'comments')) {
    await migrateSimple(source, target, {
      label: 'comment',
      sql: `
        SELECT c.*, TRIM(CONCAT_WS(' ', up."firstName", up."middleName", up."lastName")) AS "authorName"
        FROM comments c
        LEFT JOIN user_profiles up ON up.id = c."authorId"
        ORDER BY c."createdAt", c.id
      `,
      table: 'comment',
      columns: [
        'id', 'content', 'authorId', 'authorName', 'entityType', 'entityId',
        'parentId', 'deletedAt', 'version', 'createdAt', 'updatedAt',
      ],
      conflict: 'id',
      mapRow: (c) => ({
        id: c.id,
        content: String(c.content ?? '').slice(0, 512),
        authorId: c.authorId,
        authorName: c.authorName || null,
        entityType: c.entityType,
        entityId: c.entityId,
        parentId: c.parentId,
        deletedAt: c.deletedAt,
        version: c.version ?? 0,
        createdAt: c.createdAt,
        updatedAt: c.updatedAt,
      }),
    });
  }
}

function printRow(status, label, source, dest, note = '') {
  const s = source == null ? '—' : String(source);
  const t = dest == null ? '—' : String(dest);
  const suffix = note ? `  (${note})` : '';
  console.log(`${status.padEnd(5)} ${label.padEnd(48)} source=${s.padStart(8)} target=${t.padStart(8)}${suffix}`);
}

async function runIntegrityChecks(source, target) {
  logStep('Integrity checks');
  const failures = [];

  const countPairs = [
    ['user_profiles', 'user_profile'],
    ['phone_numbers', 'user_phone_number', { allowLte: true, note: 'deduped max 2/user' }],
    ['addresses', 'user_address', { allowLte: true, note: 'deduped by addressType' }],
    ['links', 'user_social_link', { allowLte: true, note: 'deduped by linkType' }],
    ['accounts', 'finance_accounts'],
    ['donations', 'finance_donations'],
    ['transactions', 'finance_transactions'],
    ['expenses', 'finance_expenses'],
    ['earnings', 'finance_earnings'],
    ['activities', 'activities'],
    ['document_references', 'dms_document_reference'],
    ['document_mappings', 'dms_document_mapping'],
    ['comments', 'comment'],
  ];

  const renamed = [
    [await resolveLegacyTable(source, ['legacy_projects', 'projects']), 'projects'],
    [await resolveLegacyTable(source, ['legacy_beneficiaries', 'beneficiaries']), 'project_beneficiaries'],
    [await resolveLegacyTable(source, ['legacy_meetings', 'meetings']), 'meetings'],
    [await resolveLegacyTable(source, ['legacy_reports', 'reports']), 'reports'],
  ];

  console.log('\n=== Row counts ===');
  for (const [src, dest, opts = {}] of [...countPairs, ...renamed.filter(([s]) => s)]) {
    if (!src) continue;
    const sc = await countTable(source, src);
    const tc = await countTable(target, dest);
    let status = 'SKIP';
    if (sc != null && tc != null) {
      if (sc === tc) status = 'OK';
      else if (opts.allowLte && tc <= sc) status = 'OK';
      else status = 'FAIL';
    }
    if (status === 'FAIL') failures.push(`${src} → ${dest}`);
    printRow(status, `${src} → ${dest}`, sc, tc, opts.note ?? '');
  }

  console.log('\n=== Finance totals ===');
  const totals = [
    [
      'account balance (not deleted)',
      `SELECT COALESCE(SUM(balance),0)::numeric AS v FROM accounts WHERE "deletedAt" IS NULL`,
      `SELECT COALESCE(SUM(balance),0)::numeric AS v FROM finance_accounts WHERE "deletedAt" IS NULL`,
      'accounts',
    ],
    [
      'donation amount (not deleted)',
      `SELECT COALESCE(SUM(amount),0)::numeric AS v FROM donations WHERE "deletedAt" IS NULL`,
      `SELECT COALESCE(SUM(amount),0)::numeric AS v FROM finance_donations WHERE "deletedAt" IS NULL`,
      'donations',
    ],
    [
      'completed transaction amount',
      `SELECT COALESCE(SUM(amount),0)::numeric AS v FROM transactions WHERE status = 'COMPLETED' AND "deletedAt" IS NULL`,
      `SELECT COALESCE(SUM(amount),0)::numeric AS v FROM finance_transactions WHERE status = 'COMPLETED' AND "deletedAt" IS NULL`,
      'transactions',
    ],
  ];
  for (const [label, srcSql, tgtSql, srcTable] of totals) {
    if (!(await tableExists(source, srcTable))) {
      printRow('SKIP', label, null, null);
      continue;
    }
    const sv = Number(await scalarCount(source, srcSql));
    const tv = Number(await scalarCount(target, tgtSql));
    const status = Math.abs(sv - tv) < 0.01 ? 'OK' : 'FAIL';
    if (status === 'FAIL') failures.push(`total: ${label}`);
    printRow(status, label, sv, tv);
  }

  console.log('\n=== Orphans / invariants (expect 0) ===');
  const integrity = [
    [
      'donations missing donor',
      `SELECT COUNT(*)::bigint AS c FROM finance_donations d
       LEFT JOIN finance_donors dn ON dn.id = d."donorId"
       WHERE d."donorId" IS NOT NULL AND dn.id IS NULL`,
    ],
    [
      'donations missing paid-to account',
      `SELECT COUNT(*)::bigint AS c FROM finance_donations d
       LEFT JOIN finance_accounts a ON a.id = d."paidToAccountId"
       WHERE d."paidToAccountId" IS NOT NULL AND a.id IS NULL`,
    ],
    [
      'expenses missing createdBy',
      `SELECT COUNT(*)::bigint AS c FROM finance_expenses e
       LEFT JOIN user_profile up ON up.id = e."createdById" WHERE up.id IS NULL`,
    ],
    [
      'transactions missing account',
      `SELECT COUNT(*)::bigint AS c FROM finance_transactions t
       LEFT JOIN finance_accounts a ON a.id = t."accountId"
       WHERE t."accountId" IS NOT NULL AND a.id IS NULL`,
    ],
    [
      'orphan activities',
      `SELECT COUNT(*)::bigint AS c FROM activities a
       LEFT JOIN projects p ON p.id = a."projectId" WHERE p.id IS NULL`,
    ],
    [
      'MEMBER donor id ≠ userProfileId',
      `SELECT COUNT(*)::bigint AS c FROM finance_donors
       WHERE type = 'MEMBER' AND (id IS DISTINCT FROM "userProfileId" OR "userProfileId" IS NULL)`,
    ],
    [
      'MEMBER donor id missing user_profile',
      `SELECT COUNT(*)::bigint AS c FROM finance_donors d
       LEFT JOIN user_profile up ON up.id = d.id
       WHERE d.type = 'MEMBER' AND up.id IS NULL`,
    ],
  ];
  for (const [label, sql] of integrity) {
    const c = await scalarCount(target, sql);
    const status = c === 0 ? 'OK' : 'FAIL';
    if (status === 'FAIL') failures.push(`integrity: ${label}`);
    printRow(status, label, null, c);
  }

  console.log('\n=== Skipped legacy tables (WARN) ===');
  for (const table of [
    'user_roles', 'api_keys', 'oauth_tokens', 'notifications', 'user_notifications', 'audit_logs',
    'workflow_instances', 'workflow_steps', 'workflow_tasks', 'task_assignments', 'user_fcm_tokens',
  ]) {
    const c = await countTable(source, table);
    if (c == null) continue;
    printRow(c > 0 ? 'WARN' : 'OK', table, c, null, c > 0 ? 'not migrated' : '');
  }

  console.log('\n=== Summary ===');
  if (failures.length) {
    console.log(`FAILED (${failures.length}):`);
    for (const f of failures) console.log(`  - ${f}`);
    return 1;
  }
  console.log('All checks passed.');
  return 0;
}

async function main() {
  const source = new Client({ connectionString: envUrl('SOURCE_DATABASE_URL') });
  const target = new Client({ connectionString: envUrl('DATABASE_URL') });
  console.log(`[legacy-etl] dryRun=${DRY_RUN} chunkSize=${CHUNK_SIZE}`);

  try {
    await source.connect();
    await target.connect();
    if (!(await tableExists(source, 'user_profiles'))) {
      throw new Error('SOURCE_DATABASE_URL missing user_profiles');
    }
    if (!(await tableExists(target, 'user_profile'))) {
      throw new Error('DATABASE_URL missing user_profile — run migrate:deploy first');
    }

    await migrateUsers(source, target);
    await migratePhones(source, target);
    await migrateAddresses(source, target);
    await migrateLinks(source, target);
    await migrateProjects(source, target);
    if (await tableExists(source, 'accounts')) await migrateAccounts(source, target);
    if (await tableExists(source, 'donations') || await tableExists(source, 'user_profiles')) {
      await migrateMemberDonors(source, target);
    }
    if (await tableExists(source, 'donations')) await migrateDonations(source, target);
    await migrateTxnsExpensesEarnings(source, target);
    await migrateMeetingsReportsDmsComments(source, target);

    const code = await runIntegrityChecks(source, target);
    if (DRY_RUN) console.log('\n[legacy-etl] dry run — no rows written.');
    process.exitCode = code;
  } finally {
    await source.end().catch(() => {});
    await target.end().catch(() => {});
  }
}

main().catch((err) => {
  console.error('[legacy-etl] failed:', err?.message || err);
  if (err?.cause) console.error('[legacy-etl] cause:', err.cause);
  if (err?.stack) console.error(err.stack);
  process.exitCode = 1;
});
