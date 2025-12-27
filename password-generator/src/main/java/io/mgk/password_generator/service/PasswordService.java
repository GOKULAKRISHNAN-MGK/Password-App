package io.mgk.password_generator.service;

import io.mgk.password_generator.dto.CredentialDTO;
import io.mgk.password_generator.dto.ViewCredentialDTO;
import io.mgk.password_generator.model.Credential;
import io.mgk.password_generator.repository.CredentialRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class PasswordService {

    private static final String LOWERCASE = " abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}";

    private final List<String> dictionary = new ArrayList<>();
    private final SecureRandom RANDOM = new SecureRandom();
    private SecretKey masterKey;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private EncryptionService encryptionService;

    @PostConstruct
    public void init() throws IOException {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("words.txt"))))) {
            String line;
            while((line = br.readLine()) != null) {
                dictionary.add(line.trim());
            }
        } catch (IOException e) {
            throw new IOException("Error reading words.txt", e);
        } catch (Exception e) {
            throw new RuntimeException("Error reading words.txt", e);
        }
    }
    public String generatePassword(int length) {
        if(length < 6) {
            throw new IllegalArgumentException("Password length must be at least 6 characters");
        }
        String CHARACTERS = LOWERCASE + UPPERCASE + DIGITS + SYMBOLS;

        String mandatoryChars = Stream.of(LOWERCASE, UPPERCASE, DIGITS, SYMBOLS)
                .map(s -> s.charAt(RANDOM.nextInt(s.length())))
                .map(Objects::toString)
                .collect(Collectors.joining());

        String remainingChars = IntStream.range(0, length - mandatoryChars.length())
                .mapToObj(i -> CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())))
                .map(Objects::toString)
                .collect(Collectors.joining());

        return Stream.of(mandatoryChars, remainingChars)
                .map(s -> s.split(""))
                .flatMap(Stream::of)
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return String.join("", collected);
                }));
    }

    public String generateWordPassword(int count) {
        if(count < 1) {
            throw new IllegalArgumentException("Word count must be at least 1");
        }

        return IntStream.range(0, count)
                .mapToObj(i -> {
                    // Pick a random word from the dictionary
                    String word = dictionary.get(RANDOM.nextInt(dictionary.size()));

                    // If it's not the last word, append a random number and symbol
                    if (i < count - 1) {
                        char symbol = SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length()));
                        int number = RANDOM.nextInt(10);
                        return word + number + symbol;
                    }
                    return word;
                })
                .collect(Collectors.joining());
    }

    public void save(CredentialDTO credentialDTO) throws Exception {
        byte[] iv = encryptionService.generateIV();
        String encryptedPassword = encryptionService.encrypt(credentialDTO.getPassword(), masterKey, iv);
        Credential credential = Credential.builder()
                .username(credentialDTO.getUsername())
                .password(encryptedPassword)
                .siteName(credentialDTO.getSiteName())
                .initVector(Base64.getEncoder().encodeToString(iv))
                .createdAt(LocalDateTime.now())
                .build();
        credentialRepository.save(credential);
    }

    public List<ViewCredentialDTO> getAllEntries() {
        return credentialRepository.findAll().stream()
                .map(credential -> ViewCredentialDTO.builder()
                        .id(Long.valueOf(credential.getId()))
                        .username(credential.getUsername())
                        .password("**********")
                        .siteName(credential.getSiteName())
                        .build())
                .toList();
    }
}
