package com.matopohl.user_management.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ResourceCreateRequest extends ResourceBaseRequest implements Serializable {

    @NotEmpty
    List<String> authorities;

}
