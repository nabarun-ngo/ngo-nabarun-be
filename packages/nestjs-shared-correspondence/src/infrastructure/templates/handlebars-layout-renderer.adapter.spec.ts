/**
 * HandlebarsLayoutRendererAdapter unit tests.
 * Loads the real email.hbs base layout co-located in src and verifies that
 * the `or` helper and structured content placeholders render correctly.
 */
import { HandlebarsLayoutRendererAdapter } from '@nabarun-ngo/nestjs-shared-correspondence/infrastructure/templates/handlebars-layout-renderer.adapter';
import { EmailLayoutData } from '@nabarun-ngo/nestjs-shared-correspondence/domain/ports/template.port';
import { TemplateNotFoundError } from '@nabarun-ngo/nestjs-shared-correspondence/domain/errors/correspondence.errors';

describe('HandlebarsLayoutRendererAdapter', () => {
  const adapter = new HandlebarsLayoutRendererAdapter();

  it('renders the email base layout with header and content (uses the "or" helper)', () => {
    const data: EmailLayoutData = {
      body: {
        header: { heading: 'Welcome Sam', subHeading: 'Your account is ready' },
        content: {
          salutation: 'Hi Sam,',
          paragraph1_blue: 'Your id is u-1',
        },
      },
    };

    const html = adapter.render('email', data);

    expect(html).toContain('Welcome Sam');
    expect(html).toContain('Your account is ready');
    expect(html).toContain('Hi Sam,');
    expect(html).toContain('Your id is u-1');
    expect(html.toLowerCase()).toContain('<html');
  });

  it('throws when the base layout does not exist', () => {
    expect(() =>
      adapter.render('does-not-exist', { body: { header: { heading: 'x' }, content: {} } }),
    ).toThrow(TemplateNotFoundError);
  });
});
