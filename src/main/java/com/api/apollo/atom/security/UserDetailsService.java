package com.api.apollo.atom.security;

import java.util.Optional;

import com.api.apollo.atom.entity.master.MTLocation;
import com.api.apollo.atom.repository.master.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.api.apollo.atom.entity.ApplicationUser;
import com.api.apollo.atom.repository.UserRepository;
import org.springframework.util.StringUtils;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    LocationRepository locationRepository;
    private final AccountStatusUserDetailsChecker detailsChecker = new AccountStatusUserDetailsChecker();

    @Override
    public final LoginUser loadUserByUsername(String username) throws UsernameNotFoundException, DisabledException {
        LoginUser currentUser = null;
        final Optional<ApplicationUser> user = userRepository.findOneByUserIdIgnoreCase(username);
        if (user.isPresent()) {
            /*if (user.getUserDetails().getActive()) {
                currentUser = new LoginUser(user);
            } else {
                throw new DisabledException("User is not activated (Disabled User)");
            }*/
            Optional<MTLocation> mtLocation = locationRepository.findByLocationId(user.get().getPlantCode());
            if(mtLocation.isPresent() && !StringUtils.isEmpty(mtLocation.get().getLocationClass()) && mtLocation.get().getLocationClass().contentEquals("EXT_WAREHOUSE")){
                user.get().setIsExtWarehouse(true);
            }
        	currentUser = new LoginUser(user.get());
            detailsChecker.check(currentUser);
        } else {
            new UsernameNotFoundException("User not found");
        }
        return currentUser;
    }
}
