import { Injectable } from '@nestjs/common';
import {
  INewsletterSubscriptionPort,
  NewsletterSubscribeParams,
} from '../../domain/ports/newsletter-subscription.port';

/** Placeholder until an external newsletter provider is wired. */
@Injectable()
export class NoOpNewsletterSubscriptionAdapter implements INewsletterSubscriptionPort {
  async subscribe(_params: NewsletterSubscribeParams): Promise<void> {
    // External integration (e.g. Mailchimp, SendGrid lists) will replace this adapter.
  }
}
