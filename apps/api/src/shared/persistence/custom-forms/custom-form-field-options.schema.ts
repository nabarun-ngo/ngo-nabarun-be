import { z } from 'zod';
import { CUSTOM_FORMS_FIELD_OPTIONS_JSON_STORE_NAMESPACE } from '@nabarun-ngo/nestjs-shared-custom-forms';

export { CUSTOM_FORMS_FIELD_OPTIONS_JSON_STORE_NAMESPACE };

export const CustomFormFieldOptionSchema = z.object({
  key: z.string().min(1),
  label: z.string().min(1),
});

export const CustomFormFieldOptionsPayloadSchema = z.object({
  options: z.array(CustomFormFieldOptionSchema),
});

export type CustomFormFieldOptionsPayload = z.infer<typeof CustomFormFieldOptionsPayloadSchema>;
