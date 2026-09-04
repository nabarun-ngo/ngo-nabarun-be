import { MeetingDetailFilterDto } from '../../dtos/meeting.dto';

export class ListMeetingsQuery {
  constructor(
    public readonly filter: MeetingDetailFilterDto = {},
  ) {}
}
