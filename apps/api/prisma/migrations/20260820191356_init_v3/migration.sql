-- CreateTable
CREATE TABLE "activities" (
    "id" TEXT NOT NULL,
    "projectId" VARCHAR(255) NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "description" TEXT,
    "scale" VARCHAR(20) NOT NULL,
    "type" VARCHAR(50) NOT NULL,
    "status" VARCHAR(20) NOT NULL,
    "priority" VARCHAR(20) NOT NULL,
    "startDate" TIMESTAMP(3),
    "endDate" TIMESTAMP(3),
    "actualStartDate" TIMESTAMP(3),
    "actualEndDate" TIMESTAMP(3),
    "location" VARCHAR(255),
    "venue" VARCHAR(255),
    "assignedTo" VARCHAR(255),
    "organizerId" VARCHAR(255),
    "parentActivityId" VARCHAR(255),
    "expectedParticipants" INTEGER,
    "actualParticipants" INTEGER,
    "estimatedCost" DECIMAL(15,2),
    "actualCost" DECIMAL(15,2),
    "currency" VARCHAR(3),
    "tags" VARCHAR(50)[],
    "metadata" JSONB,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "version" INTEGER NOT NULL DEFAULT 0,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "activities_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ops_assets" (
    "id" TEXT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "category" VARCHAR(50) NOT NULL,
    "serialNumber" VARCHAR(100),
    "location" VARCHAR(255),
    "status" VARCHAR(20) NOT NULL,
    "custodianUserId" VARCHAR(255),
    "projectId" VARCHAR(255),
    "expenseId" VARCHAR(255),
    "purchaseDate" DATE,
    "purchaseCost" DECIMAL(15,2),
    "currency" VARCHAR(3),
    "currentValue" DECIMAL(15,2),
    "depreciationMethodNotes" TEXT,
    "maintenanceNotes" TEXT,
    "createdById" VARCHAR(255),
    "updatedById" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "version" INTEGER NOT NULL DEFAULT 0,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "ops_assets_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ops_asset_custody_records" (
    "id" TEXT NOT NULL,
    "assetId" VARCHAR(255) NOT NULL,
    "custodianUserId" VARCHAR(255) NOT NULL,
    "assignedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "assignedById" VARCHAR(255),
    "returnedAt" TIMESTAMP(3),
    "returnedById" VARCHAR(255),
    "notes" TEXT,

    CONSTRAINT "ops_asset_custody_records_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "audit_entity_change_log" (
    "id" TEXT NOT NULL,
    "entityType" VARCHAR(30) NOT NULL,
    "entityId" VARCHAR(255) NOT NULL,
    "action" VARCHAR(20) NOT NULL,
    "userId" VARCHAR(255) NOT NULL,
    "userName" VARCHAR(255) NOT NULL,
    "oldValues" JSONB NOT NULL DEFAULT '{}',
    "newValues" JSONB NOT NULL DEFAULT '{}',
    "ipAddress" VARCHAR(45),
    "userAgent" VARCHAR(512),
    "traceId" VARCHAR(20),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "audit_entity_change_log_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "auth_apikey" (
    "id" TEXT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "apiKey" VARCHAR(255) NOT NULL,
    "apiKeyId" VARCHAR(50) NOT NULL,
    "permissions" TEXT[],
    "createdBy" VARCHAR(255),
    "expiresAt" TIMESTAMP(3),
    "lastUsedAt" TIMESTAMP(3),
    "ownerId" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "auth_apikey_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "auth_permission" (
    "id" TEXT NOT NULL,
    "key" VARCHAR(50) NOT NULL,
    "description" TEXT,
    "deletedAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "auth_permission_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "auth_role" (
    "id" TEXT NOT NULL,
    "key" VARCHAR(50) NOT NULL,
    "description" TEXT,
    "isShadow" BOOLEAN NOT NULL DEFAULT false,
    "deletedAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "auth_role_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "auth_role_permission" (
    "roleId" VARCHAR(50) NOT NULL,
    "permissionId" VARCHAR(50) NOT NULL,

    CONSTRAINT "auth_role_permission_pkey" PRIMARY KEY ("roleId","permissionId")
);

-- CreateTable
CREATE TABLE "auth_role_group" (
    "id" TEXT NOT NULL,
    "key" VARCHAR(50) NOT NULL,
    "description" TEXT,
    "isShadow" BOOLEAN NOT NULL DEFAULT false,
    "deletedAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "auth_role_group_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "auth_role_group_role" (
    "groupId" VARCHAR(50) NOT NULL,
    "roleId" VARCHAR(50) NOT NULL,

    CONSTRAINT "auth_role_group_role_pkey" PRIMARY KEY ("groupId","roleId")
);

-- CreateTable
CREATE TABLE "auth_user_role" (
    "id" TEXT NOT NULL,
    "idpSub" VARCHAR(100) NOT NULL,
    "ownerId" VARCHAR(100),
    "entityId" VARCHAR(100),
    "entityType" VARCHAR(30),
    "roleId" VARCHAR(50) NOT NULL,
    "sourceGroupId" VARCHAR(50),
    "grantedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "revokedAt" TIMESTAMP(3),
    "grantedBy" VARCHAR(100),
    "revokedBy" VARCHAR(100),
    "note" VARCHAR(512),

    CONSTRAINT "auth_user_role_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "auth_user_role_group" (
    "id" TEXT NOT NULL,
    "idpSub" VARCHAR(50) NOT NULL,
    "ownerId" VARCHAR(50),
    "entityId" VARCHAR(50),
    "entityType" VARCHAR(30),
    "groupId" VARCHAR(50) NOT NULL,
    "grantedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "revokedAt" TIMESTAMP(3),
    "grantedBy" VARCHAR(50),
    "revokedBy" VARCHAR(50),
    "note" VARCHAR(512),

    CONSTRAINT "auth_user_role_group_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "auth_user_permission" (
    "id" TEXT NOT NULL,
    "idpSub" VARCHAR(100) NOT NULL,
    "ownerId" VARCHAR(100),
    "entityId" VARCHAR(100),
    "entityType" VARCHAR(30),
    "permissionId" VARCHAR(50) NOT NULL,
    "grantedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "revokedAt" TIMESTAMP(3),
    "grantedBy" VARCHAR(100),
    "revokedBy" VARCHAR(100),
    "note" VARCHAR(512),

    CONSTRAINT "auth_user_permission_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ops_books" (
    "id" TEXT NOT NULL,
    "title" VARCHAR(255) NOT NULL,
    "author" VARCHAR(255) NOT NULL,
    "category" VARCHAR(50) NOT NULL,
    "subject" VARCHAR(50) NOT NULL,
    "classLevel" VARCHAR(50) NOT NULL,
    "isbn" VARCHAR(32),
    "location" VARCHAR(255),
    "status" VARCHAR(20) NOT NULL,
    "acquisitionType" VARCHAR(30) NOT NULL,
    "acquisitionNotes" TEXT,
    "holderUserId" VARCHAR(255),
    "holderGuestName" VARCHAR(255),
    "createdById" VARCHAR(255),
    "updatedById" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "version" INTEGER NOT NULL DEFAULT 0,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "ops_books_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ops_book_loan_records" (
    "id" TEXT NOT NULL,
    "bookId" VARCHAR(255) NOT NULL,
    "borrowerUserId" VARCHAR(255),
    "guestName" VARCHAR(255),
    "loanedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "dueDate" TIMESTAMP(3),
    "returnedAt" TIMESTAMP(3),
    "returnedById" VARCHAR(255),
    "notes" TEXT,

    CONSTRAINT "ops_book_loan_records_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "comment" (
    "id" TEXT NOT NULL,
    "content" VARCHAR(512) NOT NULL,
    "authorId" VARCHAR(100) NOT NULL,
    "authorName" VARCHAR(255),
    "entityType" VARCHAR(255) NOT NULL,
    "entityId" VARCHAR(100) NOT NULL,
    "parentId" VARCHAR(100),
    "deletedAt" TIMESTAMP(3),
    "version" INTEGER NOT NULL DEFAULT 0,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "comment_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "comment_mention" (
    "commentId" VARCHAR(100) NOT NULL,
    "mentionedUserId" VARCHAR(100) NOT NULL,
    "displayName" VARCHAR(255) NOT NULL,
    "email" VARCHAR(100) NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "comment_mention_pkey" PRIMARY KEY ("commentId","mentionedUserId")
);

-- CreateTable
CREATE TABLE "correspondence_notification" (
    "id" TEXT NOT NULL,
    "title" VARCHAR(255) NOT NULL,
    "body" TEXT NOT NULL,
    "type" VARCHAR(255) NOT NULL,
    "category" VARCHAR(255) NOT NULL,
    "priority" VARCHAR(255) NOT NULL,
    "actionUrl" VARCHAR(255),
    "actionType" VARCHAR(255),
    "actionData" JSONB,
    "referenceId" VARCHAR(255),
    "referenceType" VARCHAR(255),
    "dispatchId" VARCHAR(255),
    "imageUrl" VARCHAR(255),
    "icon" VARCHAR(255),
    "metadata" JSONB,
    "expiresAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "correspondence_notification_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "correspondence_user_notification" (
    "id" TEXT NOT NULL,
    "notificationId" VARCHAR(100) NOT NULL,
    "userId" VARCHAR(100) NOT NULL,
    "isRead" BOOLEAN NOT NULL DEFAULT false,
    "readAt" TIMESTAMP(3),
    "isArchived" BOOLEAN NOT NULL DEFAULT false,
    "archivedAt" TIMESTAMP(3),
    "isPushSent" BOOLEAN NOT NULL DEFAULT false,
    "pushSentAt" TIMESTAMP(3),
    "pushDelivered" BOOLEAN NOT NULL DEFAULT false,
    "pushError" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "correspondence_user_notification_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "correspondence_resource_subscription" (
    "id" TEXT NOT NULL,
    "subscriberType" VARCHAR(30) NOT NULL,
    "userId" VARCHAR(100),
    "userEmail" VARCHAR(100),
    "userName" VARCHAR(255),
    "roleName" VARCHAR(255),
    "resourceType" VARCHAR(255) NOT NULL,
    "resourceId" VARCHAR(100),
    "subscribedVia" VARCHAR(255) NOT NULL,
    "isActive" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "correspondence_resource_subscription_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "correspondence_subscription_channel" (
    "id" TEXT NOT NULL,
    "subscriptionId" VARCHAR(100) NOT NULL,
    "channel" VARCHAR(50) NOT NULL,
    "enabled" BOOLEAN NOT NULL DEFAULT true,
    "emailRole" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "correspondence_subscription_channel_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "cron_job_definition" (
    "id" TEXT NOT NULL,
    "name" VARCHAR(100) NOT NULL,
    "description" VARCHAR(255),
    "expression" VARCHAR(50) NOT NULL,
    "handler" VARCHAR(255) NOT NULL,
    "enabled" BOOLEAN NOT NULL DEFAULT true,
    "inputData" JSONB,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "cron_job_definition_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "custom_form" (
    "id" TEXT NOT NULL,
    "entityType" VARCHAR(100) NOT NULL,
    "key" VARCHAR(100) NOT NULL,
    "label" VARCHAR(255) NOT NULL,
    "description" VARCHAR(255),
    "status" VARCHAR(255) NOT NULL DEFAULT 'draft',
    "managePermissions" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "readPermissions" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "writePermissions" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "createdBy" VARCHAR(255),
    "publishedBy" VARCHAR(255),
    "disabledBy" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "custom_form_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "custom_form_field_definition" (
    "id" TEXT NOT NULL,
    "formId" VARCHAR(100) NOT NULL,
    "key" VARCHAR(100) NOT NULL,
    "label" VARCHAR(255) NOT NULL,
    "fieldType" VARCHAR(50) NOT NULL,
    "mandatory" BOOLEAN NOT NULL DEFAULT false,
    "isHidden" BOOLEAN NOT NULL DEFAULT false,
    "isEncrypted" BOOLEAN NOT NULL DEFAULT false,
    "enabled" BOOLEAN NOT NULL DEFAULT true,
    "sortOrder" INTEGER NOT NULL DEFAULT 0,
    "stepId" VARCHAR(100),
    "stepName" VARCHAR(255),
    "conditionJson" JSONB,
    "dependentOptionsJson" JSONB,
    "viewPermissions" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "validationRulesJson" JSONB,
    "createdBy" VARCHAR(255),
    "disabledBy" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "custom_form_field_definition_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "custom_form_submission" (
    "id" TEXT NOT NULL,
    "entityType" VARCHAR(30) NOT NULL,
    "entityId" VARCHAR(255) NOT NULL,
    "formId" VARCHAR(100) NOT NULL,
    "status" VARCHAR(255) NOT NULL DEFAULT 'draft',
    "submittedAt" TIMESTAMP(3),
    "submittedBy" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "custom_form_submission_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "custom_form_field_value" (
    "id" TEXT NOT NULL,
    "entityType" VARCHAR(30) NOT NULL,
    "entityId" VARCHAR(255) NOT NULL,
    "formId" VARCHAR(100) NOT NULL,
    "formSubmissionId" VARCHAR(100) NOT NULL,
    "fieldDefId" VARCHAR(100) NOT NULL,
    "value" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "custom_form_field_value_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "custom_form_field_value_history_entry" (
    "id" TEXT NOT NULL,
    "formFieldValueId" VARCHAR(100) NOT NULL,
    "entityType" VARCHAR(30) NOT NULL,
    "entityId" VARCHAR(255) NOT NULL,
    "formId" VARCHAR(100) NOT NULL,
    "fieldDefId" VARCHAR(100) NOT NULL,
    "oldValue" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "newValue" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "changedBy" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "custom_form_field_value_history_entry_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "dms_document_reference" (
    "id" TEXT NOT NULL,
    "fileName" TEXT NOT NULL,
    "remotePath" TEXT NOT NULL,
    "publicToken" TEXT,
    "contentType" TEXT NOT NULL,
    "fileSize" INTEGER,
    "isPublic" BOOLEAN NOT NULL DEFAULT false,
    "uploadedById" TEXT,
    "storageOwnerId" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "dms_document_reference_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "dms_document_mapping" (
    "id" TEXT NOT NULL,
    "documentReferenceId" TEXT NOT NULL,
    "entityId" TEXT NOT NULL,
    "entityType" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "dms_document_mapping_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "finance_accounts" (
    "id" TEXT NOT NULL,
    "name" VARCHAR(100) NOT NULL,
    "type" VARCHAR(30) NOT NULL,
    "ownerType" VARCHAR(20) NOT NULL,
    "currency" VARCHAR(3) NOT NULL,
    "status" VARCHAR(20) NOT NULL,
    "description" TEXT,
    "balance" DECIMAL(10,2) NOT NULL DEFAULT 0,
    "accountHolderName" VARCHAR(255),
    "accountHolderId" VARCHAR(255),
    "custodianUserIds" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "activatedOn" TIMESTAMP(3),
    "createdById" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "version" INTEGER NOT NULL DEFAULT 0,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "finance_accounts_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "finance_account_bank_invest_details" (
    "id" TEXT NOT NULL,
    "accountId" VARCHAR(255) NOT NULL,
    "bankAccountHolderName" VARCHAR(255),
    "bankName" VARCHAR(255),
    "bankBranch" VARCHAR(255),
    "bankAccountNumber" VARCHAR(100),
    "bankAccountType" VARCHAR(50),
    "IFSCNumber" VARCHAR(20),
    "maturityDate" DATE,
    "maturityAmount" DECIMAL(14,2),
    "investmentAmount" DECIMAL(14,2),
    "sourceAccountId" VARCHAR(255),
    "dematId" VARCHAR(100),
    "interestRate" DECIMAL(10,4),
    "interestPayingTerm" VARCHAR(30),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "finance_account_bank_invest_details_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "finance_account_upi_detail" (
    "id" TEXT NOT NULL,
    "accountId" VARCHAR(255) NOT NULL,
    "payeeName" VARCHAR(255),
    "upiId" VARCHAR(100),
    "mobileNumber" VARCHAR(50),
    "qrData" TEXT,
    "label" VARCHAR(100),
    "isPrimary" BOOLEAN NOT NULL DEFAULT false,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "finance_account_upi_detail_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "finance_donors" (
    "id" TEXT NOT NULL,
    "type" VARCHAR(20) NOT NULL,
    "status" VARCHAR(20) NOT NULL,
    "preferredAmount" DECIMAL(10,2),
    "statusEndDate" TIMESTAMP(3),
    "fullName" VARCHAR(200),
    "email" VARCHAR(100),
    "phoneCode" VARCHAR(10),
    "phoneNumber" VARCHAR(50),
    "userProfileId" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "version" INTEGER NOT NULL DEFAULT 0,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "finance_donors_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "finance_donations" (
    "id" TEXT NOT NULL,
    "type" VARCHAR(20) NOT NULL,
    "amount" DECIMAL(10,2) NOT NULL,
    "currency" VARCHAR(3) NOT NULL,
    "status" VARCHAR(20) NOT NULL,
    "donorId" VARCHAR(255),
    "startDate" TIMESTAMP(3),
    "endDate" TIMESTAMP(3),
    "raisedOn" TIMESTAMP(3) NOT NULL,
    "paidOn" TIMESTAMP(3),
    "confirmedById" VARCHAR(255),
    "confirmedOn" TIMESTAMP(3),
    "paymentMethod" VARCHAR(20),
    "paidToAccountId" VARCHAR(255),
    "forEventId" VARCHAR(255),
    "paidUsingUPI" VARCHAR(20),
    "isPaymentNotified" BOOLEAN DEFAULT false,
    "transactionRef" VARCHAR(255),
    "remarks" TEXT,
    "cancelletionReason" TEXT,
    "laterPaymentReason" TEXT,
    "paymentFailureDetail" TEXT,
    "additionalFields" JSONB,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "version" INTEGER NOT NULL DEFAULT 0,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "finance_donations_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "finance_transactions" (
    "id" TEXT NOT NULL,
    "transactionRef" TEXT NOT NULL,
    "type" VARCHAR(20) NOT NULL,
    "amount" DECIMAL(15,2) NOT NULL,
    "currency" VARCHAR(3) NOT NULL,
    "status" VARCHAR(20) NOT NULL,
    "referenceId" VARCHAR(255),
    "referenceType" VARCHAR(50),
    "description" TEXT NOT NULL,
    "metadata" JSONB,
    "transactionDate" TIMESTAMP(3) NOT NULL,
    "particulars" TEXT,
    "createdById" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "version" INTEGER NOT NULL DEFAULT 0,
    "deletedAt" TIMESTAMP(3),
    "refAccountId" VARCHAR(255),
    "accountId" VARCHAR(255),

    CONSTRAINT "finance_transactions_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "finance_expenses" (
    "id" TEXT NOT NULL,
    "title" VARCHAR(255) NOT NULL,
    "items" TEXT,
    "amount" DECIMAL(15,2) NOT NULL,
    "currency" VARCHAR(3) NOT NULL DEFAULT 'INR',
    "status" VARCHAR(20) NOT NULL,
    "description" TEXT,
    "referenceId" VARCHAR(255),
    "referenceType" VARCHAR(50),
    "isDelegated" BOOLEAN NOT NULL DEFAULT false,
    "createdById" VARCHAR(255) NOT NULL,
    "paidById" VARCHAR(255) NOT NULL,
    "expenseDate" TIMESTAMP(3) NOT NULL,
    "submittedById" VARCHAR(255),
    "submittedOn" TIMESTAMP(3),
    "finalizedById" VARCHAR(255),
    "finalizedOn" TIMESTAMP(3),
    "settledById" VARCHAR(255),
    "settledOn" TIMESTAMP(3),
    "rejectedById" VARCHAR(255),
    "rejectedOn" TIMESTAMP(3),
    "updatedById" VARCHAR(255),
    "updatedOn" TIMESTAMP(3),
    "accountId" VARCHAR(255),
    "accountName" VARCHAR(255),
    "transactionRef" VARCHAR(255),
    "remarks" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "version" INTEGER NOT NULL DEFAULT 0,
    "deletedAt" TIMESTAMP(3),
    "userProfileId" TEXT,

    CONSTRAINT "finance_expenses_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "finance_earnings" (
    "id" TEXT NOT NULL,
    "category" VARCHAR(30) NOT NULL,
    "amount" DECIMAL(10,2) NOT NULL,
    "currency" VARCHAR(3) NOT NULL,
    "status" VARCHAR(20) NOT NULL,
    "description" TEXT NOT NULL,
    "source" VARCHAR(100) NOT NULL,
    "referenceId" VARCHAR(255),
    "referenceType" VARCHAR(50),
    "accountId" VARCHAR(255),
    "transactionId" VARCHAR(255),
    "earningDate" TIMESTAMP(3),
    "createdById" VARCHAR(255),
    "receivedById" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "version" INTEGER NOT NULL DEFAULT 0,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "finance_earnings_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "json_store_document" (
    "id" TEXT NOT NULL,
    "key" TEXT NOT NULL,
    "namespace" TEXT NOT NULL,
    "payload" JSONB NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "json_store_document_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "meetings" (
    "id" TEXT NOT NULL,
    "extMeetingId" VARCHAR(255),
    "summary" VARCHAR(500) NOT NULL,
    "description" TEXT,
    "type" VARCHAR(20) NOT NULL,
    "status" VARCHAR(20) NOT NULL,
    "location" VARCHAR(500),
    "startTime" TIMESTAMP(3) NOT NULL,
    "endTime" TIMESTAMP(3) NOT NULL,
    "agenda" TEXT,
    "outcomes" TEXT,
    "attendees" TEXT,
    "hostEmail" VARCHAR(255),
    "meetLink" VARCHAR(1000),
    "calendarLink" VARCHAR(1000),
    "createdById" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "version" INTEGER NOT NULL DEFAULT 0,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "meetings_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "projects" (
    "id" TEXT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "description" TEXT NOT NULL,
    "code" VARCHAR(50) NOT NULL,
    "category" VARCHAR(50) NOT NULL,
    "status" VARCHAR(20) NOT NULL,
    "phase" VARCHAR(20) NOT NULL,
    "startDate" TIMESTAMP(3) NOT NULL,
    "endDate" TIMESTAMP(3),
    "actualEndDate" TIMESTAMP(3),
    "budget" DECIMAL(15,2) NOT NULL,
    "spentAmount" DECIMAL(15,2) NOT NULL DEFAULT 0,
    "currency" VARCHAR(3) NOT NULL,
    "location" VARCHAR(255),
    "targetBeneficiaryCount" INTEGER,
    "actualBeneficiaryCount" INTEGER,
    "managerId" VARCHAR(255) NOT NULL,
    "sponsorId" VARCHAR(255),
    "tags" VARCHAR(50)[],
    "isPublic" BOOLEAN NOT NULL DEFAULT false,
    "metadata" JSONB,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "version" INTEGER NOT NULL DEFAULT 0,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "projects_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "project_beneficiaries" (
    "id" TEXT NOT NULL,
    "projectId" VARCHAR(255) NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "type" VARCHAR(30) NOT NULL,
    "gender" VARCHAR(20),
    "age" INTEGER,
    "dateOfBirth" TIMESTAMP(3),
    "contactNumber" VARCHAR(20),
    "email" VARCHAR(255),
    "address" TEXT,
    "location" VARCHAR(255),
    "category" VARCHAR(100),
    "enrollmentDate" TIMESTAMP(3) NOT NULL,
    "exitDate" TIMESTAMP(3),
    "status" VARCHAR(20) NOT NULL,
    "benefitsReceived" VARCHAR(100)[],
    "notes" TEXT,
    "metadata" JSONB,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "project_beneficiaries_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "project_goals" (
    "id" TEXT NOT NULL,
    "projectId" VARCHAR(255) NOT NULL,
    "title" VARCHAR(255) NOT NULL,
    "description" TEXT,
    "targetValue" DECIMAL(15,2),
    "targetUnit" VARCHAR(50),
    "currentValue" DECIMAL(15,2) NOT NULL DEFAULT 0,
    "deadline" TIMESTAMP(3),
    "priority" VARCHAR(20) NOT NULL,
    "status" VARCHAR(30) NOT NULL,
    "weight" DECIMAL(5,4),
    "dependencies" TEXT[],
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "version" INTEGER NOT NULL DEFAULT 0,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "project_goals_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "project_milestones" (
    "id" TEXT NOT NULL,
    "projectId" VARCHAR(255) NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "description" TEXT,
    "targetDate" TIMESTAMP(3) NOT NULL,
    "actualDate" TIMESTAMP(3),
    "status" VARCHAR(20) NOT NULL,
    "importance" VARCHAR(20) NOT NULL,
    "dependencies" TEXT[],
    "notes" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "project_milestones_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "project_team_members" (
    "id" TEXT NOT NULL,
    "projectId" VARCHAR(255) NOT NULL,
    "userId" VARCHAR(255) NOT NULL,
    "role" VARCHAR(30) NOT NULL,
    "responsibilities" TEXT,
    "startDate" TIMESTAMP(3) NOT NULL,
    "endDate" TIMESTAMP(3),
    "hoursAllocated" DECIMAL(10,2),
    "isActive" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "project_team_members_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "project_risks" (
    "id" TEXT NOT NULL,
    "projectId" VARCHAR(255) NOT NULL,
    "title" VARCHAR(255) NOT NULL,
    "description" TEXT,
    "category" VARCHAR(30) NOT NULL,
    "severity" VARCHAR(20) NOT NULL,
    "probability" VARCHAR(20) NOT NULL,
    "status" VARCHAR(20) NOT NULL,
    "impact" TEXT,
    "mitigationPlan" TEXT,
    "ownerId" VARCHAR(255),
    "identifiedDate" TIMESTAMP(3) NOT NULL,
    "resolvedDate" TIMESTAMP(3),
    "notes" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "project_risks_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "reports" (
    "id" VARCHAR(20) NOT NULL,
    "reportCode" VARCHAR(50) NOT NULL,
    "reportName" VARCHAR(100) NOT NULL DEFAULT '',
    "requestedById" VARCHAR(255),
    "status" VARCHAR(20) NOT NULL,
    "parameters" JSONB,
    "needApproval" BOOLEAN NOT NULL DEFAULT false,
    "approvedById" VARCHAR(255),
    "approvedAt" TIMESTAMP(3),
    "approverRoles" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "viewerRoles" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "docId" VARCHAR(255),
    "docVersion" INTEGER NOT NULL DEFAULT 1,
    "workflowId" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "reports_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "requests" (
    "id" VARCHAR(100) NOT NULL,
    "type" VARCHAR(50) NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "formKey" VARCHAR(200) NOT NULL,
    "formSubmissionId" VARCHAR(100),
    "status" VARCHAR(30) NOT NULL,
    "initiatedById" VARCHAR(100),
    "initiatedForId" VARCHAR(100),
    "assigneeId" VARCHAR(100),
    "claimedById" VARCHAR(100),
    "claimedAt" TIMESTAMP(3),
    "executorRoles" JSONB NOT NULL DEFAULT '[]',
    "executorGroups" JSONB NOT NULL DEFAULT '[]',
    "executorPermissions" JSONB NOT NULL DEFAULT '[]',
    "approverRoles" JSONB NOT NULL DEFAULT '[]',
    "approverGroups" JSONB NOT NULL DEFAULT '[]',
    "approverPermissions" JSONB NOT NULL DEFAULT '[]',
    "needApproval" BOOLEAN NOT NULL DEFAULT false,
    "completedAt" TIMESTAMP(3),
    "decisionNote" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "requests_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "request_events" (
    "id" TEXT NOT NULL,
    "requestId" VARCHAR(100) NOT NULL,
    "type" VARCHAR(50) NOT NULL,
    "actorId" VARCHAR(100),
    "payload" JSONB NOT NULL DEFAULT '{}',
    "occurredAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "request_events_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "token_vault_o_auth_account" (
    "id" TEXT NOT NULL,
    "provider" VARCHAR(50) NOT NULL,
    "email" VARCHAR(255) NOT NULL,
    "externalId" VARCHAR(255),
    "name" VARCHAR(255),
    "givenName" VARCHAR(255),
    "familyName" VARCHAR(255),
    "pictureUrl" VARCHAR(255),
    "locale" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "token_vault_o_auth_account_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "token_vault_o_auth_token" (
    "id" TEXT NOT NULL,
    "accountId" VARCHAR(100) NOT NULL,
    "clientId" VARCHAR(255) NOT NULL,
    "provider" VARCHAR(50) NOT NULL,
    "email" VARCHAR(255) NOT NULL,
    "ownerSub" VARCHAR(255),
    "accessToken" TEXT NOT NULL,
    "refreshToken" TEXT,
    "tokenType" VARCHAR(50),
    "expiresAt" TIMESTAMP(3),
    "scope" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "token_vault_o_auth_token_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "user_profile" (
    "id" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "idpSub" TEXT,
    "title" VARCHAR(50),
    "firstName" VARCHAR(50) NOT NULL,
    "middleName" VARCHAR(50),
    "lastName" VARCHAR(50) NOT NULL,
    "dateOfBirth" TIMESTAMP(3),
    "gender" VARCHAR(10),
    "about" TEXT,
    "picture" TEXT,
    "roleKeys" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "status" VARCHAR(20) NOT NULL,
    "isPublic" BOOLEAN NOT NULL DEFAULT true,
    "isSameAddress" BOOLEAN,
    "isProfileComplete" BOOLEAN NOT NULL DEFAULT false,
    "version" INTEGER NOT NULL DEFAULT 0,
    "createdById" VARCHAR(255),
    "updatedById" VARCHAR(255),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "user_profile_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "user_phone_number" (
    "id" TEXT NOT NULL,
    "userId" VARCHAR(100) NOT NULL,
    "phoneCode" VARCHAR(10) NOT NULL,
    "phoneNumber" VARCHAR(50) NOT NULL,
    "hidden" BOOLEAN NOT NULL DEFAULT false,
    "isPrimary" BOOLEAN NOT NULL,

    CONSTRAINT "user_phone_number_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "user_address" (
    "id" TEXT NOT NULL,
    "userId" VARCHAR(100) NOT NULL,
    "addressType" VARCHAR(20) NOT NULL,
    "addressLine1" VARCHAR(255) NOT NULL,
    "addressLine2" VARCHAR(255),
    "addressLine3" VARCHAR(255),
    "hometown" VARCHAR(100) NOT NULL,
    "zipCode" VARCHAR(10) NOT NULL,
    "state" VARCHAR(50) NOT NULL,
    "district" VARCHAR(50) NOT NULL,
    "country" VARCHAR(50) NOT NULL,

    CONSTRAINT "user_address_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "user_social_link" (
    "id" TEXT NOT NULL,
    "userId" VARCHAR(100) NOT NULL,
    "linkName" VARCHAR(50) NOT NULL,
    "linkType" VARCHAR(50) NOT NULL,
    "linkValue" VARCHAR(255) NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "user_social_link_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "activities_projectId_idx" ON "activities"("projectId");

-- CreateIndex
CREATE INDEX "activities_status_idx" ON "activities"("status");

-- CreateIndex
CREATE INDEX "activities_scale_idx" ON "activities"("scale");

-- CreateIndex
CREATE INDEX "activities_type_idx" ON "activities"("type");

-- CreateIndex
CREATE INDEX "activities_assignedTo_idx" ON "activities"("assignedTo");

-- CreateIndex
CREATE INDEX "activities_organizerId_idx" ON "activities"("organizerId");

-- CreateIndex
CREATE INDEX "activities_parentActivityId_idx" ON "activities"("parentActivityId");

-- CreateIndex
CREATE INDEX "ops_assets_status_idx" ON "ops_assets"("status");

-- CreateIndex
CREATE INDEX "ops_assets_category_idx" ON "ops_assets"("category");

-- CreateIndex
CREATE INDEX "ops_assets_custodianUserId_idx" ON "ops_assets"("custodianUserId");

-- CreateIndex
CREATE INDEX "ops_assets_projectId_idx" ON "ops_assets"("projectId");

-- CreateIndex
CREATE INDEX "ops_assets_deletedAt_idx" ON "ops_assets"("deletedAt");

-- CreateIndex
CREATE INDEX "ops_asset_custody_records_assetId_assignedAt_idx" ON "ops_asset_custody_records"("assetId", "assignedAt");

-- CreateIndex
CREATE UNIQUE INDEX "auth_apikey_apiKeyId_key" ON "auth_apikey"("apiKeyId");

-- CreateIndex
CREATE INDEX "auth_apikey_ownerId_idx" ON "auth_apikey"("ownerId");

-- CreateIndex
CREATE UNIQUE INDEX "auth_permission_key_key" ON "auth_permission"("key");

-- CreateIndex
CREATE UNIQUE INDEX "auth_role_key_key" ON "auth_role"("key");

-- CreateIndex
CREATE UNIQUE INDEX "auth_role_group_key_key" ON "auth_role_group"("key");

-- CreateIndex
CREATE INDEX "auth_user_role_idpSub_idx" ON "auth_user_role"("idpSub");

-- CreateIndex
CREATE INDEX "auth_user_role_idpSub_ownerId_idx" ON "auth_user_role"("idpSub", "ownerId");

-- CreateIndex
CREATE INDEX "auth_user_role_idpSub_entityType_entityId_idx" ON "auth_user_role"("idpSub", "entityType", "entityId");

-- CreateIndex
CREATE INDEX "auth_user_role_group_idpSub_idx" ON "auth_user_role_group"("idpSub");

-- CreateIndex
CREATE INDEX "auth_user_role_group_idpSub_ownerId_idx" ON "auth_user_role_group"("idpSub", "ownerId");

-- CreateIndex
CREATE INDEX "auth_user_role_group_idpSub_entityType_entityId_idx" ON "auth_user_role_group"("idpSub", "entityType", "entityId");

-- CreateIndex
CREATE INDEX "auth_user_permission_idpSub_idx" ON "auth_user_permission"("idpSub");

-- CreateIndex
CREATE INDEX "auth_user_permission_idpSub_ownerId_idx" ON "auth_user_permission"("idpSub", "ownerId");

-- CreateIndex
CREATE INDEX "auth_user_permission_idpSub_entityType_entityId_idx" ON "auth_user_permission"("idpSub", "entityType", "entityId");

-- CreateIndex
CREATE INDEX "ops_books_status_idx" ON "ops_books"("status");

-- CreateIndex
CREATE INDEX "ops_books_category_idx" ON "ops_books"("category");

-- CreateIndex
CREATE INDEX "ops_books_subject_idx" ON "ops_books"("subject");

-- CreateIndex
CREATE INDEX "ops_books_classLevel_idx" ON "ops_books"("classLevel");

-- CreateIndex
CREATE INDEX "ops_books_author_idx" ON "ops_books"("author");

-- CreateIndex
CREATE INDEX "ops_books_holderUserId_idx" ON "ops_books"("holderUserId");

-- CreateIndex
CREATE INDEX "ops_books_deletedAt_idx" ON "ops_books"("deletedAt");

-- CreateIndex
CREATE INDEX "ops_book_loan_records_bookId_loanedAt_idx" ON "ops_book_loan_records"("bookId", "loanedAt");

-- CreateIndex
CREATE INDEX "corr_notification_type_idx" ON "correspondence_notification"("type");

-- CreateIndex
CREATE INDEX "corr_notification_category_idx" ON "correspondence_notification"("category");

-- CreateIndex
CREATE INDEX "corr_notification_createdAt_idx" ON "correspondence_notification"("createdAt");

-- CreateIndex
CREATE INDEX "corr_notification_ref_idx" ON "correspondence_notification"("referenceId", "referenceType");

-- CreateIndex
CREATE INDEX "corr_notification_dispatchId_idx" ON "correspondence_notification"("dispatchId");

-- CreateIndex
CREATE INDEX "corr_userNotification_userId_idx" ON "correspondence_user_notification"("userId");

-- CreateIndex
CREATE INDEX "corr_userNotification_notificationId_idx" ON "correspondence_user_notification"("notificationId");

-- CreateIndex
CREATE INDEX "corr_userNotification_userId_isRead_idx" ON "correspondence_user_notification"("userId", "isRead");

-- CreateIndex
CREATE INDEX "corr_userNotification_userId_isArchived_idx" ON "correspondence_user_notification"("userId", "isArchived");

-- CreateIndex
CREATE INDEX "corr_userNotification_userId_createdAt_idx" ON "correspondence_user_notification"("userId", "createdAt");

-- CreateIndex
CREATE UNIQUE INDEX "correspondence_user_notification_notificationId_userId_key" ON "correspondence_user_notification"("notificationId", "userId");

-- CreateIndex
CREATE INDEX "corr_subscription_resource_active_idx" ON "correspondence_resource_subscription"("resourceType", "resourceId", "isActive");

-- CreateIndex
CREATE INDEX "corr_subscription_userId_active_idx" ON "correspondence_resource_subscription"("userId", "isActive");

-- CreateIndex
CREATE INDEX "corr_subscription_roleName_active_idx" ON "correspondence_resource_subscription"("roleName", "isActive");

-- CreateIndex
CREATE INDEX "corr_subscription_isActive_updatedAt_idx" ON "correspondence_resource_subscription"("isActive", "updatedAt");

-- CreateIndex
CREATE UNIQUE INDEX "correspondence_resource_subscription_subscriberType_userId__key" ON "correspondence_resource_subscription"("subscriberType", "userId", "roleName", "resourceType", "resourceId");

-- CreateIndex
CREATE INDEX "corr_subscriptionChannel_subscriptionId_idx" ON "correspondence_subscription_channel"("subscriptionId");

-- CreateIndex
CREATE UNIQUE INDEX "correspondence_subscription_channel_subscriptionId_channel_key" ON "correspondence_subscription_channel"("subscriptionId", "channel");

-- CreateIndex
CREATE UNIQUE INDEX "cron_job_definition_name_key" ON "cron_job_definition"("name");

-- CreateIndex
CREATE INDEX "custom_form_entityType_status_idx" ON "custom_form"("entityType", "status");

-- CreateIndex
CREATE UNIQUE INDEX "custom_form_entityType_key_key" ON "custom_form"("entityType", "key");

-- CreateIndex
CREATE INDEX "custom_form_field_definition_formId_enabled_idx" ON "custom_form_field_definition"("formId", "enabled");

-- CreateIndex
CREATE UNIQUE INDEX "custom_form_field_definition_formId_key_key" ON "custom_form_field_definition"("formId", "key");

-- CreateIndex
CREATE INDEX "custom_form_submission_entityType_entityId_idx" ON "custom_form_submission"("entityType", "entityId");

-- CreateIndex
CREATE UNIQUE INDEX "custom_form_submission_entityType_entityId_formId_key" ON "custom_form_submission"("entityType", "entityId", "formId");

-- CreateIndex
CREATE INDEX "custom_form_field_value_entityType_entityId_formId_idx" ON "custom_form_field_value"("entityType", "entityId", "formId");

-- CreateIndex
CREATE UNIQUE INDEX "custom_form_field_value_entityType_entityId_formId_fieldDef_key" ON "custom_form_field_value"("entityType", "entityId", "formId", "fieldDefId");

-- CreateIndex
CREATE INDEX "custom_form_field_value_history_entry_entityType_entityId_f_idx" ON "custom_form_field_value_history_entry"("entityType", "entityId", "formId");

-- CreateIndex
CREATE INDEX "finance_accounts_type_status_idx" ON "finance_accounts"("type", "status");

-- CreateIndex
CREATE INDEX "finance_accounts_accountHolderId_idx" ON "finance_accounts"("accountHolderId");

-- CreateIndex
CREATE INDEX "finance_accounts_accountHolderId_type_status_idx" ON "finance_accounts"("accountHolderId", "type", "status");

-- CreateIndex
CREATE UNIQUE INDEX "finance_account_bank_invest_details_accountId_key" ON "finance_account_bank_invest_details"("accountId");

-- CreateIndex
CREATE INDEX "finance_account_bank_invest_details_sourceAccountId_idx" ON "finance_account_bank_invest_details"("sourceAccountId");

-- CreateIndex
CREATE INDEX "finance_account_upi_detail_accountId_idx" ON "finance_account_upi_detail"("accountId");

-- CreateIndex
CREATE UNIQUE INDEX "finance_donors_email_key" ON "finance_donors"("email");

-- CreateIndex
CREATE UNIQUE INDEX "finance_donors_userProfileId_key" ON "finance_donors"("userProfileId");

-- CreateIndex
CREATE INDEX "finance_donors_type_status_idx" ON "finance_donors"("type", "status");

-- CreateIndex
CREATE INDEX "finance_donations_donorId_status_idx" ON "finance_donations"("donorId", "status");

-- CreateIndex
CREATE INDEX "finance_donations_type_status_idx" ON "finance_donations"("type", "status");

-- CreateIndex
CREATE INDEX "finance_donations_raisedOn_idx" ON "finance_donations"("raisedOn");

-- CreateIndex
CREATE INDEX "finance_transactions_accountId_createdAt_id_idx" ON "finance_transactions"("accountId", "createdAt", "id");

-- CreateIndex
CREATE INDEX "finance_transactions_type_status_idx" ON "finance_transactions"("type", "status");

-- CreateIndex
CREATE INDEX "finance_transactions_referenceId_referenceType_idx" ON "finance_transactions"("referenceId", "referenceType");

-- CreateIndex
CREATE INDEX "finance_expenses_status_idx" ON "finance_expenses"("status");

-- CreateIndex
CREATE INDEX "finance_expenses_createdById_idx" ON "finance_expenses"("createdById");

-- CreateIndex
CREATE INDEX "finance_expenses_paidById_idx" ON "finance_expenses"("paidById");

-- CreateIndex
CREATE INDEX "finance_expenses_paidById_status_idx" ON "finance_expenses"("paidById", "status");

-- CreateIndex
CREATE INDEX "finance_expenses_referenceId_referenceType_idx" ON "finance_expenses"("referenceId", "referenceType");

-- CreateIndex
CREATE UNIQUE INDEX "finance_earnings_transactionId_key" ON "finance_earnings"("transactionId");

-- CreateIndex
CREATE INDEX "finance_earnings_category_status_idx" ON "finance_earnings"("category", "status");

-- CreateIndex
CREATE INDEX "finance_earnings_source_idx" ON "finance_earnings"("source");

-- CreateIndex
CREATE INDEX "json_store_namespace_idx" ON "json_store_document"("namespace");

-- CreateIndex
CREATE UNIQUE INDEX "json_store_document_key_namespace_key" ON "json_store_document"("key", "namespace");

-- CreateIndex
CREATE UNIQUE INDEX "meetings_extMeetingId_key" ON "meetings"("extMeetingId");

-- CreateIndex
CREATE INDEX "meetings_type_status_idx" ON "meetings"("type", "status");

-- CreateIndex
CREATE INDEX "meetings_extMeetingId_idx" ON "meetings"("extMeetingId");

-- CreateIndex
CREATE INDEX "meetings_startTime_endTime_idx" ON "meetings"("startTime", "endTime");

-- CreateIndex
CREATE INDEX "meetings_createdById_idx" ON "meetings"("createdById");

-- CreateIndex
CREATE UNIQUE INDEX "projects_code_key" ON "projects"("code");

-- CreateIndex
CREATE INDEX "projects_status_idx" ON "projects"("status");

-- CreateIndex
CREATE INDEX "projects_category_idx" ON "projects"("category");

-- CreateIndex
CREATE INDEX "projects_phase_idx" ON "projects"("phase");

-- CreateIndex
CREATE INDEX "projects_managerId_idx" ON "projects"("managerId");

-- CreateIndex
CREATE INDEX "projects_code_idx" ON "projects"("code");

-- CreateIndex
CREATE INDEX "projects_isPublic_idx" ON "projects"("isPublic");

-- CreateIndex
CREATE INDEX "project_beneficiaries_projectId_idx" ON "project_beneficiaries"("projectId");

-- CreateIndex
CREATE INDEX "project_beneficiaries_status_idx" ON "project_beneficiaries"("status");

-- CreateIndex
CREATE INDEX "project_beneficiaries_type_idx" ON "project_beneficiaries"("type");

-- CreateIndex
CREATE INDEX "project_beneficiaries_enrollmentDate_idx" ON "project_beneficiaries"("enrollmentDate");

-- CreateIndex
CREATE INDEX "project_goals_projectId_idx" ON "project_goals"("projectId");

-- CreateIndex
CREATE INDEX "project_goals_status_idx" ON "project_goals"("status");

-- CreateIndex
CREATE INDEX "project_goals_priority_idx" ON "project_goals"("priority");

-- CreateIndex
CREATE INDEX "project_milestones_projectId_idx" ON "project_milestones"("projectId");

-- CreateIndex
CREATE INDEX "project_milestones_status_idx" ON "project_milestones"("status");

-- CreateIndex
CREATE INDEX "project_milestones_targetDate_idx" ON "project_milestones"("targetDate");

-- CreateIndex
CREATE INDEX "project_team_members_projectId_idx" ON "project_team_members"("projectId");

-- CreateIndex
CREATE INDEX "project_team_members_userId_idx" ON "project_team_members"("userId");

-- CreateIndex
CREATE INDEX "project_team_members_isActive_idx" ON "project_team_members"("isActive");

-- CreateIndex
CREATE INDEX "project_risks_projectId_idx" ON "project_risks"("projectId");

-- CreateIndex
CREATE INDEX "project_risks_status_idx" ON "project_risks"("status");

-- CreateIndex
CREATE INDEX "project_risks_severity_idx" ON "project_risks"("severity");

-- CreateIndex
CREATE INDEX "project_risks_category_idx" ON "project_risks"("category");

-- CreateIndex
CREATE INDEX "reports_reportCode_idx" ON "reports"("reportCode");

-- CreateIndex
CREATE INDEX "reports_requestedById_idx" ON "reports"("requestedById");

-- CreateIndex
CREATE INDEX "reports_status_idx" ON "reports"("status");

-- CreateIndex
CREATE INDEX "requests_type_status_idx" ON "requests"("type", "status");

-- CreateIndex
CREATE INDEX "requests_status_idx" ON "requests"("status");

-- CreateIndex
CREATE INDEX "requests_initiatedById_createdAt_idx" ON "requests"("initiatedById", "createdAt");

-- CreateIndex
CREATE INDEX "requests_initiatedForId_idx" ON "requests"("initiatedForId");

-- CreateIndex
CREATE INDEX "requests_assigneeId_status_idx" ON "requests"("assigneeId", "status");

-- CreateIndex
CREATE INDEX "requests_claimedById_status_idx" ON "requests"("claimedById", "status");

-- CreateIndex
CREATE INDEX "request_events_requestId_occurredAt_idx" ON "request_events"("requestId", "occurredAt");

-- CreateIndex
CREATE INDEX "token_vault_o_auth_account_externalId_idx" ON "token_vault_o_auth_account"("externalId");

-- CreateIndex
CREATE INDEX "token_vault_o_auth_account_deletedAt_idx" ON "token_vault_o_auth_account"("deletedAt");

-- CreateIndex
CREATE UNIQUE INDEX "token_vault_o_auth_account_provider_email_key" ON "token_vault_o_auth_account"("provider", "email");

-- CreateIndex
CREATE INDEX "token_vault_o_auth_token_ownerSub_idx" ON "token_vault_o_auth_token"("ownerSub");

-- CreateIndex
CREATE INDEX "token_vault_o_auth_token_provider_email_idx" ON "token_vault_o_auth_token"("provider", "email");

-- CreateIndex
CREATE UNIQUE INDEX "token_vault_o_auth_token_accountId_clientId_key" ON "token_vault_o_auth_token"("accountId", "clientId");

-- CreateIndex
CREATE UNIQUE INDEX "user_profile_email_key" ON "user_profile"("email");

-- CreateIndex
CREATE UNIQUE INDEX "user_profile_idpSub_key" ON "user_profile"("idpSub");

-- CreateIndex
CREATE UNIQUE INDEX "user_phone_number_userId_isPrimary_key" ON "user_phone_number"("userId", "isPrimary");

-- CreateIndex
CREATE UNIQUE INDEX "user_address_userId_addressType_key" ON "user_address"("userId", "addressType");

-- CreateIndex
CREATE UNIQUE INDEX "user_social_link_userId_linkType_key" ON "user_social_link"("userId", "linkType");

-- AddForeignKey
ALTER TABLE "activities" ADD CONSTRAINT "activities_projectId_fkey" FOREIGN KEY ("projectId") REFERENCES "projects"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "activities" ADD CONSTRAINT "activities_assignedTo_fkey" FOREIGN KEY ("assignedTo") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "activities" ADD CONSTRAINT "activities_organizerId_fkey" FOREIGN KEY ("organizerId") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "activities" ADD CONSTRAINT "activities_parentActivityId_fkey" FOREIGN KEY ("parentActivityId") REFERENCES "activities"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ops_asset_custody_records" ADD CONSTRAINT "ops_asset_custody_records_assetId_fkey" FOREIGN KEY ("assetId") REFERENCES "ops_assets"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auth_role_permission" ADD CONSTRAINT "auth_role_permission_roleId_fkey" FOREIGN KEY ("roleId") REFERENCES "auth_role"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auth_role_permission" ADD CONSTRAINT "auth_role_permission_permissionId_fkey" FOREIGN KEY ("permissionId") REFERENCES "auth_permission"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auth_role_group_role" ADD CONSTRAINT "auth_role_group_role_groupId_fkey" FOREIGN KEY ("groupId") REFERENCES "auth_role_group"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auth_role_group_role" ADD CONSTRAINT "auth_role_group_role_roleId_fkey" FOREIGN KEY ("roleId") REFERENCES "auth_role"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auth_user_role" ADD CONSTRAINT "auth_user_role_roleId_fkey" FOREIGN KEY ("roleId") REFERENCES "auth_role"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auth_user_role" ADD CONSTRAINT "auth_user_role_sourceGroupId_fkey" FOREIGN KEY ("sourceGroupId") REFERENCES "auth_role_group"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auth_user_role_group" ADD CONSTRAINT "auth_user_role_group_groupId_fkey" FOREIGN KEY ("groupId") REFERENCES "auth_role_group"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auth_user_permission" ADD CONSTRAINT "auth_user_permission_permissionId_fkey" FOREIGN KEY ("permissionId") REFERENCES "auth_permission"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ops_book_loan_records" ADD CONSTRAINT "ops_book_loan_records_bookId_fkey" FOREIGN KEY ("bookId") REFERENCES "ops_books"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "comment" ADD CONSTRAINT "comment_parentId_fkey" FOREIGN KEY ("parentId") REFERENCES "comment"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "comment_mention" ADD CONSTRAINT "comment_mention_commentId_fkey" FOREIGN KEY ("commentId") REFERENCES "comment"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "correspondence_user_notification" ADD CONSTRAINT "correspondence_user_notification_notificationId_fkey" FOREIGN KEY ("notificationId") REFERENCES "correspondence_notification"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "correspondence_subscription_channel" ADD CONSTRAINT "correspondence_subscription_channel_subscriptionId_fkey" FOREIGN KEY ("subscriptionId") REFERENCES "correspondence_resource_subscription"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "custom_form_field_definition" ADD CONSTRAINT "custom_form_field_definition_formId_fkey" FOREIGN KEY ("formId") REFERENCES "custom_form"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "custom_form_submission" ADD CONSTRAINT "custom_form_submission_formId_fkey" FOREIGN KEY ("formId") REFERENCES "custom_form"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "custom_form_field_value" ADD CONSTRAINT "custom_form_field_value_fieldDefId_fkey" FOREIGN KEY ("fieldDefId") REFERENCES "custom_form_field_definition"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "custom_form_field_value" ADD CONSTRAINT "custom_form_field_value_formSubmissionId_fkey" FOREIGN KEY ("formSubmissionId") REFERENCES "custom_form_submission"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "custom_form_field_value_history_entry" ADD CONSTRAINT "custom_form_field_value_history_entry_formFieldValueId_fkey" FOREIGN KEY ("formFieldValueId") REFERENCES "custom_form_field_value"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "dms_document_mapping" ADD CONSTRAINT "dms_document_mapping_documentReferenceId_fkey" FOREIGN KEY ("documentReferenceId") REFERENCES "dms_document_reference"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_accounts" ADD CONSTRAINT "finance_accounts_accountHolderId_fkey" FOREIGN KEY ("accountHolderId") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_accounts" ADD CONSTRAINT "finance_accounts_createdById_fkey" FOREIGN KEY ("createdById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_account_bank_invest_details" ADD CONSTRAINT "finance_account_bank_invest_details_accountId_fkey" FOREIGN KEY ("accountId") REFERENCES "finance_accounts"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_account_bank_invest_details" ADD CONSTRAINT "finance_account_bank_invest_details_sourceAccountId_fkey" FOREIGN KEY ("sourceAccountId") REFERENCES "finance_accounts"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_account_upi_detail" ADD CONSTRAINT "finance_account_upi_detail_accountId_fkey" FOREIGN KEY ("accountId") REFERENCES "finance_accounts"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_donors" ADD CONSTRAINT "finance_donors_userProfileId_fkey" FOREIGN KEY ("userProfileId") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_donations" ADD CONSTRAINT "finance_donations_donorId_fkey" FOREIGN KEY ("donorId") REFERENCES "finance_donors"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_donations" ADD CONSTRAINT "finance_donations_confirmedById_fkey" FOREIGN KEY ("confirmedById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_donations" ADD CONSTRAINT "finance_donations_forEventId_fkey" FOREIGN KEY ("forEventId") REFERENCES "activities"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_donations" ADD CONSTRAINT "finance_donations_paidToAccountId_fkey" FOREIGN KEY ("paidToAccountId") REFERENCES "finance_accounts"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_transactions" ADD CONSTRAINT "finance_transactions_createdById_fkey" FOREIGN KEY ("createdById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_transactions" ADD CONSTRAINT "finance_transactions_accountId_fkey" FOREIGN KEY ("accountId") REFERENCES "finance_accounts"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_expenses" ADD CONSTRAINT "finance_expenses_referenceId_fkey" FOREIGN KEY ("referenceId") REFERENCES "activities"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_expenses" ADD CONSTRAINT "finance_expenses_createdById_fkey" FOREIGN KEY ("createdById") REFERENCES "user_profile"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_expenses" ADD CONSTRAINT "finance_expenses_paidById_fkey" FOREIGN KEY ("paidById") REFERENCES "user_profile"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_expenses" ADD CONSTRAINT "finance_expenses_submittedById_fkey" FOREIGN KEY ("submittedById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_expenses" ADD CONSTRAINT "finance_expenses_finalizedById_fkey" FOREIGN KEY ("finalizedById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_expenses" ADD CONSTRAINT "finance_expenses_settledById_fkey" FOREIGN KEY ("settledById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_expenses" ADD CONSTRAINT "finance_expenses_rejectedById_fkey" FOREIGN KEY ("rejectedById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_expenses" ADD CONSTRAINT "finance_expenses_updatedById_fkey" FOREIGN KEY ("updatedById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_expenses" ADD CONSTRAINT "finance_expenses_accountId_fkey" FOREIGN KEY ("accountId") REFERENCES "finance_accounts"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_expenses" ADD CONSTRAINT "finance_expenses_userProfileId_fkey" FOREIGN KEY ("userProfileId") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_earnings" ADD CONSTRAINT "finance_earnings_accountId_fkey" FOREIGN KEY ("accountId") REFERENCES "finance_accounts"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_earnings" ADD CONSTRAINT "finance_earnings_createdById_fkey" FOREIGN KEY ("createdById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "finance_earnings" ADD CONSTRAINT "finance_earnings_receivedById_fkey" FOREIGN KEY ("receivedById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "meetings" ADD CONSTRAINT "meetings_createdById_fkey" FOREIGN KEY ("createdById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "projects" ADD CONSTRAINT "projects_managerId_fkey" FOREIGN KEY ("managerId") REFERENCES "user_profile"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "projects" ADD CONSTRAINT "projects_sponsorId_fkey" FOREIGN KEY ("sponsorId") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "project_beneficiaries" ADD CONSTRAINT "project_beneficiaries_projectId_fkey" FOREIGN KEY ("projectId") REFERENCES "projects"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "project_goals" ADD CONSTRAINT "project_goals_projectId_fkey" FOREIGN KEY ("projectId") REFERENCES "projects"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "project_milestones" ADD CONSTRAINT "project_milestones_projectId_fkey" FOREIGN KEY ("projectId") REFERENCES "projects"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "project_team_members" ADD CONSTRAINT "project_team_members_projectId_fkey" FOREIGN KEY ("projectId") REFERENCES "projects"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "project_team_members" ADD CONSTRAINT "project_team_members_userId_fkey" FOREIGN KEY ("userId") REFERENCES "user_profile"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "project_risks" ADD CONSTRAINT "project_risks_projectId_fkey" FOREIGN KEY ("projectId") REFERENCES "projects"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "project_risks" ADD CONSTRAINT "project_risks_ownerId_fkey" FOREIGN KEY ("ownerId") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "reports" ADD CONSTRAINT "reports_requestedById_fkey" FOREIGN KEY ("requestedById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "reports" ADD CONSTRAINT "reports_approvedById_fkey" FOREIGN KEY ("approvedById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "requests" ADD CONSTRAINT "requests_initiatedById_fkey" FOREIGN KEY ("initiatedById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "requests" ADD CONSTRAINT "requests_initiatedForId_fkey" FOREIGN KEY ("initiatedForId") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "requests" ADD CONSTRAINT "requests_assigneeId_fkey" FOREIGN KEY ("assigneeId") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "requests" ADD CONSTRAINT "requests_claimedById_fkey" FOREIGN KEY ("claimedById") REFERENCES "user_profile"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "request_events" ADD CONSTRAINT "request_events_requestId_fkey" FOREIGN KEY ("requestId") REFERENCES "requests"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "token_vault_o_auth_token" ADD CONSTRAINT "token_vault_o_auth_token_accountId_fkey" FOREIGN KEY ("accountId") REFERENCES "token_vault_o_auth_account"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "user_phone_number" ADD CONSTRAINT "user_phone_number_userId_fkey" FOREIGN KEY ("userId") REFERENCES "user_profile"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "user_address" ADD CONSTRAINT "user_address_userId_fkey" FOREIGN KEY ("userId") REFERENCES "user_profile"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "user_social_link" ADD CONSTRAINT "user_social_link_userId_fkey" FOREIGN KEY ("userId") REFERENCES "user_profile"("id") ON DELETE CASCADE ON UPDATE CASCADE;
