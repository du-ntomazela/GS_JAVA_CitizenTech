package br.com.fiap3espv.SemaforoInteligente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integracao: sobe a aplicacao inteira com banco H2 em memoria,
 * executa as migracoes do Flyway (incluindo os dados iniciais) e percorre os fluxos pela API REST.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SemaforoInteligenteApplicationTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JsonMapper jsonMapper;

    private String login(String usuario, String senha) throws Exception {
        String resposta = mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"%s\",\"senha\":\"%s\"}".formatted(usuario, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + jsonMapper.readTree(resposta).get("tokenJWT").asString();
    }

    @Test
    @DisplayName("Health check é público")
    void healthCheck() throws Exception {
        mvc.perform(get("/health-check")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Documentação OpenAPI (Swagger) é gerada automaticamente")
    void documentacaoSwagger() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Semáforo Inteligente"))
                .andExpect(jsonPath("$.paths['/emergencia/{id}/posicao']").exists());
    }

    @Test
    @DisplayName("Login com senha errada devolve 401")
    void loginInvalido() throws Exception {
        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Endpoints protegidos exigem token e respeitam o perfil")
    void controleDeAcesso() throws Exception {
        mvc.perform(get("/cruzamento")).andExpect(status().isForbidden());

        String socorrista = login("socorrista", "socorrista123");
        mvc.perform(post("/cruzamento").header("Authorization", socorrista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"cruzamento-99\",\"nome\":\"Teste\",\"localizacao\":{\"latitude\":-23.5,\"longitude\":-46.6}}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Migrações criam os cruzamentos iniciais e o admin consegue cadastrar um novo")
    void cadastroDeCruzamento() throws Exception {
        String admin = login("admin", "admin123");

        mvc.perform(post("/cruzamento").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"cruzamento-50\",\"nome\":\"Av. Faria Lima x R. dos Pinheiros\",\"localizacao\":{\"latitude\":-23.5670,\"longitude\":-46.6930}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status_conexao").value("OFFLINE"))
                .andExpect(jsonPath("$.modo_operacao").value("ADAPTATIVO"));

        mvc.perform(post("/cruzamento").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"Codigo Invalido\",\"nome\":\"\",\"localizacao\":{\"latitude\":-23.5,\"longitude\":-46.6}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Telemetria de um cruzamento adaptativo gera um plano e o operador consegue intervir manualmente")
    void telemetriaEPlanoManual() throws Exception {
        String operador = login("operador", "operador123");

        mvc.perform(post("/cruzamento/8/leitura").header("Authorization", operador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vias":[
                                  {"via":"NORTE","fila":25,"fluxo_por_minuto":18},
                                  {"via":"SUL","fila":20,"fluxo_por_minuto":15},
                                  {"via":"LESTE","fila":4,"fluxo_por_minuto":5},
                                  {"via":"OESTE","fila":3,"fluxo_por_minuto":4}]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leituras_registradas").value(4))
                .andExpect(jsonPath("$.plano_aplicado.origem").value("ALGORITMO"));

        mvc.perform(get("/cruzamento/8").header("Authorization", operador))
                .andExpect(jsonPath("$.status_conexao").value("ONLINE"));

        mvc.perform(post("/cruzamento/8/plano").header("Authorization", operador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verde_norte_sul\":40,\"verde_leste_oeste\":30,\"observacao\":\"Evento no Allianz Parque\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origem").value("MANUAL"));

        mvc.perform(get("/cruzamento/8/plano").header("Authorization", operador))
                .andExpect(jsonPath("$.verde_norte_sul").value(40));
    }

    @Test
    @DisplayName("Fluxo completo da onda verde: abrir, liberar o primeiro cruzamento e encerrar")
    void fluxoDaOndaVerde() throws Exception {
        String socorrista = login("socorrista", "socorrista123");

        String resposta = mvc.perform(post("/emergencia").header("Authorization", socorrista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id_ambulancia":1,
                                 "origem":{"latitude":-23.55455,"longitude":-46.66339},
                                 "destino":{"latitude":-23.5713,"longitude":-46.6447},
                                 "descricao_destino":"Hospital - Av. Paulista"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.rota[0].codigo").value("cruzamento-01"))
                .andExpect(jsonPath("$.rota[0].situacao").value("LIBERADO"))
                .andExpect(jsonPath("$.rota[1].situacao").value("AGUARDANDO"))
                .andReturn().getResponse().getContentAsString();
        JsonNode emergencia = jsonMapper.readTree(resposta);
        long id = emergencia.get("id").asLong();
        assertThat(emergencia.get("rota").size()).isEqualTo(7);

        mvc.perform(post("/emergencia/{id}/posicao", id).header("Authorization", socorrista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\":-23.5703,\"longitude\":-46.6725}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/emergencia/{id}/posicao", id).header("Authorization", socorrista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\":-23.55500,\"longitude\":-46.66290}"))
                .andExpect(status().isOk());

        mvc.perform(post("/emergencia", id).header("Authorization", socorrista)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id_ambulancia":1,
                                 "origem":{"latitude":-23.55455,"longitude":-46.66339},
                                 "destino":{"latitude":-23.5713,"longitude":-46.6447},
                                 "descricao_destino":"Outro"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ambulância já possui uma emergência ativa!"));

        mvc.perform(post("/emergencia/{id}/encerramento", id).header("Authorization", socorrista))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCERRADA"))
                .andExpect(jsonPath("$.semaforos_liberados").value(1))
                .andExpect(jsonPath("$.rota[0].situacao").value("RESTAURADO"));
    }
}
