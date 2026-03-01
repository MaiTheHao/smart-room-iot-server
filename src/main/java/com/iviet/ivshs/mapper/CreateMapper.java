package com.iviet.ivshs.mapper;

public interface CreateMapper<E, CD> {
  E fromCreateDto(CD dto);
}
