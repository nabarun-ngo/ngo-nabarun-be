import { BusinessError } from '@nabarun-ngo/nestjs-shared-core';

export class RequestNotFoundError extends BusinessError {
  constructor(id: string) {
    super(`Request not found: ${id}`, 'REQUEST_NOT_FOUND', 404);
  }
}

export class RequestDefinitionNotFoundError extends BusinessError {
  constructor(type: string) {
    super(`Request definition not found: ${type}`, 'REQUEST_DEFINITION_NOT_FOUND', 404);
  }
}

export class RequestForbiddenError extends BusinessError {
  constructor(message = 'You are not allowed to perform this action on the request') {
    super(message, 'REQUEST_FORBIDDEN', 403);
  }
}

export class RequestInvalidStateError extends BusinessError {
  constructor(message: string) {
    super(message, 'REQUEST_INVALID_STATE', 400);
  }
}
