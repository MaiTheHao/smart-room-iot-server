package com.iviet.ivshs.service.auth;

import com.iviet.ivshs.dto.LoginViewModel;

import jakarta.servlet.http.HttpServletRequest;

public interface LoginViewService {
  public LoginViewModel getModel(HttpServletRequest request, Boolean isError);
}
