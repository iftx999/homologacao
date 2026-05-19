package com.example.homologacao.dto;

import java.util.Map;

public class DashboardResumoResponse {

    private Cards cards;
    private Map<String, Long> ichosPorStatus;
    private Map<String, Long> implantacoesPorStatus;

    public DashboardResumoResponse(Cards cards,
                                   Map<String, Long> ichosPorStatus,
                                   Map<String, Long> implantacoesPorStatus) {
        this.cards = cards;
        this.ichosPorStatus = ichosPorStatus;
        this.implantacoesPorStatus = implantacoesPorStatus;
    }

    public Cards getCards() {
        return cards;
    }

    public void setCards(Cards cards) {
        this.cards = cards;
    }

    public Map<String, Long> getIchosPorStatus() {
        return ichosPorStatus;
    }

    public void setIchosPorStatus(Map<String, Long> ichosPorStatus) {
        this.ichosPorStatus = ichosPorStatus;
    }

    public Map<String, Long> getImplantacoesPorStatus() {
        return implantacoesPorStatus;
    }

    public void setImplantacoesPorStatus(Map<String, Long> implantacoesPorStatus) {
        this.implantacoesPorStatus = implantacoesPorStatus;
    }

    public static class Cards {

        private long totalImplantacoes;
        private long totalModulos;
        private long totalIchos;
        private long implantacoesEmAndamento;
        private long implantacoesFinalizadas;
        private long implantacoesCanceladas;
        private long ichosPendentes;
        private long ichosComFalha;

        public Cards(long totalImplantacoes,
                     long totalModulos,
                     long totalIchos,
                     long implantacoesEmAndamento,
                     long implantacoesFinalizadas,
                     long implantacoesCanceladas,
                     long ichosPendentes,
                     long ichosComFalha) {
            this.totalImplantacoes = totalImplantacoes;
            this.totalModulos = totalModulos;
            this.totalIchos = totalIchos;
            this.implantacoesEmAndamento = implantacoesEmAndamento;
            this.implantacoesFinalizadas = implantacoesFinalizadas;
            this.implantacoesCanceladas = implantacoesCanceladas;
            this.ichosPendentes = ichosPendentes;
            this.ichosComFalha = ichosComFalha;
        }

        public long getTotalImplantacoes() {
            return totalImplantacoes;
        }

        public void setTotalImplantacoes(long totalImplantacoes) {
            this.totalImplantacoes = totalImplantacoes;
        }

        public long getTotalModulos() {
            return totalModulos;
        }

        public void setTotalModulos(long totalModulos) {
            this.totalModulos = totalModulos;
        }

        public long getTotalIchos() {
            return totalIchos;
        }

        public void setTotalIchos(long totalIchos) {
            this.totalIchos = totalIchos;
        }

        public long getImplantacoesEmAndamento() {
            return implantacoesEmAndamento;
        }

        public void setImplantacoesEmAndamento(long implantacoesEmAndamento) {
            this.implantacoesEmAndamento = implantacoesEmAndamento;
        }

        public long getImplantacoesFinalizadas() {
            return implantacoesFinalizadas;
        }

        public void setImplantacoesFinalizadas(long implantacoesFinalizadas) {
            this.implantacoesFinalizadas = implantacoesFinalizadas;
        }

        public long getImplantacoesCanceladas() {
            return implantacoesCanceladas;
        }

        public void setImplantacoesCanceladas(long implantacoesCanceladas) {
            this.implantacoesCanceladas = implantacoesCanceladas;
        }

        public long getIchosPendentes() {
            return ichosPendentes;
        }

        public void setIchosPendentes(long ichosPendentes) {
            this.ichosPendentes = ichosPendentes;
        }

        public long getIchosComFalha() {
            return ichosComFalha;
        }

        public void setIchosComFalha(long ichosComFalha) {
            this.ichosComFalha = ichosComFalha;
        }
    }
}
