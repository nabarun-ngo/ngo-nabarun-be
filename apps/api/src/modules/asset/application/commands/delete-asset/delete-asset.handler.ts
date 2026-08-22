import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { DeleteAssetCommand } from './delete-asset.command';
import { IAssetRepository } from '../../../domain/repositories/asset.repository';

@CommandHandler(DeleteAssetCommand)
@Injectable()
export class DeleteAssetHandler implements ICommandHandler<DeleteAssetCommand, void> {
  constructor(@Inject(IAssetRepository) private readonly assetRepository: IAssetRepository) {}

  async execute({ id }: DeleteAssetCommand): Promise<void> {
    const asset = await this.assetRepository.findById(id);
    if (!asset) {
      throw new BusinessException('Asset not found with id ' + id);
    }
    await this.assetRepository.delete(asset.id);
  }
}
