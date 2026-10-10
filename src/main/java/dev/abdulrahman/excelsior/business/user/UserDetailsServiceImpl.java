package dev.abdulrahman.excelsior.business.user;

import dev.abdulrahman.excelsior.persistence.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository repository;

    public UserDetailsServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return new UserAdapter(
                repository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Couldn't find user with name " + username))
        );
    }
}
