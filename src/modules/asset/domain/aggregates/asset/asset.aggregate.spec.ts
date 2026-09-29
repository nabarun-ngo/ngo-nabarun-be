import { Asset } from './asset.aggregate';
import { AssetCategory, AssetStatus } from '../../enums/asset.enum';

describe('Asset aggregate', () => {
  it('creates as AVAILABLE and assigns custody', () => {
    const asset = Asset.create({
      name: 'Laptop',
      category: AssetCategory.ELECTRONICS,
    });
    expect(asset.status).toBe(AssetStatus.AVAILABLE);

    asset.assignCustody('user-1', 'admin-1', 'field use');
    expect(asset.status).toBe(AssetStatus.ASSIGNED);
    expect(asset.custodianUserId).toBe('user-1');
    expect(asset.custodyHistory).toHaveLength(1);
  });

  it('auto-closes open custody on reassign and returns to AVAILABLE', () => {
    const asset = Asset.create({
      name: 'Chair',
      category: AssetCategory.FURNITURE,
    });
    asset.assignCustody('user-1', 'admin-1');
    asset.assignCustody('user-2', 'admin-1');
    expect(asset.custodyHistory).toHaveLength(2);
    expect(asset.custodyHistory[0].returnedAt).toBeDefined();
    expect(asset.custodianUserId).toBe('user-2');

    asset.returnCustody('admin-1');
    expect(asset.custodianUserId).toBeUndefined();
    expect(asset.status).toBe(AssetStatus.AVAILABLE);
  });

  it('blocks assign when retired and requires currency with amounts', () => {
    const asset = Asset.create({
      name: 'Printer',
      category: AssetCategory.EQUIPMENT,
      status: AssetStatus.RETIRED,
    });
    expect(() => asset.assignCustody('user-1')).toThrow(/Retired/);

    expect(() =>
      Asset.create({
        name: 'Desk',
        category: AssetCategory.FURNITURE,
        purchaseCost: 1000,
      }),
    ).toThrow(/Currency/);
  });
});
