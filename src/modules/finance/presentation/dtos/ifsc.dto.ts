import { ApiProperty } from '@nestjs/swagger';

export class IfscDetailsDto {
  @ApiProperty({ example: 'HDFC0001234' })
  ifsc: string;

  @ApiProperty({ example: 'HDFC Bank' })
  bankName: string;

  @ApiProperty({ example: 'Salt Lake Sector V' })
  branch: string;
}
