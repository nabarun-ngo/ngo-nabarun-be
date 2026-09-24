export type GetMyProfileCriteria = {
  userId?: string;
  idpSub?: string;
};

export class GetMyProfileQuery {
  constructor(public readonly criteria: GetMyProfileCriteria) {}
}
