export interface User {
  userId: string;
  username: string;
  email: string;
  fullName: string;
  role: UserRole;
  createdAt: string;
}

export type UserRole = 'TRADER' | 'ADMIN';
