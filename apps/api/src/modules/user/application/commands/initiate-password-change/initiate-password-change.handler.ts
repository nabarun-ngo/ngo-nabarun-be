import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { IUserRepository } from '../../../domain/repositories/user.repository';
import { IIdentityProvider } from '../../../domain/ports/identity-provider.port';
import { UserNotFoundError, IdentityNotLinkedError } from '../../../domain/errors/user.errors';
import { USER_OPTIONS } from '../../../infrastructure/user-options.token';
import type { UserModuleOptions } from '../../../user.schema';
import { InitiatePasswordChangeCommand } from './initiate-password-change.command';
import { PasswordChangeTicketResponseDto } from '../../dtos/password-change-ticket-response.dto';

/** Self-service change-password tickets expire after 10 minutes. */
const PASSWORD_CHANGE_TICKET_TTL_SEC = 10 * 60;

@CommandHandler(InitiatePasswordChangeCommand)
@Injectable()
export class InitiatePasswordChangeHandler
  implements ICommandHandler<InitiatePasswordChangeCommand, PasswordChangeTicketResponseDto>
{
  constructor(
    @Inject(IUserRepository) private readonly repo: IUserRepository,
    @Inject(IIdentityProvider) private readonly identityProvider: IIdentityProvider,
    @Inject(USER_OPTIONS) private readonly options: UserModuleOptions,
  ) {}

  async execute({
    params: cmd,
  }: InitiatePasswordChangeCommand): Promise<PasswordChangeTicketResponseDto> {
    const user = await this.repo.findById(cmd.userId);
    if (!user) throw new UserNotFoundError(cmd.userId);
    if (!user.idpSub) throw new IdentityNotLinkedError(user.id);

    await this.identityProvider.verifyPassword(user.email, cmd.currentPassword);

    const { ticketUrl } = await this.identityProvider.createPasswordChangeTicket({
      userId: user.idpSub,
      markEmailAsVerified: false,
      includeEmailInRedirect: false,
      ttlSec: PASSWORD_CHANGE_TICKET_TTL_SEC,
      resultUrl: this.resolveResultUrl(cmd.redirectUrl),
    });

    return { ticketUrl };
  }

  /**
   * Accepts a client-supplied return URL only when it shares the configured
   * application origin — otherwise falls back to the default app URL. This blocks
   * open-redirect abuse where a valid ticket could bounce the user to a hostile host.
   */
  private resolveResultUrl(redirectUrl?: string): string {
    const fallback = this.options.appFeUrl;
    if (!redirectUrl) return fallback;

    try {
      const candidate = new URL(redirectUrl);
      const appOrigin = new URL(fallback).origin;
      return candidate.origin === appOrigin ? candidate.toString() : fallback;
    } catch {
      return fallback;
    }
  }
}
