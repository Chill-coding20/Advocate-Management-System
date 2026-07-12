package advocate.com.advocate_app.websocket;

import java.security.Principal;

public class UserPrincipal implements Principal {

    private final String email;

    public UserPrincipal(String email) {
        this.email = email;
    }

    @Override
    public String getName() {
        return email;
    }

    public String getEmail() {
        return email;
    }
}
