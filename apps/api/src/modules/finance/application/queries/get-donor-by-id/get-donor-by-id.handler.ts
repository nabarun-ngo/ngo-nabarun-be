import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IUserLookupPort } from '@nabarun-ngo/nestjs-shared-core';
import { DonorNotFoundError } from '../../../domain/errors/donor.errors';
import { IDonorRepository } from '../../../domain/repositories/donor.repository';
import { DonorDto } from '../../dtos/donor.dto';
import { DonorMapper } from '../../mappers/donor.mapper';
import { GetDonorByIdQuery } from './get-donor-by-id.query';

@QueryHandler(GetDonorByIdQuery)
@Injectable()
export class GetDonorByIdHandler implements IQueryHandler<GetDonorByIdQuery, DonorDto> {
  constructor(
    @Inject(IDonorRepository) private readonly donorRepository: IDonorRepository,
    @Inject(IUserLookupPort) private readonly userLookup: IUserLookupPort,
  ) {}

  async execute(query: GetDonorByIdQuery): Promise<DonorDto> {
    const donor = await this.donorRepository.findById(query.donorId);
    if (!donor) throw new DonorNotFoundError(query.donorId);
    const userProfile = donor.userProfileId
      ? await this.userLookup.findById(donor.userProfileId)
      : undefined;
    return DonorMapper.toDto(donor, userProfile);
  }
}
