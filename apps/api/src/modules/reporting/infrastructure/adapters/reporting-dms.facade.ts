import { Injectable } from '@nestjs/common';
import { DmsFacade } from '@nabarun-ngo/nestjs-shared-dms';

@Injectable()
export class ReportingDmsFacade {
  constructor(private readonly dmsFacade: DmsFacade) {}

  async uploadReportDocument(params: {
    buffer: Buffer;
    fileName: string;
    contentType: string;
    reportId: string;
    userId: string;
    userPermissions: string[];
  }): Promise<string> {
    const result = await this.dmsFacade.upload({
      buffer: params.buffer,
      fileName: params.fileName,
      contentType: params.contentType,
      mappings: [{ entityType: 'report', entityId: params.reportId }],
      visibility: 'PRIVATE',
      userId: params.userId,
      userPermissions: params.userPermissions,
    });
    return result.id;
  }

  async getDocuments(reportId: string, userId: string, userPermissions: string[]): Promise<{ id: string }[]> {
    const docs = await this.dmsFacade.listByEntity('report', reportId, userId, userPermissions);
    return docs.map((d) => ({ id: d.id }));
  }

  async deleteDocuments(
    documentIds: string[],
    userId: string,
    userPermissions: string[],
  ): Promise<void> {
    for (const id of documentIds) {
      await this.dmsFacade.delete(id, userId, userPermissions);
    }
  }
}
