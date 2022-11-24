package com.matopohl.user_management.model.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
public class RefreshTokenBaseResponse extends RepresentationModel<RefreshTokenBaseResponse> implements Serializable {

    private UUID id;
    private ZonedDateTime creationDate;
    private ZonedDateTime expirationDate;
    private ZonedDateTime lastRefreshDate;
    private Long refreshCount;

}
