/** App-wide entityType discriminator (custom forms, DMS, comments, etc.). */
export enum EntityType {
  Donation = 'donation',
  Donor = 'donor',
  Invoice = 'invoice',
  Workflow = 'workflow',
  PublicSite = 'public_site',
}
