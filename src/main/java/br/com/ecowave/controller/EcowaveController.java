package br.com.ecowave.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import br.com.ecowave.model.Localizacao;
import br.com.ecowave.model.Amigos;
import br.com.ecowave.service.AmigosService;
import br.com.ecowave.service.LocalizacaoService;
import br.com.ecowave.model.Usuario;
import br.com.ecowave.service.UsuarioService;
import br.com.ecowave.service.ItensRecicladosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import br.com.ecowave.model.ItensReciclados;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import javax.validation.Valid;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@Validated
@RequestMapping("/api")
public class EcowaveController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ItensRecicladosService itensRecicladosService;

    @Autowired
    private AmigosService amigosService;

    @Autowired
    private LocalizacaoService localizacaoService;

    @ApiOperation(value = "Registrar Usuário")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Usuário registrado com sucesso"),
            @ApiResponse(code = 400, message = "Erro nos dados fornecidos")
    })
    @PostMapping("/registrar")
    public ResponseEntity<String> registrarUsuario(
            @ApiParam(value = "Informações do usuário para registro", required = true) @Valid @RequestBody Usuario usuario) {
        usuarioService.registrarUsuario(usuario);
        return new ResponseEntity<>("Usuário registrado com sucesso.", HttpStatus.CREATED);
    }

    @ApiOperation(value = "Login Usuário")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Login bem-sucedido"),
            @ApiResponse(code = 401, message = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @ApiParam(value = "Nome do usuário", required = true) @RequestParam String nomeUsuario,
            @ApiParam(value = "Senha do usuário", required = true) @RequestParam String senha) {
        boolean loginSucesso = usuarioService.verificarCredenciais(nomeUsuario, senha);
        if (loginSucesso) {
            return new ResponseEntity<>("Login bem-sucedido.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Credenciais inválidas.", HttpStatus.UNAUTHORIZED);
        }
    }

    @ApiOperation(value = "Itens Reciclados")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Itens reciclados retornados com sucesso"),
            @ApiResponse(code = 204, message = "Nenhum item reciclado encontrado"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @GetMapping("/item")
    public ResponseEntity<Page<ItensReciclados>> getAllItensReciclados(
            @ApiParam(value = "Tipo do item a ser filtrado") @RequestParam(required = false) String tipoItem,
            @ApiParam(value = "Número da página") @RequestParam(defaultValue = "0") int page,
            @ApiParam(value = "Tamanho da página") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ItensReciclados> itensRecicladosPage;

            if (tipoItem == null)
                itensRecicladosPage = itensRecicladosService.findAllPaginado(page, size);
            else
                itensRecicladosPage = itensRecicladosService.findByTipoItemContainingPaginado(tipoItem, page, size);

            if (itensRecicladosPage.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(itensRecicladosPage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Quantidade total de Itens Reciclados")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Quantidade total de itens reciclados retornada com sucesso"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @GetMapping("/item/total")
    public ResponseEntity<Long> getTotalItens() {
        try {
            long totalItens = itensRecicladosService.findTotalQuantidadeItens();
            return new ResponseEntity<>(totalItens, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retorna todos os Usuários Cadastrados")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Usuários retornados com sucesso"),
            @ApiResponse(code = 204, message = "Nenhum usuário encontrado"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @GetMapping("/usuarios")
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> obterTodosUsuariosPaginado(
            @ApiParam(value = "Número da página") @RequestParam(defaultValue = "0") int page,
            @ApiParam(value = "Tamanho da página") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Usuario> usuariosPage = usuarioService.obterTodosUsuariosPaginado(PageRequest.of(page, size));
            if (usuariosPage.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            List<EntityModel<Usuario>> usuarios = usuariosPage.stream()
                    .map(usuario -> EntityModel.of(usuario,
                            linkTo(methodOn(EcowaveController.class).obterTodosUsuariosPaginado(page, size)).withSelfRel(),
                            linkTo(methodOn(EcowaveController.class).getItensByUsuarioId(usuario.getIdUsuario(), page, size)).withRel("itens"),
                            linkTo(methodOn(EcowaveController.class).getTotalItensByUsuarioId(usuario.getIdUsuario())).withRel("totalItens")))
                    .collect(Collectors.toList());

            CollectionModel<EntityModel<Usuario>> resource = CollectionModel.of(usuarios,
                    linkTo(methodOn(EcowaveController.class).obterTodosUsuariosPaginado(page, size)).withSelfRel());

            return ResponseEntity.ok(resource);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Itens reciclados por usuário")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Itens reciclados retornados com sucesso"),
            @ApiResponse(code = 204, message = "Nenhum item reciclado encontrado"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @GetMapping("/usuario/{id}/item")
    public ResponseEntity<CollectionModel<EntityModel<ItensReciclados>>> getItensByUsuarioId(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable("id") long id,
            @ApiParam(value = "Número da página") @RequestParam(defaultValue = "0") int page,
            @ApiParam(value = "Tamanho da página") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ItensReciclados> itensRecicladosPage = itensRecicladosService.findByUsuarioIdPaginado(id, page, size);
            if (itensRecicladosPage.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            List<EntityModel<ItensReciclados>> itensReciclados = itensRecicladosPage.stream()
                    .map(item -> EntityModel.of(item,
                            linkTo(methodOn(EcowaveController.class).getItensByUsuarioId(id, page, size)).withSelfRel(),
                            linkTo(methodOn(EcowaveController.class).getItemByUsuarioIdAndItemId(id, item.getIdItem())).withRel("item"),
                            linkTo(methodOn(EcowaveController.class).deleteItemByUsuarioIdAndItemId(id, item.getIdItem())).withRel("delete")))
                    .collect(Collectors.toList());

            CollectionModel<EntityModel<ItensReciclados>> resource = CollectionModel.of(itensReciclados,
                    linkTo(methodOn(EcowaveController.class).getItensByUsuarioId(id, page, size)).withSelfRel());

            return ResponseEntity.ok(resource);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Criar Item Reciclado para Usuário")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Item reciclado adicionado com sucesso"),
            @ApiResponse(code = 404, message = "Usuário não encontrado"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @PostMapping("/usuario/{id}/item")
    public ResponseEntity<String> createItemReciclado(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable long id,
            @ApiParam(value = "Informações do item reciclado", required = true) @RequestBody ItensReciclados itensReciclados) {
        try {
            Usuario usuario = usuarioService.findByIdUsuario(id);
            if(usuario == null) {
                return new ResponseEntity<>("Usuário não encontrado.", HttpStatus.NOT_FOUND);
            }
            itensReciclados.setUsuario(usuario);
            itensRecicladosService.save(itensReciclados);
            return new ResponseEntity<>("Item Reciclado foi adicionado com sucesso.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao adicionar item reciclado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Quantidade total de Itens Reciclados por Usuário")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Quantidade total de itens reciclados retornada com sucesso"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @GetMapping("/usuario/{id}/item/total")
    public ResponseEntity<Long> getTotalItensByUsuarioId(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable("id") long id) {
        try {
            long totalItens = itensRecicladosService.findTotalQuantidadeItensByUsuarioId(id);
            return new ResponseEntity<>(totalItens, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Atualizar Item Reciclado")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Item reciclado atualizado com sucesso"),
            @ApiResponse(code = 404, message = "Item reciclado não encontrado"),
            @ApiResponse(code = 401, message = "Não autorizado a atualizar este item"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @PutMapping("/usuario/{id}/item/{itemId}")
    public ResponseEntity<String> updateItemReciclado(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable long id,
            @ApiParam(value = "ID do item reciclado", required = true) @PathVariable long itemId,
            @ApiParam(value = "Informações atualizadas do item reciclado", required = true) @RequestBody ItensReciclados updatedItem) {
        try {
            ItensReciclados existingItem = itensRecicladosService.findById(itemId);
            if (existingItem == null) {
                return new ResponseEntity<>("Item Reciclado não encontrado.", HttpStatus.NOT_FOUND);
            }

            if (existingItem.getUsuario().getIdUsuario() != id) {
                return new ResponseEntity<>("Não autorizado a atualizar este item.", HttpStatus.UNAUTHORIZED);
            }

            existingItem.setTipoItem(updatedItem.getTipoItem());
            existingItem.setLocalizacaoItem(updatedItem.getLocalizacaoItem());
            existingItem.setQuantidadeItem(updatedItem.getQuantidadeItem());

            itensRecicladosService.save(existingItem);

            return new ResponseEntity<>("Item Reciclado foi atualizado com sucesso.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao atualizar o item reciclado.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Obter Item Reciclado por Usuário e Item")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Item reciclado retornado com sucesso"),
            @ApiResponse(code = 404, message = "Item reciclado não encontrado"),
            @ApiResponse(code = 401, message = "Não autorizado a visualizar este item"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @GetMapping("/usuario/{id}/item/{itemId}")
    public ResponseEntity<EntityModel<ItensReciclados>> getItemByUsuarioIdAndItemId(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable("id") long id,
            @ApiParam(value = "ID do item reciclado", required = true) @PathVariable("itemId") long itemId) {
        try {
            ItensReciclados item = itensRecicladosService.findById(itemId);

            if (item == null || item.getUsuario().getIdUsuario() != id) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            EntityModel<ItensReciclados> resource = EntityModel.of(item,
                    linkTo(methodOn(EcowaveController.class).getItemByUsuarioIdAndItemId(id, itemId)).withSelfRel(),
                    linkTo(methodOn(EcowaveController.class).getItensByUsuarioId(id, 0, 10)).withRel("itens"),
                    linkTo(methodOn(EcowaveController.class).deleteItemByUsuarioIdAndItemId(id, itemId)).withRel("delete"));

            return ResponseEntity.ok(resource);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Remover Item Reciclado por Usuário e Item")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Item removido com sucesso"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @DeleteMapping("/usuario/{id}/item/{itemId}")
    public ResponseEntity<String> deleteItemByUsuarioIdAndItemId(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable("id") long id,
            @ApiParam(value = "ID do item reciclado", required = true) @PathVariable("itemId") long itemId) {
        try {
            itensRecicladosService.deleteById(itemId);
            return new ResponseEntity<>("Item removido com sucesso.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao remover o item.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Adicionar Amigo para Usuário")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Amigo adicionado com sucesso"),
            @ApiResponse(code = 400, message = "idAmigo é necessário ou usuário não pode adicionar a si mesmo"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @PostMapping("/usuario/{id}/amigos")
    public ResponseEntity<String> adicionarAmigo(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable("id") Long idUsuario,
            @ApiParam(value = "Payload contendo o ID do amigo", required = true) @RequestBody Map<String, Long> payload) {
        try {
            Long idAmigo = payload.get("idAmigo");
            if (idAmigo == null) {
                return new ResponseEntity<>("idAmigo é necessário", HttpStatus.BAD_REQUEST);
            }
            if (idUsuario.equals(idAmigo)) {
                return new ResponseEntity<>("Um usuário não pode adicionar a si mesmo como amigo.", HttpStatus.BAD_REQUEST);
            }
            amigosService.adicionarAmigo(idUsuario, idAmigo);
            return new ResponseEntity<>("Amigo adicionado com sucesso.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao adicionar amigo.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Obter Amigos do Usuário")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Amigos retornados com sucesso"),
            @ApiResponse(code = 204, message = "Nenhum amigo encontrado"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @GetMapping("/usuario/{id}/amigos")
    public ResponseEntity<List<Amigos>> obterAmigos(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable("id") Long idUsuario) {
        try {
            List<Amigos> amigos = amigosService.obterAmigos(idUsuario);
            if (amigos.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(amigos, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Remover Amigo do Usuário")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Amigo removido com sucesso"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @DeleteMapping("/usuario/{id}/amigos/{idAmigo}")
    public ResponseEntity<String> removerAmigo(
            @ApiParam(value = "ID do usuário", required = true) @PathVariable("id") Long idUsuario,
            @ApiParam(value = "ID do amigo", required = true) @PathVariable("idAmigo") Long idAmigo) {
        try {
            amigosService.removerAmigo(idUsuario, idAmigo);
            return new ResponseEntity<>("Amigo removido com sucesso.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erro ao remover amigo.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Criar Localização")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Localização criada com sucesso"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @PostMapping("/localizacoes")
    public ResponseEntity<Localizacao> criarLocalizacao(
            @ApiParam(value = "Informações da localização", required = true) @RequestBody Localizacao localizacao) {
        try {
            Localizacao novaLocalizacao = localizacaoService.salvarLocalizacao(localizacao);
            novaLocalizacao.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EcowaveController.class).obterLocalizacaoPorId(novaLocalizacao.getIdLocalizacao())).withSelfRel());
            return new ResponseEntity<>(novaLocalizacao, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ApiOperation(value = "Obter Todas as Localizações Paginado")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Localizações retornadas com sucesso"),
            @ApiResponse(code = 204, message = "Nenhuma localização encontrada"),
            @ApiResponse(code = 500, message = "Erro interno do servidor")
    })
    @GetMapping("/localizacoes")
    public ResponseEntity<Page<Localizacao>> obterTodasLocalizacoesPaginado(
            @ApiParam(value = "Número da página") @RequestParam(defaultValue = "0") int page,
            @ApiParam(value = "Tamanho da página") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Localizacao> localizacoesPage = localizacaoService.obterTodasLocalizacoesPaginado(PageRequest.of(page, size));
            if (localizacoesPage.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            localizacoesPage.forEach(localizacao -> {
                long id = localizacao.getIdLocalizacao();
                localizacao.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EcowaveController.class).obterLocalizacaoPorId(id)).withSelfRel());
            });

            return new ResponseEntity<>(localizacoesPage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Obter Localização por ID")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Localização retornada com sucesso"),
            @ApiResponse(code = 404, message = "Localização não encontrada")
    })
    @GetMapping("/localizacoes/{id}")
    public ResponseEntity<EntityModel<Localizacao>> obterLocalizacaoPorId(
            @ApiParam(value = "ID da localização", required = true) @PathVariable("id") Long id) {
        Optional<Localizacao> localizacao = localizacaoService.obterLocalizacaoPorId(id);
        return localizacao.map(value -> {
            EntityModel<Localizacao> localizacaoModel = EntityModel.of(value);
            localizacaoModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(EcowaveController.class).obterTodasLocalizacoesPaginado(0, 10)).withRel("all-locations"));
            return new ResponseEntity<>(localizacaoModel, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
