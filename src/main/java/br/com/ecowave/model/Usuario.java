package br.com.ecowave.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "USUARIOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends RepresentationModel<Usuario> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private long idUsuario;

    @Column(name = "NOME_USUARIO", nullable = false, unique = true)
    @NotNull(message = "O nome do usuário não pode ser nulo")
    @NotEmpty(message = "O nome do usuário não pode estar vazio")
    private String nomeUsuario;

    @Column(name = "SENHA_USUARIO", nullable = false)
    @NotNull(message = "A senha do usuário não pode ser nula")
    @NotEmpty(message = "A senha do usuário não pode estar vazia")
    private String senhaUsuario;

    @Column(name = "EMAIL_USUARIO", nullable = false, unique = true)
    @NotNull(message = "O email do usuário não pode ser nulo")
    @NotEmpty(message = "O email do usuário não pode estar vazio")
    @Email(message = "O email do usuário deve ser válido")
    private String emailUsuario;

    @Column(name = "DATA_REGISTRO_USUARIO", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataRegistroUsuario;

    @Column(name = "LOCALIZACAO_USUARIO")
    private String localizacaoUsuario;
}
