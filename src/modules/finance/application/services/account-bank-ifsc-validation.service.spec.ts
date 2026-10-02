import { AccountBankIfscValidationService } from './account-bank-ifsc-validation.service';
import { IIfscLookupPort } from '../ports/ifsc-lookup.port';

describe('AccountBankIfscValidationService', () => {
  const lookupPort: jest.Mocked<IIfscLookupPort> = {
    lookup: jest.fn(),
  };

  const service = new AccountBankIfscValidationService(lookupPort);

  beforeEach(() => jest.clearAllMocks());

  it('passes when submitted bank details match lookup', async () => {
    lookupPort.lookup.mockResolvedValue({
      ifsc: 'STDB0001234',
      bankName: 'State Demo Bank',
      branch: 'Salt Lake',
    });

    await expect(
      service.assertBankDetailIfscIntegrity({
        IFSCNumber: 'stdb0001234',
        bankName: 'State Demo Bank',
        bankBranch: 'Salt Lake',
      }),
    ).resolves.toBeUndefined();
  });

  it('rejects tampered bank name for known IFSC', async () => {
    lookupPort.lookup.mockResolvedValue({
      ifsc: 'STDB0001234',
      bankName: 'State Demo Bank',
      branch: 'Salt Lake',
    });

    await expect(
      service.assertBankDetailIfscIntegrity({
        IFSCNumber: 'STDB0001234',
        bankName: 'Other Bank',
        bankBranch: 'Salt Lake',
      }),
    ).rejects.toThrow('Bank name does not match IFSC lookup');
  });
});
