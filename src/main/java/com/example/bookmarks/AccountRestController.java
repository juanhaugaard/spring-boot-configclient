package com.example.bookmarks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("v1/api/accounts")
public class AccountRestController {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountRestController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Collection<Account> readAccounts() {
        return accountRepository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@RequestBody Account input) {
        Optional<Account> account = accountRepository.findByUsername(input.getUsername());
        if (!account.isPresent()) {
            Account result = accountRepository.save(new Account(input.getUsername(), input.getPassword()));
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().path("/{id}")
                    .buildAndExpand(result.getUsername()).toUri();

            return ResponseEntity.created(location).body(result);
        }
        return ResponseEntity.badRequest().body("username is already used");
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{userId}")
    ResponseEntity<?> update(@PathVariable String userId,
                             @RequestBody String password) {
        Optional<Account> account = accountRepository.findByUsername(userId);
        if (account.isPresent()) {
            account.get().setPassword(password);
            accountRepository.flush();
            return ResponseEntity.ok(account);
        }
        throw new UserNotFoundException(userId);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{userId}")
    ResponseEntity<?> delete(@PathVariable String userId) {
        Optional<Account> account = accountRepository.findByUsername(userId);
        if (account.isPresent()) {
            accountRepository.delete(account.get().getId());
            return ResponseEntity.noContent().build();
        }
        throw new UserNotFoundException(userId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{userId}")
    public Optional<Account> readAccount(@PathVariable String userId) {
        return accountRepository.findByUsername(userId);
    }
}
