import com.chrishurd.security.Role
import com.chrishurd.security.User
import com.chrishurd.security.UserRole

class BootStrap {

    def init = { servletContext ->
        def password = 'password'

        def roleAdmin = new Role(authority: 'ROLE_ADMIN').save()
        def roleUser = new Role(authority: 'ROLE_USER').save()

        def user = new User(username: 'user', password: password, enabled: true).save()
        def admin = new User(username: 'admin', password: password, enabled: true).save()

        UserRole.create(user, roleUser)
        UserRole.create(admin, roleUser)
        UserRole.create(admin, roleAdmin, true)
    }
    def destroy = {
    }
}
