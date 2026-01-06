package com.iviet.ivshs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Annotation này được sử dụng để đánh dấu các phương thức Mapper trong MapStruct 
 * nhằm bỏ qua (ignore) các trường thông tin kiểm toán (audit fields).
 * <p>
 * Các trường bị bỏ qua bao gồm các thuộc tính kế thừa từ {@code BaseAuditEntity}:
 * <ul>
 * <li>{@code createdAt}</li>
 * <li>{@code createdBy}</li>
 * <li>{@code updatedAt}</li>
 * <li>{@code updatedBy}</li>
 * <li>{@code version}</li>
 * </ul>
 * * <p><b>Cách sử dụng:</b></p>
 * <pre>
 * &#64;Mapper
 * public interface UserMapper {
 * &#64;IgnoreAuditFields
 * User toEntity(UserDTO dto);
 * }
 * </pre>
 *
 * @see com.iviet.ivshs.entities.BaseAuditEntity
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@Mappings({
    @Mapping(target = "createdAt", ignore = true),
    @Mapping(target = "createdBy", ignore = true),
    @Mapping(target = "updatedAt", ignore = true),
    @Mapping(target = "updatedBy", ignore = true),
    @Mapping(target = "version", ignore = true)
})
public @interface IgnoreAuditFields {}