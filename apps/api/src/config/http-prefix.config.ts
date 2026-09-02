import { RequestMethod } from '@nestjs/common';
import { RouteExclusion } from '@nabarun-ngo/nestjs-shared-core';

export const prefixExclusions: RouteExclusion[] = [
  { path: 'newsletter', method: RequestMethod.POST },
  { path: 'health', method: RequestMethod.GET },
  { path: 'ready', method: RequestMethod.GET },
  { path: 'metrics', method: RequestMethod.GET },
];
