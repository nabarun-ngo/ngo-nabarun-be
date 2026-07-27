/**
 * Canonical email template keys used across correspondence resolvers, adapters,
 * and config-key policies. Keep the string values in sync with the seeded
 * email templates — resolvers must reference this enum instead of raw literals.
 */
export enum EmailTemplateKey {
  // User lifecycle
  UserWelcome = 'USER_WELCOME',
  UserDeactivated = 'USER_DEACTIVATED',

  // Donation lifecycle
  DonationCreated = 'DONATION_CREATED',
  DonationPaid = 'DONATION_PAID',
  DonationReminder = 'DONATION_REMINDER',

  // Comments
  CommentMention = 'COMMENT_MENTION',
  CommentAdded = 'COMMENT_ADDED',
}
