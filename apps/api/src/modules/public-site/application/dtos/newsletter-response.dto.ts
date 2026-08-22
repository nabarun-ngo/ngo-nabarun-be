import { ApiProperty } from '@nestjs/swagger';

export class NewsletterSubscribeResponseDto {
  @ApiProperty({ example: 'Subscribed successfully' })
  message!: string;
}
