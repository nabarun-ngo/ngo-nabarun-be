export interface NewsletterSubscribeParams {
  email: string;
  ipAddress?: string;
}

export interface INewsletterSubscriptionPort {
  subscribe(params: NewsletterSubscribeParams): Promise<void>;
}

export const NEWSLETTER_SUBSCRIPTION_PORT = Symbol('INewsletterSubscriptionPort');
