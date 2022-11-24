package com.matopohl.user_management.service.helper;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class RequestService {

    public Pageable getPageable(Integer page, Integer size, String sort) {
        boolean sorting = false;
        boolean paging = false;

        if(sort != null && !sort.isBlank()) {
            sorting = true;
        }
        if(page != null && size != null) {
            paging = true;
        }

        Pageable pageable;

        if(sorting && paging) {
            pageable = PageRequest.of(page - 1, size, Sort.by(sort));
        }
        else if(sorting) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(sort));
        }
        else if(paging) {
            pageable = PageRequest.of(page - 1, size);
        }
        else {
            pageable = PageRequest.of(0, Integer.MAX_VALUE);
        }

        return pageable;
    }

}
