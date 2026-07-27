import { ApiProperty } from '@nestjs/swagger';
import { FieldOptionDto } from './field-option.dto';

export class DependentOptionsDto {
  @ApiProperty()
  dependsOnKey: string;

  @ApiProperty({ description: 'parentValue → available FieldOptions' })
  optionMap: Record<string, FieldOptionDto[]>;
}
