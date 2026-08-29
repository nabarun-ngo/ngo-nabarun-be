import { GetIdentityCardPdfHandler } from './get-identity-card-pdf.handler';
import { GetIdentityCardPdfQuery } from './get-identity-card-pdf.query';
import { IdentityCardPdfService } from '../../services/identity-card-pdf.service';
import { User } from '../../../domain/aggregates/user/user.aggregate';
import { UserStatus } from '../../../domain/enums/user-status.enum';
import { IUserRepository } from '../../../domain/repositories/user.repository';
import { IIdentityCardPdfPort } from '../../../domain/ports/identity-card-pdf.port';
import { IdentityCardNotIssuableError } from '../../../domain/errors/user.errors';
import { UserNotFoundError } from '../../../domain/errors/user.errors';
import type { UserModuleOptions } from '../../../user.schema';

function makeUser(overrides: Partial<{ status: UserStatus; complete: boolean; uniqueMemberId?: string }> = {}): User {
  return User.rehydrate({
    id: 'user-1',
    email: 'asha@example.com',
    status: overrides.status ?? UserStatus.ACTIVE,
    firstName: 'Asha',
    lastName: 'Verma',
    isProfileComplete: overrides.complete ?? true,
    isPublic: true,
    socialMediaLinks: [],
    uniqueMemberId: 'uniqueMemberId' in overrides ? overrides.uniqueMemberId : 'NM24120011',
    createdAt: new Date('2024-12-01T00:00:00Z'),
  });
}

describe('IdentityCardPdfService / GetIdentityCardPdfHandler', () => {
  let users: jest.Mocked<Pick<IUserRepository, 'findById' | 'allocateNextUniqueMemberId' | 'update'>>;
  let pdf: jest.Mocked<IIdentityCardPdfPort>;
  let handler: GetIdentityCardPdfHandler;

  const options = {
    publicApiUrl: 'https://api.example.test',
    organisationName: 'Test Org',
  } as UserModuleOptions;

  beforeEach(() => {
    users = {
      findById: jest.fn().mockResolvedValue(makeUser()),
      allocateNextUniqueMemberId: jest.fn().mockResolvedValue('NM24120099'),
      update: jest.fn().mockResolvedValue(undefined),
    };
    pdf = { render: jest.fn().mockResolvedValue(Buffer.from('%PDF-mock')) };
    const service = new IdentityCardPdfService(
      users as unknown as IUserRepository,
      pdf,
      options,
    );
    handler = new GetIdentityCardPdfHandler(service);
  });

  it('renders a PDF for an active complete member', async () => {
    const result = await handler.execute(new GetIdentityCardPdfQuery('user-1'));
    expect(result.uniqueMemberId).toBe('NM24120011');
    expect(result.fileName).toBe('identity-card-NM24120011.pdf');
    expect(result.buffer.toString()).toContain('%PDF');
    expect(pdf.render).toHaveBeenCalledWith(
      expect.objectContaining({
        uniqueMemberId: 'NM24120011',
        verifyUrl: 'https://api.example.test/public/identity-cards/NM24120011',
        organisationName: 'Test Org',
        organisationLogoDataUrl: undefined,
        roleLabel: 'Member',
      }),
    );
  });

  it('rejects a blocked member', async () => {
    users.findById.mockResolvedValue(makeUser({ status: UserStatus.BLOCKED }));
    await expect(handler.execute(new GetIdentityCardPdfQuery('user-1'))).rejects.toThrow(
      IdentityCardNotIssuableError,
    );
  });

  it('throws when the member is missing', async () => {
    users.findById.mockResolvedValue(null);
    await expect(handler.execute(new GetIdentityCardPdfQuery('ghost'))).rejects.toThrow(UserNotFoundError);
  });

  it('allocates a uniqueMemberId for a legacy member who has none', async () => {
    users.findById.mockResolvedValue(makeUser({ uniqueMemberId: undefined }));
    const result = await handler.execute(new GetIdentityCardPdfQuery('user-1'));
    expect(users.allocateNextUniqueMemberId).toHaveBeenCalled();
    expect(users.update).toHaveBeenCalled();
    expect(result.uniqueMemberId).toBe('NM24120099');
  });
});
