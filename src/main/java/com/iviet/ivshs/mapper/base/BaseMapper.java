package com.iviet.ivshs.mapper.base;

import java.util.List;

public interface BaseMapper<E, D> {

  E toEntity(D dto);

  D toDto(E entity);

  List<D> toDtoList(List<E> entities);
}
