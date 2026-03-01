package com.iviet.ivshs.mapper;

public interface UpdateMapper<E, UD> {
  void updateFromDto(UD dto, E entity);
}
