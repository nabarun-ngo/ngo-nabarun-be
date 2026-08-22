import { BusinessError } from '@nabarun-ngo/nestjs-shared-core';

export class DonorNotFoundError extends BusinessError {
  constructor(id: string) {
    super(`Donor not found: ${id}`, 'DONOR_NOT_FOUND', 404);
  }
}

export class DuplicateDonorEmailError extends BusinessError {
  constructor(email: string) {
    super(`A donor with email '${email}' already exists`, 'DUPLICATE_DONOR_EMAIL', 409);
  }
}

export class InvalidDonorMergeError extends BusinessError {
  constructor(message: string) {
    super(message, 'INVALID_DONOR_MERGE', 400);
  }
}
