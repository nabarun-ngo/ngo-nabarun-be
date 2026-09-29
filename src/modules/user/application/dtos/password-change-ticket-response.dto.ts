import { ApiProperty } from '@nestjs/swagger';

export class PasswordChangeTicketResponseDto {
  @ApiProperty({
    description: 'Auth0 hosted password-change ticket URL (short-lived).',
    example: 'https://login.example.auth0.com/lo/reset?ticket=abc',
  })
  ticketUrl!: string;
}
