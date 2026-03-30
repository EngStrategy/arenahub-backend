package com.engstrategy.arenahub_api.service.impl;

import com.engstrategy.arenahub_api.dto.SorteadorRequestDTO;
import com.engstrategy.arenahub_api.dto.SorteadorResponseDTO;
import com.engstrategy.arenahub_api.dto.TeamPlayerDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SorteadorServiceImpl {

    private static final Pattern PLAYER_PATTERN = Pattern.compile("^(\\d+\\s*[.\\-]?|-)\\s*(.*)");
    private static final Pattern CLEAN_NAME_PATTERN = Pattern.compile(
            "(?i)\\s?✅|\\sR\\$\\s?\\d+(,\\d+)?|\\spg\\b|\\sok\\b|\\spix\\b|\\spago\\b|\\s?\\(vai atrasar\\)|\\ssem número|\\s?\\(confirmado\\)|\\s?- confirmado|\\s?- pago|\\s?\\(goleiro\\)"
    );

    private enum Sessao { MAIN, SUPLENTES, GOLEIROS, ESPERA }

    public SorteadorResponseDTO processarSorteio(SorteadorRequestDTO request) {
        if (!isRequestValid(request)) {
            return new SorteadorResponseDTO(new ArrayList<>());
        }

        List<TeamPlayerDTO> jogadores = new ArrayList<>();
        List<TeamPlayerDTO> goleiros = new ArrayList<>();

        extrairJogadores(request.getLista(), jogadores, goleiros);

        int qtdTimes = (request.getQuantidadeTimes() != null && request.getQuantidadeTimes() > 0)
                ? request.getQuantidadeTimes()
                : 2;

        return new SorteadorResponseDTO(distribuirEmTimes(jogadores, goleiros, qtdTimes));
    }

    private boolean isRequestValid(SorteadorRequestDTO request) {
        return request != null && request.getLista() != null && !request.getLista().isEmpty();
    }

    private void extrairJogadores(String lista, List<TeamPlayerDTO> jogadores, List<TeamPlayerDTO> goleiros) {
        String[] linhas = lista.split("\\r?\\n");
        Sessao sessaoAtual = Sessao.MAIN;

        for (String linha : linhas) {
            linha = linha.trim();
            if (linha.isEmpty()) continue;

            Sessao novaSessao = verificarMudancaSessao(linha);
            if (novaSessao != null) {
                sessaoAtual = novaSessao;
                continue;
            }

            if (sessaoAtual == Sessao.SUPLENTES || sessaoAtual == Sessao.ESPERA) {
                continue;
            }

            processarLinhaJogador(linha, sessaoAtual, jogadores, goleiros);
        }
    }

    private Sessao verificarMudancaSessao(String linha) {
        String lower = linha.toLowerCase();
        if (lower.startsWith("suplentes")) return Sessao.SUPLENTES;
        if (lower.startsWith("goleiros")) return Sessao.GOLEIROS;
        if (lower.startsWith("lista de espera") || lower.startsWith("espera")) return Sessao.ESPERA;
        return null;
    }

    private void processarLinhaJogador(String linha, Sessao sessaoAtual, List<TeamPlayerDTO> jogadores, List<TeamPlayerDTO> goleiros) {
        Matcher matcher = PLAYER_PATTERN.matcher(linha);
        if (!matcher.matches()) return;

        String rawName = matcher.group(2).trim();
        if (rawName.isEmpty() || rawName.matches("(?i)pago|pix")) return;

        boolean isGoleiro = sessaoAtual == Sessao.GOLEIROS || rawName.toLowerCase().contains("(goleiro)");

        String cleanName = rawName.split(" - ")[0];
        cleanName = CLEAN_NAME_PATTERN.matcher(cleanName).replaceAll("").trim();

        if (cleanName.isEmpty()) return;
        if (cleanName.startsWith("-")) {
            cleanName = cleanName.substring(1).trim();
        }

        if (isGoleiro) {
            goleiros.add(new TeamPlayerDTO(cleanName, true));
        } else {
            jogadores.add(new TeamPlayerDTO(cleanName, false));
        }
    }

    private List<List<TeamPlayerDTO>> distribuirEmTimes(List<TeamPlayerDTO> jogadores, List<TeamPlayerDTO> goleiros, int qtdTimes) {
        Collections.shuffle(goleiros);
        Collections.shuffle(jogadores);

        List<List<TeamPlayerDTO>> times = new ArrayList<>();
        for (int i = 0; i < qtdTimes; i++) {
            times.add(new ArrayList<>());
        }

        int currentTeam = 0;
        for (TeamPlayerDTO goleiro : goleiros) {
            times.get(currentTeam).add(goleiro);
            currentTeam = (currentTeam + 1) % qtdTimes;
        }

        for (TeamPlayerDTO jogador : jogadores) {
            times.get(currentTeam).add(jogador);
            currentTeam = (currentTeam + 1) % qtdTimes;
        }

        return times;
    }
}