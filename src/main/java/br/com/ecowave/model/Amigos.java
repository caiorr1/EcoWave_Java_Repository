package br.com.ecowave.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "AMIGOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(Amigos.AmigosId.class)
public class Amigos {

    @Id
    @Column(name = "ID_USUARIO", nullable = false)
    @NotNull(message = "O ID do usuário não pode ser nulo")
    private Long idUsuario;

    @Id
    @Column(name = "ID_AMIGO", nullable = false)
    @NotNull(message = "O ID do amigo não pode ser nulo")
    private Long idAmigo;

    @Column(name = "DATA_AMIZADE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dataAmizade = new Date();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmigosId implements Serializable {
        private Long idUsuario;

        private Long idAmigo;
    }
}
