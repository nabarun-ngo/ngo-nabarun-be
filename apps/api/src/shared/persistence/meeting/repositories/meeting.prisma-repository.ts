import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { PrismaClient } from '../../prisma/client';
import type {
  MeetingWhereInput,
  MeetingWhereUniqueInput,
  MeetingUncheckedCreateInput,
  MeetingUncheckedUpdateInput,
  MeetingOrderByWithRelationInput,
} from '../../prisma/models/Meeting';
import { Meeting, MeetingFilter } from '../../../../modules/meeting/domain/aggregates/meeting/meeting.aggregate';
import { IMeetingRepository } from '../../../../modules/meeting/domain/repositories/meeting.repository';
import { MeetingPersistence, MeetingPrismaMapper } from '../mapper/meeting-prisma.mapper';

@Injectable()
export class MeetingPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'meeting',
    Meeting,
    string,
    MeetingFilter,
    MeetingPersistence,
    MeetingWhereInput,
    MeetingWhereUniqueInput,
    MeetingUncheckedCreateInput,
    MeetingUncheckedUpdateInput,
    MeetingOrderByWithRelationInput
  >
  implements IMeetingRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'meeting');
  }

  protected toDomain(row: MeetingPersistence): Meeting {
    return MeetingPrismaMapper.toDomain(row)!;
  }

  protected toCreateInput(entity: Meeting): MeetingUncheckedCreateInput {
    return MeetingPrismaMapper.toCreate(entity);
  }

  protected toUpdateInput(_id: string, entity: Meeting): MeetingUncheckedUpdateInput {
    return MeetingPrismaMapper.toUpdate(entity);
  }

  protected toUniqueWhere(id: string): MeetingWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(props?: MeetingFilter): MeetingWhereInput {
    return {
      deletedAt: null,
      ...(props?.createdById ? { createdById: props.createdById } : {}),
      ...(props?.participantId ? { attendees: { contains: props.participantId } } : {}),
      ...(props?.participantEmail ? { attendees: { contains: props.participantEmail } } : {}),
    };
  }

  protected override defaultOrderBy(): MeetingOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected override defaultPageSize(): number {
    return 20;
  }

  async findByExtId(extId: string): Promise<Meeting | null> {
    const row = await this.delegate.findUnique({ where: { extMeetingId: extId } });
    return MeetingPrismaMapper.toDomain(row);
  }

  /** Soft delete also cancels the meeting, so the generic soft-delete hook cannot be used. */
  override async delete(id: string): Promise<void> {
    await this.delegate.update({
      where: { id },
      data: { deletedAt: new Date(), status: 'cancelled' },
    });
  }
}
