package gr.hua.dit.fittrack.config;

import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.model.PersonType;
import gr.hua.dit.fittrack.core.repository.PersonRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final PersonRepository personRepository;

    public CustomAuthenticationSuccessHandler(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();

        Person person = personRepository.findByEmailAddressIgnoreCase(username).orElse(null);

        if (person == null) {
            response.sendRedirect("/login?error");
            return;
        }

        if (person.getType() == PersonType.TRAINER) {
            response.sendRedirect("/trainer");
        } else if (person.getType() == PersonType.USER) {
            response.sendRedirect("/profile");
        } else {
            response.sendRedirect("/login?error");
        }
    }
}
