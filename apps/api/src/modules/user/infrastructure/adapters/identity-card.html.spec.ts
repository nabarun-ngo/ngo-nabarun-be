import { renderIdentityCardHtml } from './identity-card.html';

describe('renderIdentityCardHtml', () => {
  it('stamps only member-specific fields onto the shared card chrome', () => {
    const html = renderIdentityCardHtml(
      {
        organisationName: 'Ichapur Nabarun Social Welfare Society',
        organisationRegistrationNumber: 'S0063530',
        displayName: 'Ananya Chatterjee',
        roleLabel: 'Member',
        initials: 'AC',
        uniqueMemberId: 'NM26030429',
        contactNumber: '+91 90000 00000',
        bloodGroup: 'B+',
        verifyUrl: 'https://api.example.test/public/identity-cards/NM26030429',
      },
      'data:image/png;base64,qq==',
    );

    expect(html).toContain('Ichapur Nabarun Social Welfare Society');
    expect(html).toContain('Reg. No. S0063530');
    expect(html).toContain('Ananya Chatterjee');
    expect(html).toContain('Member');
    expect(html).toContain('NM26030429');
    expect(html).toContain('+91 90000 00000');
    expect(html).toContain('Blood group');
    expect(html).toContain('B+');
    expect(html).toContain('Scan to check membership');
    expect(html).toContain('data:image/png;base64,qq==');
  });

  it('escapes member text so it cannot break the template', () => {
    const html = renderIdentityCardHtml(
      {
        organisationName: 'Org',
        displayName: 'A <script>x</script>',
        roleLabel: 'Member',
        initials: 'AX',
        uniqueMemberId: 'NM26030429',
        verifyUrl: 'https://api.example.test/public/identity-cards/NM26030429',
      },
      'data:image/png;base64,qq==',
    );

    expect(html).not.toContain('<script>x</script>');
    expect(html).toContain('A &lt;script&gt;x&lt;/script&gt;');
    expect(html).not.toContain('Contact');
    expect(html).not.toContain('Blood group');
  });
});
