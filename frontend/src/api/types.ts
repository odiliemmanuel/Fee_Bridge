export interface UserSummary {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  schoolId: number | null;
  schoolName: string | null;
  roles: string[];
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresInMinutes: number;
  user: UserSummary;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface StudentDto {
  id: number;
  admissionNo: string;
  fullName: string;
  className: string | null;
  classId: number | null;
  residencyType: 'DAY' | 'BOARDING';
  status: string;
}

export type InvoiceStatus = 'PENDING' | 'PARTIAL' | 'PAID' | 'OVERPAID';

export interface InvoiceDto {
  id: number;
  studentId: number;
  studentName: string;
  admissionNo: string;
  className: string | null;
  classId: number | null;
  termId: number;
  residencyType: string;
  grossNaira: number;
  scholarshipNaira: number;
  creditAppliedNaira: number;
  netNaira: number;
  amountPaidNaira: number;
  balanceNaira: number;
  status: InvoiceStatus;
}

export interface ClassDto { id: number; name: string; levelOrder: number; }

export interface Dashboard {
  totalStudents: number;
  totalClasses: number;
  invoiceCount: number;
  expectedNaira: number;
  collectedNaira: number;
  outstandingNaira: number;
  scholarshipNaira: number;
  creditOutstandingNaira: number;
  collectionRatePercent: number;
  statusCounts: { status: InvoiceStatus; count: number }[];
  byClass: { classId: number; className: string; expectedNaira: number; collectedNaira: number; outstandingNaira: number; invoiceCount: number }[];
  byResidency: { residencyType: string; expectedNaira: number; collectedNaira: number; invoiceCount: number }[];
}

export interface ChildDto {
  studentId: number;
  admissionNo: string;
  fullName: string;
  className: string | null;
  residencyType: string;
  outstandingNaira: number;
  creditNaira: number;
}

export interface OrderDto {
  id: number;
  reference: string;
  status: string;
  totalNaira: number;
  virtualAccount: {
    accountNumber: string;
    accountName: string;
    bankName: string;
    expiryAt: string;
    expectedAmountNaira: number;
  } | null;
  allocations: { studentId: number; studentName: string; amountNaira: number; applied: boolean }[];
}

export interface ReconExceptionDto {
  id: number;
  type: string;
  reference: string;
  actualNaira: number | null;
  detail: string;
  resolved: boolean;
  createdAt: string;
}
