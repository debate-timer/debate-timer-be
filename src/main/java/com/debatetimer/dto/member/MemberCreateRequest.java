package com.debatetimer.dto.member;

import jakarta.validation.constraints.NotBlank;

public record MemberCreateRequest(@NotBlank String code) {

}
