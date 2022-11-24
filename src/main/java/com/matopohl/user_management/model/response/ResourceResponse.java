package com.matopohl.user_management.model.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
public class ResourceResponse extends RepresentationModel<ResourceResponse> implements Serializable {

    private UUID id;
    private String requestUrl;
    private String requestMethod;
    private List<AuthorityResponse> authorities;

}
