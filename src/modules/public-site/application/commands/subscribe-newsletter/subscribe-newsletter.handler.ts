import { BadRequestException, Inject } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import {
  INewsletterSubscriptionPort,
  NEWSLETTER_SUBSCRIPTION_PORT,
} from '../../../domain/ports/newsletter-subscription.port';
import { SubscribeNewsletterCommand } from './subscribe-newsletter.command';

@CommandHandler(SubscribeNewsletterCommand)
export class SubscribeNewsletterHandler
  implements ICommandHandler<SubscribeNewsletterCommand, { message: string }>
{
  constructor(
    @Inject(NEWSLETTER_SUBSCRIPTION_PORT)
    private readonly newsletter: INewsletterSubscriptionPort,
  ) {}

  async execute(command: SubscribeNewsletterCommand): Promise<{ message: string }> {
    const email = command.email?.trim();
    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      throw new BadRequestException('A valid email address is required');
    }

    await this.newsletter.subscribe({ email, ipAddress: command.ipAddress });

    return { message: 'Subscribed successfully' };
  }
}
