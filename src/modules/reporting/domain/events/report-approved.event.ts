import { DomainEvent } from '@ssdev-toolkit/nestjs-core';

export type ReportApprovedSnapshot = {
  readonly reportCode: string;
  readonly reportName: string;
  readonly status: string;
};

export class ReportApprovedEvent extends DomainEvent<ReportApprovedSnapshot> {
  constructor(
    reportId: string,
    snapshot: ReportApprovedSnapshot,
  ) {
    super(reportId, snapshot);
  }
}
