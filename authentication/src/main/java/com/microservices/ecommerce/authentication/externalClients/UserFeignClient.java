package com.microservices.ecommerce.authentication.externalClients;

import com.microservices.ecommerce.authentication.dto.RegisterRequestDTO;
import com.microservices.ecommerce.authentication.dto.UserAuthResponseDTO;
import com.microservices.ecommerce.authentication.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name= "user")
public interface UserFeignClient {

    @PostMapping("/users/internal")
    UserResponseDTO createUser(@RequestBody RegisterRequestDTO registerRequestDTO,
                               @RequestHeader("X-Internal-Token") String internalToken);

    @GetMapping("/users/auth")
    UserAuthResponseDTO getUserAuthByEmail(@RequestParam("email") String email,
                                           @RequestHeader("X-Internal-Token") String internalToken);




}
