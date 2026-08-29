import { z } from 'zod';

export const InvoiceModuleOptionsSchema = z.object({});

export type InvoiceModuleOptions = z.infer<typeof InvoiceModuleOptionsSchema>;
export type InvoiceModuleInput = z.input<typeof InvoiceModuleOptionsSchema>;
