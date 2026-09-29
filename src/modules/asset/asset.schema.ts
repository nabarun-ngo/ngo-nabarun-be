import { z } from 'zod';

export const AssetModuleOptionsSchema = z.object({});

export type AssetModuleOptions = z.infer<typeof AssetModuleOptionsSchema>;
export type AssetModuleInput = z.input<typeof AssetModuleOptionsSchema>;
