export interface CreateUserDto {

  userId: string;
  username: string;
  name: string;
  surname: string;
  email: string;
  password: string;
  role: string;
  counterNumber: string;
  serviceIds: string[];
}
