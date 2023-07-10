package com.itos.vivotools;


public class UserService extends IUserService.Stub{
    @Override
    public void destroy() {
        System.exit(0);
    }

    @Override
    public void exit() {
        System.exit(0);
    }

}
