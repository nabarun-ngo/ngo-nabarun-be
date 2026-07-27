import { TokenManager } from './token.manager';
import {
  IWorkflowTokenRepository,
  WorkflowTokenStatus,
  WorkflowTokenRecord,
} from '../domain/ports/workflow-token.repository';

describe('TokenManager', () => {
  const forkGatewayId = 'pg_fork';
  const joinGatewayId = 'pg_join';

  function createManager(tokens: WorkflowTokenRecord[]): TokenManager {
    const repo: IWorkflowTokenRepository = {
      create: jest.fn(),
      createMany: jest.fn(),
      findById: jest.fn(),
      findActiveByInstance: jest.fn().mockResolvedValue(tokens),
      findByInstanceAndBranch: jest.fn(),
      moveToken: jest.fn(),
      consumeToken: jest.fn(),
      cancelByInstance: jest.fn(),
    };
    return new TokenManager(repo);
  }

  it('detects join complete when all branch tokens arrive at the join gateway', async () => {
    const tokens: WorkflowTokenRecord[] = [
      {
        id: 't1',
        instanceId: 'NW1',
        branchId: 'b1',
        parentGatewayId: forkGatewayId,
        status: WorkflowTokenStatus.Waiting,
        currentElementId: joinGatewayId,
        createdAt: new Date(),
        updatedAt: new Date(),
      },
      {
        id: 't2',
        instanceId: 'NW1',
        branchId: 'b2',
        parentGatewayId: forkGatewayId,
        status: WorkflowTokenStatus.Waiting,
        currentElementId: joinGatewayId,
        createdAt: new Date(),
        updatedAt: new Date(),
      },
    ];

    const manager = createManager(tokens);
    await expect(manager.isJoinComplete('NW1', joinGatewayId, 2)).resolves.toBe(true);
  });

  it('does not treat fork gateway id as join gateway id', async () => {
    const tokens: WorkflowTokenRecord[] = [
      {
        id: 't1',
        instanceId: 'NW1',
        branchId: 'b1',
        parentGatewayId: forkGatewayId,
        status: WorkflowTokenStatus.Active,
        currentElementId: 'ut_verify',
        createdAt: new Date(),
        updatedAt: new Date(),
      },
    ];

    const manager = createManager(tokens);
    await expect(manager.isJoinComplete('NW1', joinGatewayId, 2)).resolves.toBe(false);
  });

  it('finds an active token at a branch element', async () => {
    const tokens: WorkflowTokenRecord[] = [
      {
        id: 't1',
        instanceId: 'NW1',
        branchId: 'b1',
        parentGatewayId: forkGatewayId,
        status: WorkflowTokenStatus.Active,
        currentElementId: 'ut_verify',
        createdAt: new Date(),
        updatedAt: new Date(),
      },
    ];

    const manager = createManager(tokens);
    const token = await manager.findActiveAtElement('NW1', 'ut_verify');
    expect(token?.id).toBe('t1');
    await expect(manager.findActiveAtElement('NW1', 'ut_policy')).resolves.toBeNull();
  });
});
