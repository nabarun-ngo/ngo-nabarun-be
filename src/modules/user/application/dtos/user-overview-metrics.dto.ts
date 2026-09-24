import { ApiPropertyOptional } from '@nestjs/swagger';

export class UserOverviewMetricsDto {
  @ApiPropertyOptional({ example: 12500, description: 'Sum of outstanding donations for the current user' })
  pendingDonations?: number;

  @ApiPropertyOptional({ example: 48250, description: 'Total balance across active wallet accounts' })
  walletBalance?: number;

  @ApiPropertyOptional({ example: 3200, description: 'Sum of unsettled expenses paid by the current user' })
  unsettledExpense?: number;

  @ApiPropertyOptional({
    example: 4,
    description:
      'Request inbox items the user can act on (pending approval or yet to start, by role/group eligibility)',
  })
  pendingTask?: number;
}
