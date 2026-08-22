export enum RequestStatus {
  PendingForApproval = 'PendingForApproval',
  YetToStart = 'YetToStart',
  InProgress = 'InProgress',
  Closed = 'Closed',
  Rejected = 'Rejected',
  Withdrawn = 'Withdrawn',
}

export const REQUEST_TERMINAL_STATUSES: ReadonlySet<RequestStatus> = new Set([
  RequestStatus.Closed,
  RequestStatus.Rejected,
  RequestStatus.Withdrawn,
]);
