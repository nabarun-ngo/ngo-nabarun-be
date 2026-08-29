import { Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { IssueInvoiceService } from '../../services/issue-invoice.service';
import { VoidIssuedInvoiceCommand } from './void-issued-invoice.command';

@CommandHandler(VoidIssuedInvoiceCommand)
@Injectable()
export class VoidIssuedInvoiceHandler implements ICommandHandler<VoidIssuedInvoiceCommand, void> {
  constructor(private readonly issueInvoiceService: IssueInvoiceService) {}

  execute({ params }: VoidIssuedInvoiceCommand): Promise<void> {
    return this.issueInvoiceService.voidIssued(params.entityType, params.entityId, params.reason);
  }
}
