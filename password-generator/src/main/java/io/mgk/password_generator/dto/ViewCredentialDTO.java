package io.mgk.password_generator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViewCredentialDTO {
    private Long id;
    private String username;
    private String password;
    private String siteName;
}
