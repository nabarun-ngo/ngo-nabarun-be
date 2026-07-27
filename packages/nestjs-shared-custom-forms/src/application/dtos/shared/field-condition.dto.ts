import { ApiProperty } from '@nestjs/swagger';

export class FieldConditionDto {
  @ApiProperty()
  dependsOnKey: string;

  @ApiProperty({ enum: ['equals', 'not_equals', 'in', 'not_in'] })
  operator: 'equals' | 'not_equals' | 'in' | 'not_in';

  @ApiProperty({ type: Object })
  value: string | number | boolean | string[];
}
