import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { GetIfscDetailsHandler } from './get-ifsc-details.handler';
import { GetIfscDetailsQuery } from './get-ifsc-details.query';
import { IIfscLookupPort } from '../../ports/ifsc-lookup.port';

describe('GetIfscDetailsHandler', () => {
  const lookupPort: jest.Mocked<IIfscLookupPort> = {
    lookup: jest.fn(),
  };

  const handler = new GetIfscDetailsHandler(lookupPort);

  beforeEach(() => jest.clearAllMocks());

  it('returns IFSC details for valid code', async () => {
    lookupPort.lookup.mockResolvedValue({
      ifsc: 'HDFC0000001',
      bankName: 'HDFC Bank',
      branch: 'Mumbai Main',
    });

    const result = await handler.execute(new GetIfscDetailsQuery('hdfc0000001'));

    expect(lookupPort.lookup).toHaveBeenCalledWith('HDFC0000001');
    expect(result).toEqual({
      ifsc: 'HDFC0000001',
      bankName: 'HDFC Bank',
      branch: 'Mumbai Main',
    });
  });

  it('throws 400 for invalid IFSC format', async () => {
    await expect(handler.execute(new GetIfscDetailsQuery('BAD'))).rejects.toMatchObject({
      message: 'Invalid IFSC format',
      errorCode: 'INVALID_IFSC_FORMAT',
      statusCode: 400,
    });
    expect(lookupPort.lookup).not.toHaveBeenCalled();
  });

  it('propagates port BusinessException', async () => {
    lookupPort.lookup.mockRejectedValue(
      new BusinessException('Invalid IFSC code', 'IFSC_NOT_FOUND', 404),
    );

    await expect(handler.execute(new GetIfscDetailsQuery('HDFC0000001'))).rejects.toMatchObject({
      errorCode: 'IFSC_NOT_FOUND',
      statusCode: 404,
    });
  });
});
