import { Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { IdentityCardPdfResult } from '../../dtos/identity-card.dto';
import { IdentityCardPdfService } from '../../services/identity-card-pdf.service';
import { IssueIdentityCardCommand } from './issue-identity-card.command';

@CommandHandler(IssueIdentityCardCommand)
@Injectable()
export class IssueIdentityCardHandler
  implements ICommandHandler<IssueIdentityCardCommand, IdentityCardPdfResult>
{
  constructor(private readonly pdfs: IdentityCardPdfService) {}

  execute({ params }: IssueIdentityCardCommand): Promise<IdentityCardPdfResult> {
    return this.pdfs.renderForUser(params.userId, params.pictureDataUrl);
  }
}
