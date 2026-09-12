export interface UpdateUserDto{

  userId?: string;
  username?: string;
  name?: string;
  surname?: string;
  phone?: string;
  email?: string;
  password?: string;
  role?: string;
  active?: boolean;
  counterNumber?: number;
  serviceIds?: string[];
}
