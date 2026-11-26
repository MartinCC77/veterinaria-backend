package cl.duoc.veterinaria.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// Se ejecuta en cada petición: lee el encabezado "Authorization: Bearer <token>"
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            try {
                UsuarioAutenticado usuario = jwtService.validar(encabezado.substring(7));
                // El rol se convierte en "ROLE_ADMINISTRADOR", "ROLE_DUENO", etc.
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        usuario, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().toUpperCase())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (Exception e) {
                // Token alterado o vencido: se queda sin autenticar y Spring responde 401
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}