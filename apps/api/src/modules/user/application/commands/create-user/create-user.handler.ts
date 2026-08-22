import { Inject, Injectable, Logger } from '@nestjs/common';
import { CommandHandler, EventBus, ICommandHandler } from '@nestjs/cqrs';
import { User } from '../../../domain/aggregates/user/user.aggregate';
import { UserStatus } from '../../../domain/enums/user-status.enum';
import { IUserRepository } from '../../../domain/repositories/user.repository';
import { IIdentityProvider } from '../../../domain/ports/identity-provider.port';
import { UniqueEmailPolicy } from '../../../domain/policies/unique-email.policy';
import { UserProfileCompletenessPolicy } from '../../../domain/policies/user-profile-completeness.policy';
import { CreateUserCommand } from './create-user.command';
import { UserResponseDto } from '../../dtos/user-response.dto';
import { UserResponseMapper } from '../../mappers/user-response.mapper';

@CommandHandler(CreateUserCommand)
@Injectable()
export class CreateUserHandler implements ICommandHandler<CreateUserCommand, UserResponseDto> {
  private readonly logger = new Logger(CreateUserHandler.name);

  constructor(
    @Inject(IUserRepository) private readonly repo: IUserRepository,
    @Inject(IIdentityProvider) private readonly identityProvider: IIdentityProvider,
    private readonly eventBus: EventBus,
  ) {}

  async execute({ params: cmd }: CreateUserCommand): Promise<UserResponseDto> {
    // 1. Email collision check (includes soft-deleted rows)
    const existing = await this.repo.findByEmail(cmd.email);
    UniqueEmailPolicy.assertNoDuplicate(cmd.email, existing ?? null);

    const isReuse = !!(existing?.status === UserStatus.DELETED || existing?.deletedAt);

    // 2. Create or reuse aggregate — new users start ACTIVE (not Draft)
    const user = User.create(
      {
        email: cmd.email,
        firstName: cmd.firstName,
        lastName: cmd.lastName,
        title: cmd.title,
        middleName: cmd.middleName,
        dateOfBirth: cmd.dateOfBirth,
        gender: cmd.gender,
        about: cmd.about,
        picture: cmd.picture,
        isPublic: cmd.isPublic,
      },
      isReuse ? existing! : undefined,
    );

    if (isReuse) {
      user.restoreFromDeletion();
    }

    // 3. Profile completeness (independent of lifecycle status)
    user.applyCompleteness(UserProfileCompletenessPolicy.evaluate(user));

    // 4. Provision identity — strong system password, email_verified false, no metadata
    const { externalSub } = await this.identityProvider.createUser(user, {
      emailVerified: false,
    });
    user.linkIdentity(externalSub);

    // 5. Password-change ticket (Auth0 hosted set-password page).
    let setPasswordUrl: string | undefined;
    try {
      const ticket = await this.identityProvider.createPasswordChangeTicket({
        userId: externalSub,
        markEmailAsVerified: true,
        includeEmailInRedirect: true,
      });
      setPasswordUrl = ticket.ticketUrl;
    } catch (err) {
      this.logger.error(
        `Failed to create password-change ticket for ${user.email}: ${err instanceof Error ? err.message : err}`,
      );
    }

    // 6. Raise created event — Welcome email (title + set-password / login instructions)
    user.confirmProvisioned(setPasswordUrl);
    user.setCreatedById(cmd.createdById);
    user.setUpdatedById(cmd.createdById);

    if (isReuse) {
      await this.repo.update(user.id, user);
    } else {
      await this.repo.create(user);
    }

    // 7. Publish domain events
    const events = [...user.domainEvents];
    user.clearEvents();
    this.eventBus.publishAll(events);

    return UserResponseMapper.toDto(user);
  }
}
