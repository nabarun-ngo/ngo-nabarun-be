import { z } from 'zod';

export const BookBankModuleOptionsSchema = z.object({});

export type BookBankModuleOptions = z.infer<typeof BookBankModuleOptionsSchema>;
export type BookBankModuleInput = z.input<typeof BookBankModuleOptionsSchema>;
