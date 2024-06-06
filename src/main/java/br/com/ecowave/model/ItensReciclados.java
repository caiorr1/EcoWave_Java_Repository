package br.com.ecowave.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "ITENSRECICLADOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItensReciclados {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private long idItem;

    @Column(name = "tipo_item")
    @NotEmpty(message = "O tipo do item não pode estar vazio")
    private String tipoItem;

    @Column(name = "data_coleta_item")
    private Date dataColetaItem;

    @Column(name = "localizacao_item")
    private String localizacaoItem;

    @Column(name = "quantidade_item")
    @NotNull(message = "A quantidade do item não pode ser nula")
    private int quantidadeItem;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    // Constructors, getters, setters
}
