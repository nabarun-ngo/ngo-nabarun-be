import { Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { InvoiceSummaryDto } from '../../dtos/invoice.dto';
import { InvoiceMapper } from '../../mappers/invoice.mapper';
import { IssueInvoiceService } from '../../services/issue-invoice.service';
import { IssueInvoiceCommand } from './issue-invoice.command';

@CommandHandler(IssueInvoiceCommand)
@Injectable()
export class IssueInvoiceHandler implements ICommandHandler<IssueInvoiceCommand, InvoiceSummaryDto> {
  constructor(private readonly issueInvoiceService: IssueInvoiceService) {}

  async execute({ params }: IssueInvoiceCommand): Promise<InvoiceSummaryDto> {
    const invoice = await this.issueInvoiceService.issue(params);
    return InvoiceMapper.toSummary(invoice);
  }
}
