import { Injectable } from '@nestjs/common';
import { DmsFacade } from '@nabarun-ngo/nestjs-shared-dms';

/** Internal finance jobs use a service actor with document permissions. */
const FINANCE_DMS_SERVICE_USER_ID = 'system:finance';
const FINANCE_DMS_SERVICE_PERMISSIONS = [
  'read:documents',
  'create:documents',
  'delete:documents',
];

@Injectable()
export class FinanceDmsAdapter {
  constructor(private readonly dmsFacade: DmsFacade) {}

  getDocuments(entityType: string, entityId: string): Promise<{ id: string }[]> {
    return this.dmsFacade
      .listByEntity(
        entityType,
        entityId,
        FINANCE_DMS_SERVICE_USER_ID,
        FINANCE_DMS_SERVICE_PERMISSIONS,
      )
      .then((docs) => docs.map((d) => ({ id: d.id })));
  }

  deleteFile(documentId: string): Promise<void> {
    return this.dmsFacade.delete(
      documentId,
      FINANCE_DMS_SERVICE_USER_ID,
      FINANCE_DMS_SERVICE_PERMISSIONS,
    );
  }
}
