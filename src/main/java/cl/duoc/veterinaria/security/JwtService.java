package cl.duoc.veterinaria.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long vigenciaMs;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiration}") long vigenciaMs) {
        // Con menos de 32 caracteres la librería rechaza la clave
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.vigenciaMs = vigenciaMs;
    }

    // Crea el token firmado con el id, el correo y el rol, y con fecha de vencimiento
    public String generarToken(Long id, String correo, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(correo)
                .claim("id", id)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + vigenciaMs))
                .signWith(clave)
                .compact();
    }

    // Verifica la firma y el vencimiento. Si algo falla, lanza una excepción
    public UsuarioAutenticado validar(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long id = ((Number) claims.get("id")).longValue();
        return new UsuarioAutenticado(id, claims.getSubject(), claims.get("rol", String.class));
    }
}