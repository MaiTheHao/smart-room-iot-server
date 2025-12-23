package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.SetupRequestV1;
import com.iviet.ivshs.dto.SetupResponseV1;

public interface SetupServiceV1 {
    SetupResponseV1 setup(SetupRequestV1 req);    
}
