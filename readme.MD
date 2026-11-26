# Cabe no Bolso - Planejador Inteligente 💰

Sistema de gerenciamento financeiro colaborativo desenvolvido em Java. Permite cadastrar participantes, registrar despesas (fixas ou por pessoa) e simular cenários de corte de gastos (despesas não essenciais) caso o orçamento seja ultrapassado.

![Captura de tela do programa aberto](src/img/screenshot.png)

## 🛠️ Tecnologias e Requisitos
- **JDK:** Versão 17 ou superior.
- **Dependências Externas:** Nenhuma. O projeto utiliza apenas bibliotecas nativas do Java (`javax.swing`, `java.awt`, `java.io`).
- **Interface Gráfica:** Construída do zero utilizando `Java2D` puro para renderização customizada (anti-aliasing, bordas arredondadas e sistema de toasts).

## 📂 Organização das Pastas (Arquitetura MVC)
O código-fonte está estruturado em pacotes com responsabilidades claras:

```text
src/
 ├── controlador/      # Intermedia a comunicação entre Visão e Modelo (ControladorPlanejamento)
 ├── modelo/           # Regras de negócio, Entidades (Participante, Despesa) e Lógica do Simulador
 ├── persistencia/     # Manipulação de arquivos (GerenciadorArquivo, PersistenciaDados)
 ├── principal/        # Ponto de entrada da aplicação (Main)
 └── visao/            # Componentes visuais e interface gráfica (TelaPrincipal)
```
* O arquivo `planejamento.ser` é gerado automaticamente na raiz do projeto para salvar os dados localmente via serialização.

## 🚀 Como Compilar e Executar

**Via IDE (Recomendado - IntelliJ, Eclipse, VSCode):**
1. Abra a pasta do projeto na sua IDE.
2. Certifique-se de que o SDK (JDK 17+) está configurado no projeto.
3. Navegue até `src/principal/Main.java`.
4. Execute o método `main`.

**Via Terminal (Linha de Comando):**

**🍎 Linux / macOS:**
1. Navegue até a raiz do projeto (onde está a pasta `src`).
2. Crie a pasta para os binários: `mkdir out`
3. Compile os arquivos:
   ```bash
   javac -d out src/**/*.java
   ```
4. Execute o programa:
   ```bash
   java -cp out principal.Main
   ```

**🪟 Windows (CMD):**
1. Navegue até a raiz do projeto (onde está a pasta `src`).
2. Crie a pasta para os binários: `mkdir out`
3. Como o CMD clássico não suporta busca recursiva direta, crie uma lista dos arquivos e compile:
   ```cmd
   dir /s /B *.java > sources.txt
   javac -d out @sources.txt
   ```
4. Execute o programa:
   ```cmd
   java -cp out principal.Main
   ```

## 🧪 Dados, Testes e Reprodução (Validação)
Para reproduzir os testes da aplicação e validar as regras de negócio:

1. **Cadastro de Participantes:** Na aba "Participantes", cadastre "Ariel" (Orçamento: R$ 100) e "João" (Orçamento: R$ 50).
2. **Cadastro de Despesas:** Na aba "Despesas":
    - Cadastre "Conta de Luz" (R$ 80, Tipo: Fixa, Essencial: Sim, Participantes: Ariel e João).
    - Cadastre "Pizza" (R$ 90, Tipo: Por Pessoa, Essencial: Não, Prioridade: 1, Participantes: Ariel e João).
3. **Persistência:** Feche o aplicativo e abra novamente. Verifique se os dados continuam lá (carregados do `planejamento.ser`).

## 🎯 Objetivo Extra: Simulador de Alternativas
O sistema conta com um algoritmo de simulação avançado. Se os gastos ultrapassarem a soma dos orçamentos, o sistema tenta resolver o problema cortando despesas supérfluas.

**Como testar o Simulador:**
1. Com os dados cadastrados no passo anterior (Total de Gastos: R$ 170 | Total Orçamento: R$ 150). O orçamento "estourou".
2. Vá até a aba "Relatórios" e clique em **"Simular alternativas"**.
3. **Resultado esperado:** O algoritmo identificará que o orçamento foi excedido. Ele removerá a despesa "Pizza" (não essencial, prioridade 1) e manterá a "Conta de Luz" (essencial). O console exibirá o relatório detalhado do corte.
4. **Caso sem solução:** Se você alterar a "Conta de Luz" para R$ 200 (ultrapassando o orçamento apenas com itens essenciais), o simulador avisará que não é possível fechar a conta, pois itens essenciais não podem ser cortados.

## 🏛️ Justificativa da Modelagem (Design Patterns)
- **MVC (Model-View-Controller):** Separa a interface gráfica (`visao`) das regras de cálculo (`modelo`), facilitando a manutenção e permitindo que a interface seja totalmente redesenhada sem quebrar a lógica de negócio.
- **Herança e Polimorfismo:** A classe abstrata `Despesa` generaliza os atributos comuns, enquanto `DespesaFixa` e `DespesaPorPessoa` implementam a lógica específica de `calcularRateio()`. O sistema trata todas as despesas de forma polimórfica.
- **Injeção de Dependência:** O uso da interface `PersistenciaDados` garante que o `ControladorPlanejamento` não dependa diretamente da manipulação de arquivos (baixo acoplamento). Se no futuro o salvamento for migrado para um Banco de Dados SQL, basta criar uma nova classe que implemente a interface, sem alterar o controlador.