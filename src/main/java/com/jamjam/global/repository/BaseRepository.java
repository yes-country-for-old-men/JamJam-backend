package com.jamjam.global.repository;

import com.jamjam.global.exception.ApiException;
import com.jamjam.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    default T findByIdOrThrow(ID id, ErrorCode errorCode) {
        return findById(id)
                .orElseThrow(() -> new ApiException(errorCode));
    }
}
