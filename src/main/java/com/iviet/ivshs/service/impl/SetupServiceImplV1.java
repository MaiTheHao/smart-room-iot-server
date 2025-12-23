package com.iviet.ivshs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.iviet.ivshs.dao.ClientDaoV1;
import com.iviet.ivshs.dao.SetupDaoV1;
import com.iviet.ivshs.dto.SetupRequestV1;
import com.iviet.ivshs.dto.SetupResponseV1;
import com.iviet.ivshs.entities.ClientV1;
import com.iviet.ivshs.enumeration.ClientTypeV1;
import com.iviet.ivshs.exception.BadRequestException;
import com.iviet.ivshs.service.SetupServiceV1;

@Service
public class SetupServiceImplV1 implements SetupServiceV1 {

    @Autowired
    private ClientDaoV1 clientDao;
    
    @Autowired
    private SetupDaoV1 SetupDaoV1;

    @Override
    public SetupResponseV1 setup(SetupRequestV1 req) {
        String cname = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (cname == null) {
            throw new BadRequestException("Invalid client credentials");
        }
        
        ClientV1 client = clientDao.findByUsername(cname);
        if (client == null || client.getClientType() != ClientTypeV1.HARDWARE_GATEWAY) {
            throw new BadRequestException("Client not authorized for setup");
        }
        
        return SetupDaoV1.setup(req, client.getId());
    }
}
