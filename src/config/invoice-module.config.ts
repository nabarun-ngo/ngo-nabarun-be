import { InvoiceModule } from '../modules/invoice/invoice.module';
import { DMS_MODULE } from './dms-module.config';

export const INVOICE_MODULE = InvoiceModule.forRootAsync({
  imports: [DMS_MODULE],
  useFactory: () => ({}),
});
