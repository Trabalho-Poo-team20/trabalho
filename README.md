# Cabe no Bolso — Planejador de Despesas Compartilhadas

Aplicação desktop em Java Swing para planejamento de despesas compartilhadas em grupo, com suporte a rateio fixo (divisão exata em centavos por código crescente) e por pessoa, controle de orçamentos individuais, persistência por serialização e simulador de alternativas viáveis[cite: 1, 2, 3, 9].

![Captura de tela do programa aberto](img/screenshot.png)

## Tecnologias e Requisitos
- **JDK:** Versão 17 ou superior.
- **Dependências Externas:** Nenhuma (apenas bibliotecas padrão `javax.swing`, `java.awt`, `java.io` e `java.util`).

## Organização das Pastas (Arquitetura MVC)
```text
src/
 ├── controlador/      # ControladorPlanejamento (mediação entre Visão, Modelo e Persistência)
 ├── modelo/           # Planejamento, Participante, Despesa, DespesaFixa, DespesaPorPessoa, SimuladorAlternativas
 ├── persistencia/     # Interface PersistenciaDados e implementação GerenciadorArquivo
 ├── principal/        # Ponto de entrada da aplicação (Main)
 └── visao/            # Interface gráfica Swing/Java2D (TelaPrincipal)
