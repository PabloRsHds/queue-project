export interface ResponseUserInfoDto {

  userId: string,
  username: string,
  name: string,
  surname: string,
  phone: string,
  email: string,
  role: string,
  counterNumber: number,
  active: boolean,
  createdAt: string,
  updatedAt: string,
  serviceNames: string[]
}
