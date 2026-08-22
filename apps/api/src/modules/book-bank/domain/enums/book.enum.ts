export enum BookStatus {
  AVAILABLE = 'AVAILABLE',
  ON_LOAN = 'ON_LOAN',
  DONATED_OUT = 'DONATED_OUT',
  RETIRED = 'RETIRED',
  LOST = 'LOST',
}

export enum BookAcquisitionType {
  PURCHASED = 'PURCHASED',
  DONATED_IN = 'DONATED_IN',
}

export enum BookCategory {
  TEXTBOOK = 'TEXTBOOK',
  STORY = 'STORY',
  REFERENCE = 'REFERENCE',
  MAGAZINE = 'MAGAZINE',
  OTHER = 'OTHER',
}

export enum BookSubject {
  MATH = 'MATH',
  SCIENCE = 'SCIENCE',
  LANGUAGE = 'LANGUAGE',
  SOCIAL = 'SOCIAL',
  GENERAL = 'GENERAL',
  OTHER = 'OTHER',
}

export enum BookClassLevel {
  CLASS_1 = 'CLASS_1',
  CLASS_2 = 'CLASS_2',
  CLASS_3 = 'CLASS_3',
  CLASS_4 = 'CLASS_4',
  CLASS_5 = 'CLASS_5',
  CLASS_6 = 'CLASS_6',
  CLASS_7 = 'CLASS_7',
  CLASS_8 = 'CLASS_8',
  CLASS_9 = 'CLASS_9',
  CLASS_10 = 'CLASS_10',
  CLASS_11 = 'CLASS_11',
  CLASS_12 = 'CLASS_12',
  GENERAL = 'GENERAL',
}

export enum BookOperation {
  LEND = 'LEND',
  RETURN = 'RETURN',
  DONATE_OUT = 'DONATE_OUT',
  RETIRE = 'RETIRE',
  MARK_LOST = 'MARK_LOST',
  TRANSFER_LOCATION = 'TRANSFER_LOCATION',
}
