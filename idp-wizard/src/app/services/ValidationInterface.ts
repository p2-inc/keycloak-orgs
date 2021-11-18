export type ResponseType = 'success' | 'error'
export interface IValidationInterface {
    status: ResponseType
    message: string
}