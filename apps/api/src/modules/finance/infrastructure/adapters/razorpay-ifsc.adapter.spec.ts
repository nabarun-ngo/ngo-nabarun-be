jest.mock('@nestjs/axios', () => ({
  HttpService: class HttpService {},
}));

import { of, throwError } from 'rxjs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { RazorpayIfscAdapter } from './razorpay-ifsc.adapter';

function makeHttpService(status: number, data: unknown, error?: Error) {
  const getFn = error
    ? jest.fn().mockReturnValue(throwError(() => error))
    : jest.fn().mockReturnValue(of({ status, data }));

  return { get: getFn };
}

describe('RazorpayIfscAdapter', () => {
  beforeEach(() => jest.clearAllMocks());

  it('maps Razorpay JSON to lookup result and caches subsequent calls', async () => {
    const httpService = makeHttpService(200, {
      IFSC: 'HDFC0000001',
      BANK: 'HDFC Bank',
      BRANCH: 'Mumbai Main',
    });
    const adapter = new RazorpayIfscAdapter(httpService as any);

    const first = await adapter.lookup('hdfc0000001');
    const second = await adapter.lookup('HDFC0000001');

    expect(first).toEqual({
      ifsc: 'HDFC0000001',
      bankName: 'HDFC Bank',
      branch: 'Mumbai Main',
    });
    expect(second).toEqual(first);
    expect(httpService.get).toHaveBeenCalledTimes(1);
  });

  it('throws 404 BusinessException when Razorpay returns 404', async () => {
    const adapter = new RazorpayIfscAdapter(makeHttpService(404, {}) as any);

    await expect(adapter.lookup('HDFC0000001')).rejects.toMatchObject({
      message: 'Invalid IFSC code',
      errorCode: 'IFSC_NOT_FOUND',
      statusCode: 404,
    });
  });

  it('throws 502 when upstream request fails', async () => {
    const adapter = new RazorpayIfscAdapter(
      makeHttpService(200, {}, new Error('timeout')) as any,
    );

    await expect(adapter.lookup('HDFC0000001')).rejects.toMatchObject({
      message: 'Unable to resolve IFSC details',
      errorCode: 'IFSC_LOOKUP_FAILED',
      statusCode: 502,
    });
  });

  it('throws 502 when bank or branch is missing in response', async () => {
    const adapter = new RazorpayIfscAdapter(
      makeHttpService(200, { IFSC: 'HDFC0000001', BANK: 'HDFC Bank' }) as any,
    );

    await expect(adapter.lookup('HDFC0000001')).rejects.toBeInstanceOf(BusinessException);
  });
});
