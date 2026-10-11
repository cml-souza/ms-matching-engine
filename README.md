# Matching Engine em Java

Matching engine simples para **um único ativo**, desenvolvida em **Java**, com livro de ofertas em memória, prioridade **preço-tempo (FIFO)** e arquitetura modular.

Suporta ordens **limit**, **market** e **pegged** (bid/offer), além de **cancelamento**, **alteração** e **visualização do livro de ofertas**.

---

## Como Executar

**Pré-requisitos:** JDK 17+ e Maven 3.8+ (ou suporte nativo na IDE).

```bash
# Compilar o projeto e executar a suíte de testes unitários
mvn clean test
```

**No IntelliJ IDEA / Eclipse:**
1. Abra a pasta raiz do projeto (o `pom.xml` será detectado automaticamente).
2. Execute a classe principal `Main`.
3. Para rodar a suíte de testes automatizados, clique com o botão direito na pasta `src/test/java` e selecione **Run 'All Tests'**.

---

## Comandos e Interação via Console

A aplicação disponibiliza um menu interativo via terminal, totalmente protegido contra erros de digitação e falhas de buffer:

| Opção do Menu | Ação / Descrição |
|---|---|
| **1** | **Inserir Nova Ordem**: Permite escolher o lado (`BUY` / `SELL`) e o tipo de ordem:
| &nbsp;&nbsp;&nbsp;&nbsp;↳ Sub-opção `1` | • **Limit**: Insere uma ordem limitada com preço fixo e quantidade informados. |
| &nbsp;&nbsp;&nbsp;&nbsp;↳ Sub-opção `2` | • **Market**: Insere uma ordem a mercado para execução imediata contra a liquidez disponível. |
| &nbsp;&nbsp;&nbsp;&nbsp;↳ Sub-opção `3` | • **Pegged**: Ordem ancorada que acompanha automaticamente o melhor preço do seu lado do livro. ||
| **2** | **Visualizar Livro de Ofertas (`Print Book`)**: Exibe o estado atualizado das pontas de compra e venda. |
| **3** | **Cancelar Ordem**: Solicita o identificador (`ID_X`) para remover uma ordem ativa do livro. |
| **4** | **Modificar Ordem**: Permite alterar o preço e/ou quantidade de uma ordem existente pelo seu ID. |
| **5** | **Sair (`Exit`)**: Encerra a execução da aplicação com segurança. |

*Nota: Os identificadores de ordens (`ID_1`, `ID_2`, `ID_3`, ...) são gerados de forma automática e incremental pelo sistema, garantindo unicidade sem esforço manual.*

---

## Arquitetura do Sistema

O projeto foi refatorado e estruturado de forma desacoplada para isolar responsabilidades e facilitar a testabilidade:

```text
com.ms.matchingengine
├── Main                  # Ponto de entrada da aplicação
├── ConsoleInterface      # Gerenciamento de I/O via terminal com leitura segura
├── OrderBook             # Coração do sistema: gerencia as pontas de compra e venda
├── OrderBookPrinter      # Formatação e exibição visual do livro de ofertas
├── OrderValidator        # Validação de preços, quantidades e restrições de ordens
├── TradeExecutor         # Lógica de cruzamento e execução de negócios
└── OrderIdGenerator      # Componente isolado para geração automática de IDs sequenciais
```

### Princípios Arquiteturais Aplicados

- **Princípio da Responsabilidade Única (SRP):** 
  - O `OrderBook` é responsável exclusivamente por armazenar e organizar os níveis de preço.
  - O `OrderIdGenerator` gerencia a sequência de identificadores.
  - A `ConsoleInterface` cuida unicamente do I/O e sanitização do `Scanner`.
- **Resiliência a Falhas de Entrada (Sanitização):**
  - Métodos auxiliares dedicados (`readIntInput` e `readDoubleInput`) previnem `InputMismatchException` e loops infinitos de buffer no `Scanner`, solicitando correções amigáveis sem derrubar o processo.
- **Testabilidade por Injeção de Dependências:**
  - Componentes de lógica de negócio e geradores de ID não dependem de I/O de terminal, permitindo cobertura por testes unitários independentes.

---

## Decisões Técnicas e Regras de Negócio

### 1. Geração Automática de IDs (`OrderIdGenerator`)
A criação manual de IDs por parte do usuário foi substituída por um componente automatizado. Isso elimina conflitos de duplicação, reduz o atrito na digitação de ordens e garante o padrão sequencial (`ID_1`, `ID_2`, etc.).

### 2. Prioridade Preço-Tempo (FIFO)
- **Preço:** Compras de maior valor e vendas de menor valor possuem prioridade no topo do livro.
- **Tempo:** Em um mesmo nível de preço, ordens que chegaram primeiro são executadas primeiro.

### 3. Regra de Execução para Ordens Pegged
- Ordens *Pegged* rastreiam o melhor preço do seu **próprio lado** do livro.
- Exigem pelo menos uma ordem não-pegged (limitada) no mesmo lado para servirem de referência de preço.
- Quando o topo do livro é alterado ou cancelado, a ordem *Pegged* ajusta automaticamente seu preço mantendo sua ordem de chegada no novo nível.

### 4. Reordenação em Alterações de Ordem
- Redução de quantidade: **Mantém** a prioridade na fila.
- Aumento de quantidade ou alteração de preço: **Perde** a prioridade temporal, movendo a ordem para o final da fila do respectivo nível.

---

## Testes Automatizados

O projeto utiliza **JUnit 5** para assegurar a corretude das regras de negócio, a ausência de regressões e a estabilidade das execuções.

```bash
mvn test
```

### Cobertura Principal
- **`ConsoleInterfaceTest`**:Valida fluxos de menu, exibições, cancelamentos, modificações e interceptação de entradas inválidas via terminal.
- **`MatchingEngineMainTest`**: Valida a inicialização correta da aplicação a partir da classe principal (Main).
- **`OrderBookPrinterTest`**: Garante a formatação e exibição correta do livro de ofertas (vazio, unilaterais e com ordens cruzadas).
- **`OrderBookTest`**: Valida a ordenação de preços, prioridade FIFO, cancelamentos, modificações e rastreamento de ordens pegged.
- **`OrderIdGeneratorTest`**: Valida a geração sequencial e incremental correta dos IDs (ID_1, ID_2, ...).
- **`OrderTest`**: Testa construtores, invariantes, redução de quantidade remanescente e regras de imutabilidade/atualização.
- **`OrderTypeTest`**: Assegura a exposição correta dos tipos de ordens suportados (LIMIT, MARKET, PEGGED_BID, PEGGED_OFFER).
- **`OrderValidatorTest`**: Valida as restrições de IDs duplicados, preços não positivos e tipos de ordens aceitos.
- **`SideTest`**: Valida a correta exposição das pontas de negociação (BUY e SELL).
- **`TestSupport`**: Utilitário auxiliar para simulação de streams, captura de saída de console e criação rápida de ordens em testes.
- **`TradeExecutorTest`**: Valida o cruzamento de ordens a mercado, consumo de liquidez dos melhores níveis e tratamento de livros vazios.
