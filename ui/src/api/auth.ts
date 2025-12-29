import request from './request'

export const login = (data: any) => {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    })
}

export const getInfo = () => {
    return request({
        url: '/auth/info',
        method: 'get'
    })
}

export const register = (data: any) => {
    return request({
        url: '/auth/register',
        method: 'post',
        data
    })
}

export const resetPassword = (data: any) => {
    return request({
        url: '/auth/reset-password',
        method: 'post',
        data
    })
}