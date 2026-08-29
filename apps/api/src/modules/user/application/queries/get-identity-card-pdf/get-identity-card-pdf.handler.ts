import { Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IdentityCardPdfResult } from '../../dtos/identity-card.dto';
import { IdentityCardPdfService } from '../../services/identity-card-pdf.service';
import { GetIdentityCardPdfQuery } from './get-identity-card-pdf.query';

@QueryHandler(GetIdentityCardPdfQuery)
@Injectable()
export class GetIdentityCardPdfHandler
  implements IQueryHandler<GetIdentityCardPdfQuery, IdentityCardPdfResult>
{
  constructor(private readonly pdfs: IdentityCardPdfService) {}

  execute(query: GetIdentityCardPdfQuery): Promise<IdentityCardPdfResult> {
    return this.pdfs.renderForUser(query.userId);
  }
}
