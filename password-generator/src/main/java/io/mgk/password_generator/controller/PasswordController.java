package io.mgk.password_generator.controller;

import io.mgk.password_generator.dto.CredentialDTO;
import io.mgk.password_generator.dto.ViewCredentialDTO;
import io.mgk.password_generator.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/passwords")
public class PasswordController {

    private static final String WORDS = "words";
    @Autowired
    private PasswordService passwordService;

    @GetMapping(value = "/generate")
    public ResponseEntity<String> generate(
            @RequestParam(value = "length", defaultValue = "8") Integer length,
            @RequestParam(value = "type", defaultValue = "alphanumeric") String type
    ) {
        if(WORDS.equalsIgnoreCase(type)) {
            return ResponseEntity.ok(passwordService.generateWordPassword(length));
        } else {
            return ResponseEntity.ok(passwordService.generatePassword(length));
        }
    }

    @PostMapping(value = "/save")
    public ResponseEntity<Void> save(
            @RequestBody CredentialDTO credentialDTO
    ) throws Exception {
        // Todo - Encryption Service
        passwordService.save(credentialDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/all")
    public List<ViewCredentialDTO> getAllEntries() {
        return passwordService.getAllEntries();
    }

    public ResponseEntity<String> revealPassword(
            @PathVariable(value = "id") Long id,
            @RequestBody String masterKey
    ) {
        return ResponseEntity.ok(passwordService.getDecryptedPassword(id, masterKey));
    }
}
