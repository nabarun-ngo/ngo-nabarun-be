import { z } from 'zod';

export const CustomFormFieldOptionSchema = z.object({
  key: z.string().min(1),
  label: z.string().min(1),
});

export const CustomFormFieldOptionsPayloadSchema = z.object({
  options: z.array(CustomFormFieldOptionSchema),
});

export type CustomFormFieldOptionsPayload = z.infer<typeof CustomFormFieldOptionsPayloadSchema>;
